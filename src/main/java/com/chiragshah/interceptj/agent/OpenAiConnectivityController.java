package com.chiragshah.interceptj.agent;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Profile({"ai-unprotected", "ai-protected"})
public class OpenAiConnectivityController {

    private final OpenAiConnectivityService connectivityService;

    public OpenAiConnectivityController(
            OpenAiConnectivityService connectivityService) {
        this.connectivityService = connectivityService;
    }

    @GetMapping("/connection-test")
    public Map<String, String> testConnection() {
        return Map.of(
                "status", "success",
                "response", connectivityService.testConnection());
    }
    
    @GetMapping("/calculator-test")
    public Map<String, String> testCalculatorTool() {

        return Map.of(
                "status", "success",
                "response",
                connectivityService.testCalculatorTool());
    }
    
    @GetMapping("/unauthorized-admin-test")
    public Map<String, String> testUnauthorizedAdmin() {

        return Map.of(
                "status", "success",
                "response",
                connectivityService
                    .testUnauthorizedAdministrativeTool());
    }
    
    @GetMapping("/authorized-admin-test")
    public Map<String, String> testAuthorizedAdmin() {

        return Map.of(
                "status", "success",
                "response",
                connectivityService
                    .testAuthorizedAdministrativeTool());
    }
    
    @GetMapping("/prompt-injection-test")
    public Map<String, String> testPromptInjection() {

        return Map.of(
                "status", "success",
                "response",
                connectivityService.testPromptInjection());
    }
    
    @GetMapping("/prompt-injection-runtime-test")
    public Map<String, String> testRuntimePromptInjection() {

        return Map.of(
                "status", "success",
                "response",
                connectivityService
                    .testRuntimePromptInjection());
    }
}