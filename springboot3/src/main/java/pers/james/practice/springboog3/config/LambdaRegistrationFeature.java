package pers.james.practice.springboog3.config;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import pers.james.practice.springboog3.Application;
import pers.james.practice.springboog3.internal.repository.UserRepository;

/**
 * lambda 表达式注入到graal中
 */
public class LambdaRegistrationFeature implements Feature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        // TODO 这里需要将lambda表达式所使用的成员类都注册上来,具体情况视项目情况而定,一般扫描@Controller和@Service的会多点.
        RuntimeSerialization.registerLambdaCapturingClass(UserRepository.class);
    }

}