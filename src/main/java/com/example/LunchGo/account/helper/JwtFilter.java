package com.example.LunchGo.account.helper;

import com.example.LunchGo.common.util.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Request Header에서 토큰 꺼내기
        String jwt = resolveToken(request);

        //유효성 검사
        if(StringUtils.hasText(jwt) && tokenUtils.validateToken(jwt)) {
            Authentication auth = tokenUtils.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasLength(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
