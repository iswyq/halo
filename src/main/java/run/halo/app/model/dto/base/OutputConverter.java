package run.halo.app.model.dto.base;

import org.springframework.lang.NonNull;

import static run.halo.app.utils.BeanUtils.updateProperties;

/**
 * Converter interface for output DTO.
 * TODO 传输对象都有一个最基本的input转换器和output转换器  将输入转换为输出
 * <b>The implementation type must be equal to DTO type</b>
 *
 * @param <DTO>    the implementation class type
 * @param <DOMAIN> domain type
 * @author johnniang
 */
// 这个泛型的写法有点厉害：Dto继承了OutputConverter，DOMAIN是自己的泛型
public interface OutputConverter<DTO extends OutputConverter<DTO, DOMAIN>, DOMAIN> {

    /**
     * Convert from domain.(shallow)
     * 还给了接口中一个方法的默认实现  强强强
     * @param domain domain data
     * @return converted dto data
     */
    @SuppressWarnings("unchecked")
    @NonNull // 这里饶了一圈，还是要继承OutputConverter  LinkDTO这个Entity本来就继承了
    default <T extends DTO> T convertFrom(@NonNull DOMAIN domain) {
        // 就是使用Spring的对象copy方法进行对象拷贝
        updateProperties(domain, this);

        return (T) this;
    }
}
