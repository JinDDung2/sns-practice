package com.example.fasns.global.config;

import com.example.fasns.global.jwt.JwtAuthenticationFilter;
import com.example.fasns.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebConfig {

    private final String SWAGGER = "/swagger-ui/**";

    private final String[] MEMBER_PERMIT = {
            "/api/members/register",
            "/api/members/login"
    };

    private final String[] MEMBER_AUTH = {
            "/api/members",
            "/api/members/**/nickname-histories",
            "/api/members/**"
    };

    private final String[] POST_PERMIT = {
            "/api/posts/**",
    };

    private final String[] POST_AUTH = {
            "/api/posts",
            "/api/posts/members/**",
            "/api/posts/members/**/cursor",
            "/api/posts/members/**/timeline",
    };

    private final String[] FOLLOW_AUTH = {
            "/api/follow",
            "/api/follow/**"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .cors();

        httpSecurity
                .authorizeHttpRequests()
                .antMatchers(SWAGGER).permitAll()
                .antMatchers(MEMBER_PERMIT).permitAll()
                .antMatchers(POST_PERMIT).permitAll()
                .antMatchers(MEMBER_AUTH).authenticated()
                .antMatchers(FOLLOW_AUTH).authenticated()
                .antMatchers(POST_AUTH).authenticated();

        httpSecurity
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}