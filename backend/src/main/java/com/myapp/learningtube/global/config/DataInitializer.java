package com.myapp.learningtube.global.config;

import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * JWT 테스트 로그인(userId=1)과 DB FK를 맞추기 위해, 빈 DB일 때 시드 사용자 1명 삽입.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User u = new User("test@learningtube.local", "{noop}unused", "Test User", UserRole.MEMBER);
        userRepository.save(u);
        log.info("Seeded initial user id={} for test-login (JWT sub=1)", u.getId());
    }
}
