package pers.james.practice.springboog3.internal.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.james.practice.springboog3.internal.entity.UserPo;
import pers.james.practice.springboog3.internal.repository.UserRepository;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserPo getUser(Long id) {
        return userRepository.findUser(id);
    }

    public UserPo getUser2(Long id) {
        return userRepository.findUser2(id);
    }
}
