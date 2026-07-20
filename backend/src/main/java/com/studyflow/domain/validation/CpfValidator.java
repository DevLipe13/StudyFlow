package com.studyflow.domain.validation;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static String normalize(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        String digits = normalize(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(digits, 9) && checkDigit(digits, 10);
    }

    private static boolean checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (length + 1 - i);
        }
        int mod = (sum * 10) % 11;
        int expected = mod == 10 ? 0 : mod;
        return expected == Character.getNumericValue(digits.charAt(length));
    }
}
