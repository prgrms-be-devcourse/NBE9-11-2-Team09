package com.example.parking.global.config;

import com.example.parking.global.security.JwtFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // [CUS-08] JWT 인증 - JwtFilter를 빈으로 주입하여 SecurityFilterChain에서 사용할 수 있도록 설정
    private final JwtFilter jwtFilter;

    //  CorsConfig의 빈을 주입받아 연결하기 위해 추가
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // refresh API는 access token 만료 후에도 호출할 수 있어야 하므로 permitAll로 둔다.
                        .requestMatchers("/api/users/signup", "/api/users/login", "/api/users/refresh","/api/users/check-email","/h2-console/**","/error").permitAll()
<<<<<<< HEAD
=======
                        .requestMatchers("/api/parking-spots/*/subscribe").permitAll()
>>>>>>> 760d6b681918df1da6f9dcc8315a5f0648f59235
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/reservations/**").permitAll()
                                // [CUS-05] 결제 - 고객만 결제 가능
                                .requestMatchers("/api/payments/**").hasRole("USER")
                                // [ADM-03, ADM-04, ADM-01] 관리자 결제 조회 및 환불 - 관리자만 접근 가능
                                .requestMatchers("/api/admin/payments/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    // [CUS-06] 회원가입 - 회원 비밀번호 암호화를 위해 BCrypt 인코더를 빈으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}