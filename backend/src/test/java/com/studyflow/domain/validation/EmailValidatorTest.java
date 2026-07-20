package com.studyflow.domain.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void isValidAcceptsWellFormedEmail() {
        assertTrue(EmailValidator.isValid("student@example.com"));
        assertTrue(EmailValidator.isValid("  admin@studyflow.dev  "));
    }

    @Test
    void isValidRejectsMissingAt() {
        assertFalse(EmailValidator.isValid("invalid-email"));
    }

    @Test
    void isValidRejectsBlank() {
        assertFalse(EmailValidator.isValid(""));
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void isValidRejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }
}
