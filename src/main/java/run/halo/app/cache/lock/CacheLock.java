package run.halo.app.cache.lock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Cache lock annotation.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheLock {

    /**
     * Cache prefix, default is ""
     *
     * @return cache prefix
     */
    @AliasFor("value")
    String prefix() default "";

    /**
     * Alias of prefix, default is ""
     * 默认值为空
     * @return alias of prefix
     */
    @AliasFor("prefix")
    String value() default "";

    /**
     * Expired time, default is 5.
     *
     * @return expired time
     */
    long expired() default 5;

    /**
     * Time unit, default is TimeUnit.SECONDS.
     * 时间的单位
     * @return time unit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Delimiter, default is ':'
     * 分隔符
     * @return delimiter
     */
    String delimiter() default ":";

    /**
     * Whether delete cache after method invocation.
     * invocation 调用
     * true为删除，false不删除
     * @return true if delete cache after method invocation; false otherwise
     */
    boolean autoDelete() default true;

    /**
     * Whether trace the request info.
     * 跟踪请求的信息
     * @return true if trace the request info; false otherwise
     */
    boolean traceRequest() default false;
}
