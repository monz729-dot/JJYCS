package com.ysc.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("minimal")
@Order(1)
public class MinimalSecurityConfig {

    @Bean
    public SecurityFilterChain minimalFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(authorize -> 
                authorize
                    .requestMatchers("/test/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .requestMatchers("/dashboard/**").permitAll()
                    .requestMatchers("/orders/**").permitAll()
                    .requestMatchers("/hscode/**").permitAll()
                    .anyRequest().authenticated()
            );

        return http.build();
    }
}