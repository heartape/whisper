package com.heartape.whisper.common;

import com.heartape.whisper.exception.SystemException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JwtUtils {

    private static volatile boolean initialized = false;

    private static Key KEY;
    private static long EXPIRE;
    private static String ISSUER;
    private static String AUDIENCE;

    private JwtUtils() {}

    /**
     * 只能在应用启动时调用一次
     */
    public static void init(String base64Secret, long expire, String issuer, String audience) {

        if (initialized) {
            return;
        }

        synchronized (JwtUtils.class) {
            if (initialized) {
                return;
            }

            byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
            KEY = Keys.hmacShaKeyFor(keyBytes);

            EXPIRE = expire;
            ISSUER = issuer;
            AUDIENCE = audience;

            initialized = true;
        }
    }

    private static void checkInit() {
        if (!initialized) {
            throw new SystemException("JwtUtils 未初始化，请在应用启动阶段调用 JwtUtils.init()");
        }
    }

    private static String generateSecret() {
        // 生成符合 HS256 安全要求的密钥
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥转换为 Base64 字符串打印出来
        return Encoders.BASE64.encode(key.getEncoded());
    }

    /**
     * <table border="1">
     * <caption>jwt信息表</caption>
     * <tr>
     * <th>缩写</th>
     * <th>全称</th>
     * <th>意义与用途</th>
     * </tr>
     * <tr>
     * <td>sub</td>
     * <td>Subject</td>
     * <td>该 JWT 所面向的用户/主题。通常存放 用户 ID。在 setUser() 时，建议将此值作为 Principal 的 getName() 返回值。</td>
     * </tr>
     * <tr>
     * <td>jti</td>
     * <td>JWT ID</td>
     * <td>Token 的唯一标识。用于防止重放攻击。通过给每个 Token 分配唯一 ID，服务端可以拉黑特定的某个 Token，而不是注销用户的所有 Token。</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>iat</td>
     * <td>Issued At</td>
     * <td>签发时间。记录 Token 是什么时候生成的（Unix 时间戳）。</td>
     * </tr>
     * <tr>
     * <td>exp</td>
     * <td>Expiration</td>
     * <td>过期时间。必须大于 iat。一旦超过这个时间，Token 就会失效。</td>
     * </tr>
     * <tr>
     * <td>aud</td>
     * <td>Audience</td>
     * <td>受众。标识这个 Token 是发给哪个终端或服务的（如：web, ios, admin-backend）。</td>
     * </tr>
     * <tr>
     * <td>iss</td>
     * <td>Issuer</td>
     * <td>签发者。标识谁生成的这个 Token（如：auth-server.example.com）。</td>
     * </tr>
     * </table>
     * * @param id 用户ID
     */
    public static String generate(String id) {
        checkInit();

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(id)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .setAudience(AUDIENCE)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String generate(String id, Map<String, Object> info) {
        checkInit();

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(id)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .claim("info", info)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token) {
        checkInit();

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static void check(String token) {
        checkInit();

        Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
    }

}
