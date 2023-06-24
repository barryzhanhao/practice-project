package pers.james.practice.springboog3.internal.entity;

//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
//@TableName("user")
public class UserPo {

//    @TableId(type = IdType.AUTO)
    private Long id;

//    @TableField
    private String name;

//    @TableField("`type`")
    private String type;

    private Date createdDate;
}
