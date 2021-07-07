package com.tanhua.server.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * token工具类
 */
@Component
public class JwtUtils {

    @Value("${tanhua.secret}")
    private String secret;

    public static void main(String[] args) {
        // 创建token
        String secret = "itcast"; // 密钥

        // payload载荷，私有部分
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", "12345789");
        claims.put("id", "2");

        // 生成token
        String jwt = Jwts.builder()
                .setClaims(claims) //设置响应数据体
                .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
                .compact();

        System.out.println(jwt); //eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMjM0NTc4OSIsImlkIjoiMiJ9.VivsfLzrsKFOJo_BdGIf6cKY_7wr2jMOMOIGaFt_tps

        // 通过token解析数据
        jwt = "eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMjM0NTc4OSIsImlkIjoiMiJ9.VivsfLzrsKFOJo_BdGIf6cKY_7wr2jMOMOIGaFt_tps";
        Map<String, Object> body = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody();

        System.out.println(body); //{mobile=12345789, id=2}
        // 解析token
    }

    /**
     * 生成JWT
     *
     * @return
     */
    public String createJWT(String phone,Long userId) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", phone);
        claims.put("id", userId);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .signWith(SignatureAlgorithm.HS256, secret);
        return builder.compact();
    }
}