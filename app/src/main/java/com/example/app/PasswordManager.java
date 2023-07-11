package com.example.app;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


// Utilized online resources for guidance and reference!!!
public class PasswordManager {
    //constant variables for the AES encryption algorithm
    private  static final String AES_ALGORITHM = "AES";
    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int KEY_LENGTH_BITS = 256;


    private static SecretKey deriveKey(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Using the PBKDF2 algorithm to get a secret key
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        //Generating the secret key according to the "password" which is the authid with the salt
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        return skf.generateSecret(spec);
        //This function creates a special key for the current user accordingly by his userid. with the
    }


    // Generates a unique encryption key based on the authId
    public static String generateEncryptionKey(String authId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] authIdChars = authId.toCharArray();
        byte[] salt = generateSalt();
        SecretKey key = deriveKey(authIdChars, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        byte[] keyBytes = key.getEncoded();
        return Base64.encodeToString(keyBytes, Base64.DEFAULT);
    }



    // Returns the fixed salt value
    private static byte[] generateSalt() {
        // Generate a random salt() for PBKDF2
        return "randomsalt".getBytes(StandardCharsets.UTF_8);
    }
    public static String encryptPassword(String password, String encryptionKey) throws Exception {
        byte[] keyBytes = Base64.decode(encryptionKey, Base64.DEFAULT);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.URL_SAFE);
    }

    public static String decryptPassword(String encryptedPassword, String encryptionKey) throws Exception {
        byte[] keyBytes = Base64.decode(encryptionKey, Base64.DEFAULT);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] ciphertextBytes = Base64.decode(encryptedPassword, Base64.URL_SAFE);
        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


}
