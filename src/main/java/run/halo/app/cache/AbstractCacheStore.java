package run.halo.app.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.utils.DateUtils;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Abstract cache store.
 * 定义抽象类的缓存
 * 是一种实现方式，定义基本的方法
 * 该类中的方法直接调用抽象方法，那么在子类实现抽象方法后，在本类中调用抽象方法的方法自然可以用
 * @author johnniang
 * @date 3/28/19
 */
@Slf4j
public abstract class AbstractCacheStore<K, V> implements CacheStore<K, V> {

    /**
     * Get cache wrapper by key.
     *
     * @param key key must not be null
     * @return an optional cache wrapper
     */
    @NonNull
    abstract Optional<CacheWrapper<V>> getInternal(@NonNull K key);

    /**
     * Puts the cache wrapper.
     *
     * @param key          key must not be null
     * @param cacheWrapper cache wrapper must not be null
     */
    abstract void putInternal(@NonNull K key, @NonNull CacheWrapper<V> cacheWrapper);

    /**
     * Puts the cache wrapper if the key is absent.
     * 不写具体的实现，只声明一个方法，留给子类来具体实现
     *
     * @param key          key must not be null
     * @param cacheWrapper cache wrapper must not be null
     * @return true if the key is absent and the value is set, false if the key is present before, or null if any other reason
     */
    abstract Boolean putInternalIfAbsent(@NonNull K key, @NonNull CacheWrapper<V> cacheWrapper);

    @Override
    public Optional<V> get(K key) {
        Assert.notNull(key, "Cache key must not be blank");

        // lambda表达式传入，直接使用
        // optional的流处理
        return getInternal(key).map(cacheWrapper -> {
            // Check expiration  比较缓存的到期时间是否在当前时间之前
            if (cacheWrapper.getExpireAt() != null && cacheWrapper.getExpireAt().before(run.halo.app.utils.DateUtils.now())) {
                // Expired then delete it
                log.warn("Cache key: [{}] has been expired", key);

                // Delete the key 如果已经过期，删除
                delete(key);

                // Return null
                return null;
            }

            return cacheWrapper.getData();
        });
    }

    @Override
    public void put(K key, V value, long timeout, TimeUnit timeUnit) {
        putInternal(key, buildCacheWrapper(value, timeout, timeUnit));
    }

    @Override
    public Boolean putIfAbsent(K key, V value, long timeout, TimeUnit timeUnit) {
        // 直接调用抽象方法，说明有这么个事儿；但是不进行具体实现。
        return putInternalIfAbsent(key, buildCacheWrapper(value, timeout, timeUnit));
    }

    @Override
    public void put(K key, V value) {
        putInternal(key, buildCacheWrapper(value, 0, null));
    }

    /**
     * Builds cache wrapper.
     * 这是一种设计模式，类似于构造模式；不是构造器方法
     *
     * @param value    cache value must not be null
     * @param timeout  the key expiry time, if the expiry time is less than 1, the cache won't be expired
     * @param timeUnit timeout unit must 时间的基本单位，时，分，秒，毫秒。conCurrent包下的Enum
     * @return cache wrapper
     */
    @NonNull
    private CacheWrapper<V> buildCacheWrapper(@NonNull V value, long timeout, @Nullable TimeUnit timeUnit) {
        Assert.notNull(value, "Cache value must not be null");
        //timeout设置大于0会通过
        Assert.isTrue(timeout >= 0, "Cache expiration timeout must not be less than 1");

        Date now = run.halo.app.utils.DateUtils.now();

        Date expireAt = null;

        if (timeout > 0 && timeUnit != null) {
            expireAt = DateUtils.add(now, timeout, timeUnit);
        }

        // Build cache wrapper
        CacheWrapper<V> cacheWrapper = new CacheWrapper<>();
        cacheWrapper.setCreateAt(now);
        cacheWrapper.setExpireAt(expireAt);
        cacheWrapper.setData(value);

        return cacheWrapper;
    }
}
