package com.studyflow.domain.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    @Test
    void normalizeStripsNonDigits() {
        assertEquals("52998224725", CpfValidator.normalize("529.982.247-25"));
    }

    @Test
    void isValidAcceptsKnownValidCpf() {
        assertTrue(CpfValidator.isValid("529.982.247-25"));
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void isValidRejectsInvalidLength() {
        assertFalse(CpfValidator.isValid("1234567890"));
        assertFalse(CpfValidator.isValid("123456789012"));
    }

    @Test
    void isValidRejectsRepeatedDigits() {
        assertFalse(CpfValidator.isValid("11111111111"));
    }

    @Test
    void isValidRejectsInvalidCheckDigits() {
        assertFalse(CpfValidator.isValid("52998224726"));
    }

    @Test
    void isValidRejectsNull() {
        assertFalse(CpfValidator.isValid(null));
    }
}
