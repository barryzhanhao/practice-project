package pers.james.practice.springboog3.internal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import pers.james.practice.springboog3.internal.entity.UserPo;

@Mapper
public interface UserMapper extends BaseMapper<UserPo> {

    @Select("select * from  user where  id = #{id}")
    UserPo selectById(@Param("id") Long id);

}
