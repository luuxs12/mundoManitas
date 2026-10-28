package com.mundomanitas.config;

import com.mundomanitas.security.ApiAwareAccessDeniedHandler;
import com.mundomanitas.security.ApiAwareAuthenticationEntryPoint;
import com.mundomanitas.security.CustomUserDetailsService;
import com.mundomanitas.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${app.security.remember-me-key:mundo-manitas-remember-key}")
    private String rememberMeKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        CustomUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        AuthenticationManager authenticationManager,
        CustomUserDetailsService userDetailsService,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        ApiAwareAuthenticationEntryPoint authenticationEntryPoint,
        ApiAwareAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .authenticationManager(authenticationManager)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/registro/**",
                    "/recuperar",
                    "/restablecer",
                    "/error",
                    "/error/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/modulos/**", "/api/admin/videos/**", "/api/admin/ejercicios/**")
                    .hasAnyRole("DOCENTE", "ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/api/reportes/**").hasAnyRole("DOCENTE", "ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/api/certificados/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/admin/contenido/**", "/admin/modulos/**", "/admin/videos/**", "/admin/ejercicios/**")
                    .hasAnyRole("DOCENTE", "ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/admin").hasAnyRole("ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/admin/**").hasAnyRole("ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/docente/**").hasAuthority("MODULO_DOCENTE_CURSOS")
                .requestMatchers("/reportes/progreso.xlsx").hasAnyRole("DOCENTE", "ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/reportes/certificado/**").authenticated()
                .requestMatchers("/reportes/**").hasAnyRole("DOCENTE", "ADMINISTRADOR", "SISTEMAS")
                .requestMatchers("/dashboard").hasAuthority("MODULO_DASHBOARD")
                .requestMatchers("/cursos/**").hasAuthority("MODULO_CURSOS")
                .requestMatchers("/perfil").hasAuthority("MODULO_PERFIL")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(excepciones -> excepciones
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key(rememberMeKey)
                .tokenValiditySeconds(604800)
                .userDetailsService(userDetailsService)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
