package run.halo.app.cache;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Cache wrapper.
 * 缓存的实体包装类，将常用信息包装
 * @author johnniang
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
class CacheWrapper<V> implements Serializable {

    /**
     * Cache data
     */
    private V data;

    /**
     * Expired time.
     */
    private Date expireAt;

    /**
     * Create time.
     */
    private Date createAt;
}
