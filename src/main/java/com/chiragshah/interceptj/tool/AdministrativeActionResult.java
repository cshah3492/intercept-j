package com.chiragshah.interceptj.tool;

import java.time.Instant;

public record AdministrativeActionResult(

        AdministrativeAction action,

        String targetResource,

        String status,

        Instant processedAt) {
}