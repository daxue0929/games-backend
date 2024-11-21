package org.daxue.games.manager;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.daxue.games.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService {

    private static final String TOKEN_USER = "token_user_id:{0}";
    private static final Long expireSecond = 60 * 60L;

    public Boolean saveToken(String userId, String jwtToken, long expireSecond) {
        String key = MessageFormat.format(TOKEN_USER, userId);
        return redisUtil.set(key, jwtToken, expireSecond);
    }

    public String getToken(String userId) {
        String key = MessageFormat.format(TOKEN_USER, userId);
        return redisUtil.get(key, String.class);
    }

    public String createJwt(String subject, Map<String, Object> claims) throws JOSEException {
        JWSSigner signer = new RSASSASigner(privateKey);
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expireSecond * 1000));// 1小时有效期
        if (claims != null) {
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsSetBuilder.claim(entry.getKey(), entry.getValue());
            }
        }
        JWTClaimsSet  claimsSet = claimsSetBuilder.build();
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();

    }

    // 验证JWT（保持不变）
    public boolean verifyJwt(String jwt) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(jwt);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date expirationTime = claims.getExpirationTime();
        Date currentTime = new Date();
        if (expirationTime != null && currentTime.after(expirationTime)) {
            throw new JOSEException("JWT has expired");
        }
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        // 验证签名
        return signedJWT.verify(verifier);
    }

    // 获取JWT的Claims（保持不变）
    public JWTClaimsSet getClaims(String jwt) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(jwt);
        return signedJWT.getJWTClaimsSet();
    }

    private final RedisUtil redisUtil;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    void init() {
        try {
            this.privateKey = loadPrivateKey("rsa_private_key_pkcs8.pem");
            this.publicKey = loadPublicKey("rsa_public_key.pem");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public TokenService(RedisConnectionFactory redisConnectionFactory) {
        this.redisUtil = RedisUtil.initialize(redisConnectionFactory);
    }

    @Resource
    private ResourceLoader resourceLoader;

    // 从文件加载私钥
    private RSAPrivateKey loadPrivateKey(String filePath) throws Exception {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:/" + filePath);
        InputStream inputStream = resource.getInputStream();
        byte[] keyBytes = IOUtils.toByteArray(inputStream);
        String privateKeyContent = new String(keyBytes);
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    // 从文件加载公钥
    private RSAPublicKey loadPublicKey(String filePath) throws Exception {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:/" + filePath);
        InputStream inputStream = resource.getInputStream();
        byte[] keyBytes = IOUtils.toByteArray(inputStream);
        String publicKeyContent = new String(keyBytes);
        publicKeyContent = publicKeyContent.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodeKey = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodeKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
}
