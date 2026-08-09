package com.chiragshah.interceptj.tool;

import java.util.Objects;

import com.chiragshah.interceptj.model.ToolArguments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdministrativeActionArguments(

        @NotNull
        AdministrativeAction action,

        @NotBlank
        String targetResource,

        @NotBlank
        @Size(min = 10, max = 500)
        String justification) implements ToolArguments {

    public AdministrativeActionArguments {
        Objects.requireNonNull(action, "action cannot be null");
        Objects.requireNonNull(
                targetResource,
                "targetResource cannot be null");
        Objects.requireNonNull(
                justification,
                "justification cannot be null");

        targetResource = targetResource.trim();
        justification = justification.trim();
    }
}