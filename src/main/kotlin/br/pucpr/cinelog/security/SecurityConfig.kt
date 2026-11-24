package br.pucpr.cinelog.security

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN
import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
@EnableMethodSecurity
@SecurityScheme(
    name = "AuthServer",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class SecurityConfig(
    private val jwtTokenFilter: JwtTokenFilter
) {
    @Bean
    fun filterChain(security: HttpSecurity): SecurityFilterChain =
        security.cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint { _, res, ex ->
                    res.sendError(
                        SC_UNAUTHORIZED,
                        if (ex.message.isNullOrBlank()) "UNAUTHORIZED" else ex.message
                    )
                }
                it.accessDeniedHandler { _, res, ex ->
                    res.sendError(
                        SC_FORBIDDEN,
                        if (ex.message.isNullOrBlank()) "FORBIDDEN" else ex.message
                    )
                }
            }
            .headers { it.frameOptions { fo -> fo.disable() } }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(HttpMethod.GET).permitAll()
                    .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/users/*/roles/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/roles").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/users/*").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/users/*/profile").authenticated()
                    .requestMatchers(HttpMethod.POST, "/movies", "/directors", "/genres").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/movies/*", "/directors/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/movies/*", "/directors/*", "/genres/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/movies/*", "/directors/*", "/genres/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/movies/*/genres/*", "/movies/*/director/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/movies/*/genres/*", "/movies/*/director").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/movies/*/reviews").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/reviews/*").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/reviews/*").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter::class.java)
            .build()

    @Bean
    fun corsFilter() =
        CorsConfiguration().apply {
            addAllowedHeader("*")
            addAllowedOrigin("*")
            addAllowedMethod("*")
        }.let {
            UrlBasedCorsConfigurationSource().apply {
                registerCorsConfiguration("/**", it)
            }
        }.let { CorsFilter(it) }
}
