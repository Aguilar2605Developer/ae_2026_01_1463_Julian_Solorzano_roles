package com.pucetec.exam2.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->

                auth.requestMatchers("/api/estacionamiento/disponibles").permitAll()

                auth.anyRequest().authenticated()
            }

            .oauth2ResourceServer { oauth2 -> oauth2.jwt(Customizer.withDefaults()) }

        return http.build()
    }
}