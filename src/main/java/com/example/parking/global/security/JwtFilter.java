package com.example.parking.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // [CUS-08] JWT 인증 - 요청 헤더의 Bearer 토큰을 검증하는 필터
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        try {
            if (token != null) {
                jwtUtil.validateToken(token);

                // refresh token은 인증에 사용하지 않고 access token만 인증에 사용
                if ("access".equals(jwtUtil.getTokenType(token))) {
                    Long userId = jwtUtil.getUserId(token);
                    String userEmail = jwtUtil.getUserEmail(token);
                    String role = jwtUtil.getRole(token);

                    CustomUserDetails userDetails = new CustomUserDetails(userId, userEmail, role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "401-1",
                    "Access token이 만료되었습니다."
            );
        } catch (JwtException | IllegalArgumentException e) {
            writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "401-2",
                    "유효하지 않은 토큰입니다."
            );
        }
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. 헤더에서 토큰 확인 (일반 API 요청 처리)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 쿼리 파라미터에서 토큰 확인 (SSE 지원을 위해 추가)
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }

        return null;
    }

    // 토큰이 유효하지 않거나 만료된 경우 401 응답을 JSON 형식으로 반환
    private void writeErrorResponse(HttpServletResponse response,
                                    int status,
                                    String resultCode,
                                    String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String body = String.format("""
                {
                  "msg": "%s",
                  "resultCode": "%s",
                  "data": null
                }
                """, message, resultCode);

        response.getWriter().write(body);
    }
}
