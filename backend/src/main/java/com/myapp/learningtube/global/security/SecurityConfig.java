package com.myapp.learningtube.global.security;

import com.myapp.learningtube.global.auth.oauth.CookieOAuth2AuthorizationRequestRepository;
import com.myapp.learningtube.global.auth.oauth.GoogleOidcUserService;
import com.myapp.learningtube.global.auth.oauth.OAuth2AuthenticationFailureHandler;
import com.myapp.learningtube.global.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.myapp.learningtube.global.config.CorsProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final GoogleOidcUserService googleOidcUserService;
    private final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;
    private final OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            GoogleOidcUserService googleOidcUserService,
            CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository,
            OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.googleOidcUserService = googleOidcUserService;
        this.cookieOAuth2AuthorizationRequestRepository = cookieOAuth2AuthorizationRequestRepository;
        this.oauth2AuthorizationRequestResolver = oauth2AuthorizationRequestResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .headers(
                        headers -> {
                            if (h2ConsoleEnabled) {
                                headers.frameOptions(frame -> frame.disable());
                            } else {
                                headers.frameOptions(frame -> frame.sameOrigin());
                            }
                        })
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers("/h2-console/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/v1/health")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/test-login")
                                        .permitAll()
                                        .requestMatchers("/oauth2/**", "/login/**")
                                        .permitAll()
                                        .requestMatchers(SWAGGER_WHITELIST)
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()
                                        .requestMatchers("/api/v1/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .oauth2Login(
                        o ->
                                o.authorizationEndpoint(
                                                a ->
                                                        a.authorizationRequestRepository(
                                                                        cookieOAuth2AuthorizationRequestRepository)
                                                                .authorizationRequestResolver(
                                                                        oauth2AuthorizationRequestResolver))
                                        .userInfoEndpoint(u -> u.oidcUserService(googleOidcUserService))
                                        .successHandler(oAuth2AuthenticationSuccessHandler)
                                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * {@code http.cors(withDefaults())} 가 이 Bean을 사용한다. CORS 규칙은 {@link CorsProperties}(yml) 기준.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(corsProperties.getAllowedOrigins());
        c.setAllowedMethods(corsProperties.getAllowedMethods());
        c.setAllowedHeaders(corsProperties.getAllowedHeaders());
        c.setExposedHeaders(corsProperties.getExposedHeaders());
        c.setAllowCredentials(corsProperties.isAllowCredentials());
        c.setMaxAge(corsProperties.getMaxAgeSeconds());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
