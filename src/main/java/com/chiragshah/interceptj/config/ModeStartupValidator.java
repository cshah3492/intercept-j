package com.chiragshah.interceptj.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.model.ExperimentMode;

@Component
public class ModeStartupValidator implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ModeStartupValidator.class);

    private final InterceptJProperties properties;

    public ModeStartupValidator(InterceptJProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateConfiguration();

        LOGGER.info(
                "Intercept-J mode={}, aiEnabled={}, "
                        + "enforcementEnabled={}, auditEnabled={}",
                properties.getMode(),
                properties.getAi().isEnabled(),
                properties.getEnforcement().isEnabled(),
                properties.getAudit().isEnabled());
    }

    private void validateConfiguration() {
        ExperimentMode mode = properties.getMode();

        if (mode == null) {
            throw new IllegalStateException(
                    "The Intercept-J experimental mode must be configured.");
        }

        switch (mode) {
            case CONTROL -> validateControlMode();
            case AI_UNPROTECTED -> validateUnprotectedMode();
            case AI_PROTECTED -> validateProtectedMode();
        }
    }

    private void validateControlMode() {
        require(
                !properties.getAi().isEnabled(),
                "CONTROL mode cannot enable AI.");

        require(
                !properties.getEnforcement().isEnabled(),
                "CONTROL mode cannot enable Intercept-J enforcement.");

        require(
                !properties.getAudit().isEnabled(),
                "CONTROL mode cannot enable Intercept-J auditing.");
    }

    private void validateUnprotectedMode() {
        require(
                properties.getAi().isEnabled(),
                "AI_UNPROTECTED mode must enable AI.");

        require(
                !properties.getEnforcement().isEnabled(),
                "AI_UNPROTECTED mode cannot enable enforcement.");

        require(
                !properties.getAudit().isEnabled(),
                "AI_UNPROTECTED mode cannot enable Intercept-J auditing.");
    }

    private void validateProtectedMode() {
        require(
                properties.getAi().isEnabled(),
                "AI_PROTECTED mode must enable AI.");

        require(
                properties.getEnforcement().isEnabled(),
                "AI_PROTECTED mode must enable enforcement.");

        require(
                properties.getAudit().isEnabled(),
                "AI_PROTECTED mode must enable Intercept-J auditing.");
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}