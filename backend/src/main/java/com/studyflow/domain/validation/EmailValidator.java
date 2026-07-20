package com.studyflow.domain.validation;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        return email != null && !email.isBlank() && EMAIL.matcher(email.trim()).matches();
    }
}
