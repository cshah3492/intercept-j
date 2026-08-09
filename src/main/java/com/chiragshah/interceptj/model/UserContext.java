package com.chiragshah.interceptj.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserContext(

        @NotBlank
        String userId,

        @NotEmpty
        Set<@NotBlank String> roles,

        @NotNull
        Map<@NotBlank String, @NotBlank String> attributes) {

    public UserContext {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(roles, "roles cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");

        userId = userId.trim();
        roles = Set.copyOf(roles);
        attributes = Map.copyOf(attributes);
    }
}