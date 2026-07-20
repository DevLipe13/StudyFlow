package com.studyflow.service;

import com.studyflow.api.dto.UserResponse;

public record BootstrapResult(boolean created, UserResponse user) {
}
