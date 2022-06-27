package run.halo.app.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import run.halo.app.model.enums.Mode;

import java.time.Duration;
import java.util.ArrayList;

import static run.halo.app.model.support.HaloConst.*;
import static run.halo.app.utils.HaloUtils.ensureSuffix;


/**
 * Halo configuration properties.
 * Halo项目最基本的配置信息都在这里进行设置
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
@Data
@ConfigurationProperties("halo")
public class HaloProperties {

    /**
     * Doc api disabled. (Default is true)
     */
    private boolean docDisabled = true;

    /**
     * Production env. (Default is true)
     */
    private boolean productionEnv = true;

    /**
     * Authentication enabled
     */
    private boolean authEnabled = true;

    /**
     * Halo startup mode.
     * 这里是自定义的Enum类
     */
    private Mode mode = Mode.PRODUCTION;

    /**
     * Admin path.
     */
    private String adminPath = "admin";

    /**
     * Work directory. 组合获得配置文件的路径
     */
    private String workDir = ensureSuffix(USER_HOME, FILE_SEPARATOR) + ".halo" + FILE_SEPARATOR;

    /**
     * Halo backup directory.(Not recommended to modify this config);
     */
    private String backupDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-backup" + FILE_SEPARATOR;

    /**
     * Halo data export directory.
     */
    private String dataExportDir = ensureSuffix(TEMP_DIR, FILE_SEPARATOR) + "halo-data-export" + FILE_SEPARATOR;

    /**
     * Upload prefix.
     */
    private String uploadUrlPrefix = "upload";

    /**
     * Download Timeout.
     * 时间使用了JDK8中的time类
     */
    private Duration downloadTimeout = Duration.ofSeconds(30);

    /**
     * cache store impl 缓存的模式
     * memory
     * level
     */
    private String cache = "memory";

    private ArrayList<String> cacheRedisNodes = new ArrayList<>();

    private String cacheRedisPassword = "";


}
