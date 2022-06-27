package run.halo.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.cache.InMemoryCacheStore;
import run.halo.app.cache.LevelCacheStore;
import run.halo.app.cache.RedisCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.utils.HttpClientUtils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Halo configuration.
 *
 * @author johnniang
 */
@Configuration
@EnableConfigurationProperties(HaloProperties.class)
@Slf4j
public class HaloConfiguration {

    @Autowired
    HaloProperties haloProperties;

    // 使用了设计模式
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        builder.failOnEmptyBeans(false);
        return builder.build();
    }

    @Bean
    public RestTemplate httpsRestTemplate(RestTemplateBuilder builder)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate httpsRestTemplate = builder.build();
        httpsRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(
                (int) haloProperties.getDownloadTimeout().toMillis())));
        return httpsRestTemplate;
    }

    // 使用条件加载 如果用户没有自定义配置，则加载这个类作为Bean
    @Bean
    @ConditionalOnMissingBean
    // 返回缓存的存储方式
    public AbstractStringCacheStore stringCacheStore() {
        AbstractStringCacheStore stringCacheStore;
        switch (haloProperties.getCache()) {
            case "level":
                stringCacheStore = new LevelCacheStore();
                break;
            case "redis":
                stringCacheStore = new RedisCacheStore(this.haloProperties);
                break;
            case "memory":
                // 这里没写
            default:
                //memory or default
                stringCacheStore = new InMemoryCacheStore();
                break;

        }
        log.info("halo cache store load impl : [{}]", stringCacheStore.getClass());
        return stringCacheStore;

    }
}
