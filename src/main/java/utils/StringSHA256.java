package utils;

import java.security.MessageDigest;
import java.lang.StringBuilder;

public class StringSHA256 {

    public static String hashString(String input) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(input.getBytes("UTF-8"));

        // Converting byte array to Hexadecimal String
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        String output = sb.toString();

        return output;

  }

}