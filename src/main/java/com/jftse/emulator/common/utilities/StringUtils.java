package com.jftse.emulator.common.utilities;

import org.apache.commons.lang.RandomStringUtils;

public class StringUtils {

    public static String firstCharToUpperCase(String string) {
        if (isEmpty(string)) {
            return "";
        }

        String firstLetter = string.substring(0, 1).toUpperCase();

        return firstLetter + string.substring(1);
    }

    public static String firstCharToLowerCase(String string) {
        if (isEmpty(string)) {
            return "";
        }

        String firstLetter = string.substring(0, 1).toLowerCase();

        return firstLetter + string.substring(1);
    }

    public static boolean isEmpty(String string) {
        if (string == null || string.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String randomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String pwd = RandomStringUtils.random(length, characters);
        return pwd;
    }
}
