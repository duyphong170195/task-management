package com.eight_seneca.task_management.util;

import java.util.Random;

public class UsernameGenerator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = LETTERS + LETTERS.toUpperCase() + DIGITS;
    private static final Random random = new Random();

    public static String generateRandomUsername(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be positive");

        StringBuilder username = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            username.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        return username.toString();
    }

}
