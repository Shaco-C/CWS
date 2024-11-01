package com.watergun.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // Token 有效期，24小时

    // 生成 JWT
    public  String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // 使用邮箱作为主题
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 从 JWT 中获取用户名
    public  String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 从 JWT 中获取角色
    public String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }
    // 从 JWT 中获取Id
    public Long extractUserId(String token) {
        return ((Number) extractClaims(token).get("userId")).longValue();
    }
    // 验证 Token 是否过期
    public  boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // 解析 Token，获取声明
    private  Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证userId是否为空
     * @param token 用户token
     * @return userId 用户id
     * @author CJ
     */
    public Long getUserIdFromToken(String token) {
        Long userId = this.extractUserId(token);
        log.info("userId: {}", userId);
        if (userId == null) {
            log.warn("用户未登录");
            throw new IllegalStateException("user not login");
        }
        return userId;
    }

    /**
     * 获取用户角色
     * @param token 用户token
     * @return userRole 用户角色
     * @author CJ
     */
    public String getUserRoleFromToken(String token) {
        String userRole = this.extractRole(token);
        log.info("userId: {}", userRole);
        if (userRole == null) {
            log.warn("用户未登录");
            throw new IllegalStateException("user not login");
        }
        return userRole;
    }

}
