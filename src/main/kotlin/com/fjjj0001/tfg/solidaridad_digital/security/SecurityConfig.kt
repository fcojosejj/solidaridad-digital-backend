package com.fjjj0001.tfg.solidaridad_digital.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class SecurityConfig(private val userDetailsService: UserDetailsService) {
    @Autowired
    private lateinit var jwtUtil: JWTUtil

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        val authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder())
        val authenticationManager = authenticationManagerBuilder.build()
        httpSecurity
            .authorizeHttpRequests {
                it
                    .requestMatchers(antMatcher(HttpMethod.POST,"/user/*")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.GET,"/user/*")).authenticated()
                    .requestMatchers(antMatcher("/helpPublications/**")).authenticated()
                    .requestMatchers(antMatcher("/helpRequests/comment/**")).authenticated()
                    .requestMatchers(antMatcher("/helpRequests/rating/**")).authenticated()
                    .anyRequest().authenticated()
            }
            .authenticationManager(authenticationManager)
            .csrf { it.disable() }
            .cors { corsCustomizer ->
                corsCustomizer.configurationSource { request ->
                    CorsConfiguration().apply {
                        allowedOrigins = listOf("http://localhost:3000")
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                        exposedHeaders = listOf("Set-Cookie")
                    }
                }
            }
            .httpBasic{ it.disable() }
            .formLogin{ it.disable() }
            .addFilterBefore(JWTAuthFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter::class.java)

        return httpSecurity.build()
    }
}