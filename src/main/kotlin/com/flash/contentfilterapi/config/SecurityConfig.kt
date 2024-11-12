package com.flash.contentfilterapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/api/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .exceptionHandling { exceptionHandling ->
                exceptionHandling
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        response.status = HttpStatus.FORBIDDEN.value()
                        response.contentType = "application/json"
                        response.writer.write(
                            """{"error": "Access Denied: ${accessDeniedException.message}"}"""
                        )
                    }
            }
        return http.build()
    }
}