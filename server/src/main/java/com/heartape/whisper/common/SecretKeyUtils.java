package com.heartape.whisper.common;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 公私钥文件读取
 */
@Slf4j
public class SecretKeyUtils {

    /**
     * <pre>
     *     KeyPair keyPair = generateRsaKey();
     *     RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
     *     RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
     * </pre>
     * @return An instance of java.security.KeyPair with keys generated on startup used to create the JWKSource above.
     */
    public static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    /**
     * 获取私匙
     * @return RSAPrivateKey 私钥
     */
    @SneakyThrows
    public static RSAPrivateKey getPrivateKeyFromPem(String privateKeyPem) {
        byte[] bytes = readKeyFile(privateKeyPem);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        return (RSAPrivateKey)kf.generatePrivate(keySpec);
    }

    /**
     * 获取公钥
     * @return RSAPublicKey 公钥
     */
    @SneakyThrows
    public static RSAPublicKey getPublicKeyFromPem(String publicKeyPem) {
        byte[] bytes = readKeyFile(publicKeyPem);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        return (RSAPublicKey)kf.generatePublic(keySpec);
    }

    @SuppressWarnings("UnusedAssignment")
    private static byte[] readKeyFile(String filename){
        String dir = System.getProperty("user.dir");
        String path = dir + filename;
        StringBuilder stringBuilder = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String s = br.readLine();
            while ((s = br.readLine()).charAt(0) != '-') {
                stringBuilder.append(s);
            }
        } catch (FileNotFoundException fileNotFoundException){
            log.error("RSA key file not found");
        } catch (IOException ioException){
            log.error("read RSA key file failure");
        }
        return Base64.getDecoder().decode(stringBuilder.toString());
    }
}
