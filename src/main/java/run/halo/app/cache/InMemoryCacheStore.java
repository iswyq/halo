package run.halo.app.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory cache store.
 * 涉及到了锁
 * @author johnniang
 */
@Slf4j
public class InMemoryCacheStore extends AbstractStringCacheStore {

    /**
     * Cleaner schedule period. (ms)
     */
    private final static long PERIOD = 60 * 1000;

    /**
     * Cache container.这个字段会与并发有关，涉及到了Concurrent
     * 定义的是Cache的HashMap作为容器，是一个线程安全的
     */
    private final static ConcurrentHashMap<String, CacheWrapper<String>> CACHE_CONTAINER = new ConcurrentHashMap<>();

    //计时器
    private final Timer timer;

    /**
     * Lock.对象锁 定义了内部的锁，但是没有明显的使用；外部拿到执行权之后会获得该锁
     */
    private final Lock lock = new ReentrantLock();

    public InMemoryCacheStore() {
        // Run a cache store cleaner
        timer = new Timer();
        // 添加个缓存到期清理的任务执行  Cleaner继承了TimerTask
        // TODO 这个方法的确很好用
        timer.scheduleAtFixedRate(new CacheExpiryCleaner(), 0, PERIOD);
    }

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        // CacheWrapper 是针对字符串的Wrapper
        Assert.hasText(key, "Cache key must not be blank");

        return Optional.ofNullable(CACHE_CONTAINER.get(key));
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        // Put the cache wrapper  容器是一个Map，但是添加元素后的返回值却是一个普通对象
        CacheWrapper<String> putCacheWrapper = CACHE_CONTAINER.put(key, cacheWrapper);

        log.debug("Put [{}] cache 添加后的缓存: [{}], 原始的缓存 cache wrapper: [{}]", key, putCacheWrapper, cacheWrapper);
    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(cacheWrapper, "Cache wrapper must not be null");

        log.debug("Preparing to put key: [{}], value: [{}]", key, cacheWrapper);
        // 下面的代码需要获得锁来运行
        // TODO lock的使用
        lock.lock();
        try {
            // Get the value before
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }

            // Put the cache wrapper
            putInternal(key, cacheWrapper);
            log.debug("Put successfully");
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(String key) {
        Assert.hasText(key, "Cache key must not be blank");

        CACHE_CONTAINER.remove(key);
        log.debug("Removed key: [{}]", key);
    }

    @PreDestroy
    public void preDestroy() {
        log.debug("取消定时器中的所有任务");
        timer.cancel();
        clear();
    }

    private void clear() {
        CACHE_CONTAINER.clear();
    }

    /**
     * Cache cleaner.
     *
     * @author johnniang
     * @date 03/28/19
     */
    private class CacheExpiryCleaner extends TimerTask {

        @Override
        public void run() {
            // 使用到了流，key的集合的forEach方法执行lambda表达式，每个key都会传入执行语句。类似循环
            CACHE_CONTAINER.keySet().forEach(key -> {
                if (!InMemoryCacheStore.this.get(key).isPresent()) {
                    // 为空的时候删除
                    log.debug("Deleted the cache: [{}] for expiration", key);
                }
            });
        }
    }
}
