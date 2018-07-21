package com.troya.simplechat.helpers;

import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    private static final String AES_SALT = "just_a_salt";

    public static byte[] getRandomCryptKey() {
        try {
            MessageDigest sha256Hash = MessageDigest.getInstance("SHA-256");
            sha256Hash.update(AES_SALT.getBytes());

            return sha256Hash.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static byte[] getRandomCryptIv() {
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);

        return new IvParameterSpec(randomBytes).getIV();
    }

    public static String encrypt(byte[] aesCryptKey, byte[] aesCryptIv, String plainText)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(aesCryptIv);
        SecretKeySpec newKey = new SecretKeySpec(aesCryptKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

        return Base64.encodeToString(cipher.doFinal(plainText.getBytes()), Base64.DEFAULT);
    }

    public static String decrypt(byte[] aesCryptKey, byte[] aesCryptIv, String cipherText)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(aesCryptIv);
        SecretKeySpec newKey = new SecretKeySpec(aesCryptKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);

        return new String(cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT)), "UTF-8");
    }
}
