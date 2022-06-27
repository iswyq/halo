package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Post entity.
 * 文章实体
 * @author johnniang
 */
@Entity(name = "Post")
@DiscriminatorValue(value = "0")
public class Post extends BasePost {

}
