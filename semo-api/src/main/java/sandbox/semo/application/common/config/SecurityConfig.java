package sandbox.semo.application.common.config;

import static sandbox.semo.application.security.constant.SecurityConstants.API_LOGOUT_PATH;
import static sandbox.semo.application.security.constant.SecurityConstants.PUBLIC_PATHS;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import sandbox.semo.application.security.authentication.JwtAuthenticationFilter;
import sandbox.semo.application.security.authentication.JwtLogoutHandler;
import sandbox.semo.application.security.authentication.JwtLogoutSuccessHandler;
import sandbox.semo.application.security.authentication.LoginFilter;
import sandbox.semo.application.security.authentication.MemberAuthProvider;
import sandbox.semo.application.security.exception.MemberAuthExceptionEntryPoint;
import sandbox.semo.application.security.util.JwtUtil;
import sandbox.semo.application.security.util.RedisUtil;

@Log4j2
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberAuthProvider memberAuthProvider;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final MemberAuthExceptionEntryPoint authenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RedisUtil redisUtil;
    private final JwtLogoutHandler jwtLogoutHandler;
    private final JwtLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(memberAuthProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS.toArray(String[]::new)).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, redisUtil),
                LoginFilter.class)
            .addFilterAt(
                new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
                UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ).logout(logout -> logout
                .logoutUrl(API_LOGOUT_PATH)
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
        return configuration.getAuthenticationManager();
    }

}

