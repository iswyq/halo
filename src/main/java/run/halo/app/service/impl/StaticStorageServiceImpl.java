package run.halo.app.service.impl;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.StaticStorageChangedEvent;
import run.halo.app.exception.FileOperationException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.support.StaticFile;
import run.halo.app.service.StaticStorageService;
import run.halo.app.utils.FileUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * StaticStorageService implementation class.
 *
 * @author ryanwang
 * @date 2019-12-06
 */
@Service
@Slf4j
public class StaticStorageServiceImpl implements StaticStorageService, ApplicationListener<ApplicationStartedEvent> {

    private final Path staticDir;

    private final ApplicationEventPublisher eventPublisher;

    public StaticStorageServiceImpl(HaloProperties haloProperties,
                                    ApplicationEventPublisher eventPublisher) throws IOException {
        staticDir = Paths.get(haloProperties.getWorkDir(), STATIC_FOLDER);
        this.eventPublisher = eventPublisher;
        FileUtils.createIfAbsent(staticDir);
    }

    @Override
    public List<StaticFile> listStaticFolder() {
        return listStaticFileTree(staticDir);
    }

    @Nullable
    private List<StaticFile> listStaticFileTree(@NonNull Path topPath) {
        Assert.notNull(topPath, "Top path must not be null");

        if (!Files.isDirectory(topPath)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(topPath)) {
            List<StaticFile> staticFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                StaticFile staticFile = new StaticFile();
                staticFile.setId(IdUtil.fastSimpleUUID());
                staticFile.setName(path.getFileName().toString());
                staticFile.setPath(path.toString());
                staticFile.setRelativePath(StringUtils.removeStart(path.toString(), staticDir.toString()));
                staticFile.setIsFile(Files.isRegularFile(path));
                try {
                    staticFile.setCreateTime(Files.getLastModifiedTime(path).toMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                staticFile.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(path.toFile()));
                if (Files.isDirectory(path)) {
                    staticFile.setChildren(listStaticFileTree(path));
                }

                staticFiles.add(staticFile);
            });

            staticFiles.sort(new StaticFile());
            return staticFiles;
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    @Override
    public void delete(String relativePath) {
        Assert.notNull(relativePath, "Relative path must not be null");

        Path path = Paths.get(staticDir.toString(), relativePath);

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), path.toString());

        log.debug(path.toString());

        try {
            if (path.toFile().isDirectory()) {
                FileUtils.deleteFolder(path);
            } else {
                Files.deleteIfExists(path);
            }
            onChange();
        } catch (IOException e) {
            throw new FileOperationException("?????? " + relativePath + " ????????????", e);
        }
    }

    @Override
    public void createFolder(String basePath, String folderName) {
        Assert.notNull(folderName, "Folder name path must not be null");

        Path path;

        if (StringUtils.startsWith(folderName, API_FOLDER_NAME)) {
            throw new FileOperationException("???????????? " + folderName + " ?????????");
        }

        if (StringUtils.isEmpty(basePath)) {
            path = Paths.get(staticDir.toString(), folderName);
        } else {
            path = Paths.get(staticDir.toString(), basePath, folderName);
        }

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), path.toString());

        if (path.toFile().exists()) {
            throw new FileOperationException("?????? " + path.toString() + " ?????????").setErrorData(path);
        }

        try {
            FileUtils.createIfAbsent(path);
        } catch (IOException e) {
            throw new FileOperationException("?????? " + path.toString() + " ????????????", e);
        }
    }

    @Override
    public void upload(String basePath, MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        Path uploadPath;

        if (StringUtils.startsWith(file.getOriginalFilename(), API_FOLDER_NAME)) {
            throw new FileOperationException("???????????? " + file.getOriginalFilename() + " ?????????");
        }

        if (StringUtils.isEmpty(basePath)) {
            uploadPath = Paths.get(staticDir.toString(), file.getOriginalFilename());
        } else {
            uploadPath = Paths.get(staticDir.toString(), basePath, file.getOriginalFilename());
        }

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), uploadPath.toString());

        if (uploadPath.toFile().exists()) {
            throw new FileOperationException("?????? " + file.getOriginalFilename() + " ?????????").setErrorData(uploadPath);
        }

        try {
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
            onChange();
        } catch (IOException e) {
            throw new ServiceException("??????????????????").setErrorData(uploadPath);
        }
    }

    @Override
    public void rename(String basePath, String newName) {
        Assert.notNull(basePath, "Base path must not be null");
        Assert.notNull(newName, "New name must not be null");

        Path pathToRename;

        if (StringUtils.startsWith(newName, API_FOLDER_NAME)) {
            throw new FileOperationException("??????????????? " + newName + " ?????????");
        }

        pathToRename = Paths.get(staticDir.toString(), basePath);

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), pathToRename.toString());

        try {
            FileUtils.rename(pathToRename, newName);
            onChange();
        } catch (FileAlreadyExistsException e) {
            throw new FileOperationException("?????????????????? " + newName + " ?????????");
        } catch (IOException e) {
            throw new FileOperationException("????????? " + pathToRename.toString() + " ??????");
        }
    }

    @Override
    public void save(String path, String content) {
        Assert.notNull(path, "Path must not be null");

        Path savePath = Paths.get(staticDir.toString(), path);

        // check if the path is valid (not outside staticDir)
        FileUtils.checkDirectoryTraversal(staticDir.toString(), savePath.toString());

        // check if file exist
        if (!Files.isRegularFile(savePath)) {
            throw new FileOperationException("?????? " + path + " ?????????");
        }

        try {
            Files.write(savePath, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServiceException("?????????????????? " + path, e);
        }
    }

    private void onChange() {
        eventPublisher.publishEvent(new StaticStorageChangedEvent(this, staticDir));
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        onChange();
    }
}
