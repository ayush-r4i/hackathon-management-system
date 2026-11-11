package com.collegefest.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Admin routes - require ADMIN role
        registry.addInterceptor(new SessionAuthInterceptor("ADMIN"))
                .addPathPatterns(
                        "/dashboard/**",
                        "/events/**",
                        "/teams/**",
                        "/participants/**",
                        "/judges/**",
                        "/scores/**",
                        "/leaderboard/**",
                        "/admin/**"
                )
                .excludePathPatterns(
                        "/login", "/logout",
                        "/css/**", "/js/**", "/images/**",
                        "/api/**",
                        "/portal/**",
                        "/judge/**"
                );

        // Participant portal routes - require PARTICIPANT role
        registry.addInterceptor(new SessionAuthInterceptor("PARTICIPANT"))
                .addPathPatterns("/portal/**")
                .excludePathPatterns(
                        "/login", "/logout",
                        "/css/**", "/js/**", "/images/**",
                        "/api/**"
                );

        // Judge portal routes - require JUDGE role
        registry.addInterceptor(new SessionAuthInterceptor("JUDGE"))
                .addPathPatterns("/judge/**")
                .excludePathPatterns(
                        "/login", "/logout",
                        "/css/**", "/js/**", "/images/**",
                        "/api/**"
                );
    }
}
