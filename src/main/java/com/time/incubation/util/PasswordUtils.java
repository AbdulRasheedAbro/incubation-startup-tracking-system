package com.time.incubation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * SHA-256 password hashing.
 *
 * NOTE: This is acceptable for a course project but not for production.
 * A real system should use BCrypt or Argon2, which are intentionally slow
 * and include a per-user salt.
 */
public final class PasswordUtils {

    private PasswordUtils() {}

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password.", e);
        }
    }

    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(plainPassword).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}