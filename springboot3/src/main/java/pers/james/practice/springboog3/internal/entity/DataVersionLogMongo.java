
package pers.james.practice.springboog3.internal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 数据版本日志
 *
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@Document("tt_data_version_log")
public class DataVersionLogMongo {

    private String tableName;

    private String dataId;

    private String version;

}
