package com.myapp.learningtube.global.config;

import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** JPA Auditing, 빈 DB 시 시드 등 (Swagger 설정은 {@code global.swagger} 참고). */
@Configuration
@EnableJpaAuditing
public class GlobalConfig {

    private static final Logger log = LoggerFactory.getLogger(GlobalConfig.class);

    /** JWT 테스트 로그인(userId=1)과 DB FK를 맞추기 위해, 빈 DB일 때 시드 사용자 1명 삽입. */
    @Bean
    public ApplicationRunner databaseSeedRunner(UserRepository userRepository) {
        return args -> {
            long n = userRepository.count();
            log.info("LearningTube DB 준비 완료 (users={})", n);
            if (n > 0) {
                return;
            }
            User u = new User("test@learningtube.local", "{noop}unused", "Test User", UserRole.MEMBER);
            userRepository.save(u);
            log.info("Seeded initial user id={} for test-login (JWT sub=1)", u.getId());
        };
    }
}
