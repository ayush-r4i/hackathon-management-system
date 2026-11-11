package com.collegefest.management.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private static final String SALT = "HackArena2024";

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = SALT + rawPassword;
            byte[] encoded = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String rawPassword, String hash) {
        if (rawPassword == null || hash == null) {
            return false;
        }
        return hash(rawPassword).equals(hash);
    }
}
