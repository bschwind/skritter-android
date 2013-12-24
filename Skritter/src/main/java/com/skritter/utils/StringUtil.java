package com.skritter.utils;

public class StringUtil {
    public static String filterOutKana(String text) {
        return text.replaceAll("[\u3040-\u3096]+", "");
    }

    public static String replaceAllKanaWith(String text, String replacement) {
        return text.replaceAll("[\u3040-\u3096]+", replacement);
    }

    public static String filterOutNonKanji(String text) {
        return text.replaceAll("[^\u4e00-\u9faf]+", "");
    }

    public static String replaceAllKanjiWith(String text, String replacement) {
        return text.replaceAll("[\u4e00-\u9faf]+", replacement);
    }

    public static boolean isKana(char character) {
        int charCode = (int)character;
        return (charCode > 12352 && charCode < 12438) || (charCode > 12449 && charCode < 12538);
    }

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }
}
