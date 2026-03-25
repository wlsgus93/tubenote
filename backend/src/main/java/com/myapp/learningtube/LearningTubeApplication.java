package com.myapp.learningtube;

import com.myapp.learningtube.domain.analytics.AnalyticsProperties;
import com.myapp.learningtube.domain.dashboard.DashboardProperties;
import com.myapp.learningtube.global.auth.jwt.JwtProperties;
import com.myapp.learningtube.global.config.CorsProperties;
import com.myapp.learningtube.infra.youtube.YoutubeApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    JwtProperties.class,
    YoutubeApiProperties.class,
    DashboardProperties.class,
    AnalyticsProperties.class,
    CorsProperties.class
})
public class LearningTubeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningTubeApplication.class, args);
    }
}
