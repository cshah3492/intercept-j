package com.chiragshah.interceptj.tool;

import java.util.Objects;

import com.chiragshah.interceptj.model.ToolArguments;

import jakarta.validation.constraints.NotBlank;

public record CustomerLookupArguments(

        @NotBlank
        String customerId,

        @NotBlank
        String requestedRegion) implements ToolArguments {

    public CustomerLookupArguments {
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(
                requestedRegion,
                "requestedRegion cannot be null");

        customerId = customerId.trim().toUpperCase();
        requestedRegion = requestedRegion.trim().toUpperCase();
    }
}