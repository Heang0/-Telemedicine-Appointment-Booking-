package com.example.telemedicine.security;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    // For demo purposes, in production use Android Keystore
    private static SecretKey secretKey;
    
    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // 256-bit key
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            Log.e(TAG, "Error generating encryption key", e);
        }
    }
    
    // Encrypt data
    public static String encrypt(String data) {
        if (secretKey == null || data == null) return data;
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error", e);
            return data; // Return unencrypted data on failure (for demo)
        }
    }
    
    // Decrypt data
    public static String decrypt(String encryptedData) {
        if (secretKey == null || encryptedData == null) return encryptedData;
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            Log.e(TAG, "Decryption error", e);
            return encryptedData; // Return encrypted data on failure (for demo)
        }
    }
    
    // Generate secure random string for tokens
    public static String generateSecureToken(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < length; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return token.toString();
    }
}