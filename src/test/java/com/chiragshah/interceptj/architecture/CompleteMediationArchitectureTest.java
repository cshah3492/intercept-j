package com.chiragshah.interceptj.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.chiragshah.interceptj.tool.EnterpriseTool;
import com.chiragshah.interceptj.tool.ToolRegistry;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.chiragshah.interceptj")
class CompleteMediationArchitectureTest {

    @ArchTest
    static final ArchRule aiFacingCodeCannotAccessEnterpriseTools =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "..agent..",
                            "..experiment..",
                            "..config..",
                            "..controller..",
                            "..bridge..")
                    .should()
                    .dependOnClassesThat()
                    .areAssignableTo(EnterpriseTool.class)
                    .because(
                            "AI-facing code must submit requests through "
                                    + "ToolExecutionGateway");

    @ArchTest
    static final ArchRule aiFacingCodeCannotAccessToolRegistry =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "..agent..",
                            "..experiment..",
                            "..config..",
                            "..controller..",
                            "..bridge..")
                    .should()
                    .dependOnClassesThat()
                    .areAssignableTo(ToolRegistry.class)
                    .because(
                            "ToolRegistry dispatch is reserved for the "
                                    + "authorization and gateway layers");
}
