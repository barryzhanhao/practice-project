package pers.james.practice.springboog3.internal.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pers.james.practice.springboog3.internal.entity.UserPo;
import pers.james.practice.springboog3.internal.mapper.UserMapper;

@Slf4j
@Repository
public class UserRepository {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取user
     */
    public UserPo findUser(Long id) {
        UserPo user = userMapper.selectById(id);
        return user;
    }

    /**
     * 获取user
     */
    public UserPo findUser2(Long id) {
        UserPo user = userMapper.selectOne(
                new QueryWrapper<UserPo>()
                        .lambda()
                        .eq(UserPo::getId, id));
        return user;
    }

}
