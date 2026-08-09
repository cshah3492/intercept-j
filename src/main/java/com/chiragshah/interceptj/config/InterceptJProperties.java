package com.chiragshah.interceptj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.model.ExperimentMode;

@Component
@ConfigurationProperties(prefix = "interceptj")
public class InterceptJProperties {

    private ExperimentMode mode;
    private Feature ai = new Feature();
    private Feature enforcement = new Feature();
    private Feature audit = new Feature();

    public ExperimentMode getMode() {
        return mode;
    }

    public void setMode(ExperimentMode mode) {
        this.mode = mode;
    }

    public Feature getAi() {
        return ai;
    }

    public void setAi(Feature ai) {
        this.ai = ai;
    }

    public Feature getEnforcement() {
        return enforcement;
    }

    public void setEnforcement(Feature enforcement) {
        this.enforcement = enforcement;
    }

    public Feature getAudit() {
        return audit;
    }

    public void setAudit(Feature audit) {
        this.audit = audit;
    }

    public static class Feature {

        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}