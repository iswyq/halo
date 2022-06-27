package run.halo.app.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * comment_black_list
 *
 * @author Lei XinXin
 * @date 2020/1/3
 */
@Data
@Entity
@Table(name = "comment_black_list")
@EqualsAndHashCode(callSuper = true)
@Builder  // @Builder，就是为 java bean 生成一个构建器。Builder 模式 又被称作 建造者模式 或者 生成器模式.是一种设计模式
// builder相关知识 https://zhuanlan.zhihu.com/p/83219997
@AllArgsConstructor
@NoArgsConstructor
public class CommentBlackList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "run.halo.app.model.entity.support.CustomIdGenerator")
    private Long id;

    @Column(name = "ip_address", length = 127, nullable = false)
    private String ipAddress;

    /**
     * 封禁时间
     */
    @Column(name = "ban_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date banTime;
}
