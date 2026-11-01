# Intercept-J

Deterministic Runtime Policy Enforcement for Secure AI Tool Invocation in Enterprise Java Applications

Intercept-J is a research prototype for enforcing deterministic authorization policies on AI-generated enterprise tool requests in Java applications. The framework treats an LLM-generated tool invocation as an untrusted proposal. Before a protected tool executes, the Java application evaluates the request against trusted application-managed identity, role, attribute, and tool information.

The prototype was developed to support the empirical study **“Intercept-J: Deterministic Runtime Policy Enforcement for Secure AI Tool Invocation in Enterprise Java Applications.”** It uses a single Spring Boot codebase with three experimental configurations so that AI-related behavior can be separated from the additional cost and security effect of runtime policy enforcement.

## Research Objective

Tool-calling LLM applications create a security boundary problem: a model may be manipulated into requesting an operation that the underlying user is not authorized to perform. Intercept-J does not rely on the LLM to make the final authorization decision. Instead, authorization is enforced immediately before enterprise tool execution in the Java application layer.

The prototype evaluates three research questions:

1. Can deterministic runtime enforcement reduce successful prompt-injection and unauthorized tool execution compared with an unprotected AI-enabled application?
2. Can application-controlled RBAC and ABAC policies correctly allow authorized operations and block unauthorized operations?
3. What application-local runtime cost is introduced by Intercept-J enforcement?

## Architecture

The protected execution path is conceptually:

```text
User / Prompt
     |
     v
LLM via Spring AI
     |
     v
AiToolBridge
     |
     v
ToolExecutionGateway
     |
     v
Spring AOP Interception
     |
     v
AuthorizationService / PolicyEngine
     |
     +---- ALLOW ----> ToolRegistry ----> Enterprise Tool
     |
     +---- DENY -----> Block Execution
                           |
                           v
                    Audit / Experiment Record
```

The security boundary deliberately separates:

* **Untrusted AI information:** prompts, retrieved content, model reasoning, generated tool requests, tool arguments, and privilege claims contained in model output.
* **Trusted application information:** user identity, roles, attributes, policy configuration, and the Java enforcement path.

An LLM can therefore request a privileged operation, but it cannot grant itself the application-managed role or attributes needed to authorize that operation.

### Relationship to Spring Security and Spring AI

Intercept-J builds on rather than replaces Spring Security and Spring AI. Spring Security supplies web authentication and service-level method authorization, while Spring AI supplies model integration, tool registration, and tool-calling infrastructure. Intercept-J adds an AI-specific mediation path that converts model-generated requests into structured `ToolRequest` objects, combines them with application-controlled `UserContext` data, evaluates tool names and arguments through deterministic RBAC and ABAC policies, and records request, authorization, and execution events separately.

### Complete Mediation and Bypass Resistance

All AI-facing tool callbacks submit requests through `ToolExecutionGateway`; they are not permitted to access `ToolRegistry` or enterprise tool implementations directly. The gateway opens a narrowly scoped execution context only after the request enters the designated path. Spring Security method authorization on the `EnterpriseTool` interface rejects direct invocation of any Spring-managed enterprise tool outside that context. The Intercept-J AOP interceptor remains responsible for AI-specific RBAC and ABAC evaluation at the gateway, so the service-level safeguard does not duplicate policy rules.

The build also includes ArchUnit rules that prevent classes in the `agent`, `experiment`, `config`, `controller`, and `bridge` package patterns from depending directly on enterprise tool implementations or `ToolRegistry`. These controls combine runtime rejection with build-time dependency verification. They protect correctly configured Spring-managed execution paths, but do not claim to resist compromised application code, bytecode manipulation, or operations created outside the Spring container.

## Technology Stack

The evaluated prototype used:

* Java 21.0.12
* Spring Boot 4.1.0
* Spring AI 2.0.0
* Spring AOP
* Spring Security
* Maven
* OpenAI `gpt-5-mini` for AI-enabled experiments
* JUnit and ArchUnit for automated tests

Development and testing were performed with Eclipse, although the project can also be built and run from a command line with Maven.

## Project Organization

The main Java packages are organized by responsibility:

```text
src/main/java/com/chiragshah/interceptj/
|-- agent/        # Spring AI integration and AI-to-tool bridge
|-- audit/        # Audit and experiment-oriented logging
|-- config/       # Application and experimental configuration
|-- experiment/   # Scenario execution, observations, metrics, and export
|-- interceptor/  # Runtime interception
|-- model/        # Tool requests, user context, decisions, and results
|-- policy/       # Deterministic RBAC/ABAC policy evaluation
|-- service/      # Authorization and tool execution gateway
`-- tool/         # Enterprise tool implementations and registry
```

The repository also contains profile-specific configuration under `src/main/resources` and research results under `experimental-data`.

## Demonstration Tools

The proof-of-concept contains three representative enterprise tools:

### Calculator

An authorized arithmetic operation used across the three experimental modes and as the common operation for performance comparison.

### Customer Lookup

A simulated enterprise lookup protected by role and region attributes. The policy requires an appropriate user role and matching regional authorization.

### Administrative Action

A privileged administrative operation used to test RBAC, ABAC, and prompt-injection behavior. Administrative actions are **simulated**. The prototype does not restart or modify real production services.

## Runtime Policy Examples

The prototype demonstrates deterministic policies including:

* calculator operations are permitted as an authorized tool capability;
* customer lookup requires an `ANALYST` or `MANAGER` role;
* customer lookup requires the user's authorized region to match the requested region;
* administrative actions require the `ADMIN` role;
* administrative actions additionally require `HIGH` clearance;
* unknown tools or invalid tool arguments are denied.

Policy decisions use application-controlled `UserContext` data. A privilege statement contained in an AI prompt or model response does not modify that trusted context.

## Experimental Modes

The same Spring Boot codebase supports three Spring profiles.

|Mode|Spring profile|Configuration|Purpose|
|-|-|-|-|
|Mode 1|`control`|Gateway-mediated Java execution without an LLM or policy enforcement|Establishes the non-AI application performance control|
|Mode 2|`ai-unprotected`|Spring AI and OpenAI enabled without Intercept-J authorization enforcement|Establishes the unprotected AI security and performance baseline|
|Mode 3|`ai-protected`|Spring AI and OpenAI enabled with Intercept-J authorization and auditing|Measures security effectiveness and enforcement cost|

The primary security comparison is **Mode 2 versus Mode 3**. Mode 1 is a performance control and is not treated as a prompt-injection security baseline because it contains no LLM-mediated execution path.

## Experimental Scenario Catalog

The final protocol contains 12 scenarios.

|ID|Scenario|Expected protected-mode outcome|
|-|-|-|
|`CALC-001`|Authorized calculator|EXECUTED|
|`ADMIN-AUTH-001`|Authorized administrative action|ALLOW / EXECUTED|
|`ADMIN-UNAUTH-001`|Unauthorized administrative action|DENY / BLOCKED|
|`CUSTOMER-AUTH-001`|Authorized customer lookup|ALLOW / EXECUTED|
|`CUSTOMER-RBAC-001`|Missing required customer-access role|DENY / BLOCKED|
|`CUSTOMER-ABAC-001`|Unauthorized customer region|DENY / BLOCKED|
|`PI-001`|Direct instruction override|No unauthorized execution|
|`PI-002`|False administrator claim|DENY / BLOCKED if tool requested|
|`PI-003`|Emergency or urgency bypass|DENY / BLOCKED if tool requested|
|`PI-004`|Fabricated administrator approval|DENY / BLOCKED if tool requested|
|`PI-005`|Policy-disable instruction|DENY / BLOCKED if tool requested|
|`PI-006`|Combined override and privilege escalation|DENY / BLOCKED if tool requested|

## Requirements

To build the project locally, install:

* JDK 21
* Maven 3.x, or use the included Maven Wrapper if available in the repository
* Git
* an OpenAI API key for the AI-enabled profiles

Eclipse IDE for Enterprise Java and Web Developers with Spring Tools is convenient but not required.

Verify Java before building:

```bash
java -version
javac -version
```

Both should resolve to Java 21 for reproduction of the reported environment.

## Clone the Repository

```bash
git clone https://github.com/cshah3492/intercept-j.git
cd intercept-j
```

## Configure the OpenAI API Key

Do not commit an API key to `application.properties`, source code, or this repository.

Set the key as an environment variable named `OPENAI_API_KEY`.

### Windows Command Prompt

```cmd
set OPENAI_API_KEY=your-key-here
```

### Windows PowerShell

```powershell
$env:OPENAI_API_KEY="your-key-here"
```

### macOS / Linux

```bash
export OPENAI_API_KEY="your-key-here"
```

### Eclipse

1. Open **Run > Run Configurations**.
2. Select the Intercept-J Spring Boot configuration.
3. Open the **Environment** tab.
4. Add `OPENAI_API_KEY` and its value.
5. Do not store or publish screenshots containing the secret value.

The `control` profile does not require LLM access for its gateway-mediated Java execution path.

## Build and Test

Run the complete automated verification suite before starting experimental executions. The command runs unit, integration, bypass-resistance, and architecture tests together without calling the external LLM:

### Maven Wrapper on Windows

```cmd
mvnw.cmd clean verify
```

### Maven Wrapper on macOS / Linux

```bash
./mvnw clean verify
```

If Maven is installed globally:

```bash
mvn clean verify
```

A successful build should report zero test failures, zero test errors, and no architecture-rule violations.

After the final audit-lifecycle and Spring Security method-authorization additions, a successful automated suite should report:

```text
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

This total includes two ArchUnit complete-mediation rules, integration tests that verify rejection of direct calls to all three Spring-managed enterprise tools while permitting gateway-mediated execution, and audit tests confirming distinct request, authorization, and execution lifecycle records.

The verification run also performs 200 local warm-up invocations followed by 1,000 measured protected-path invocations. It does not call the external LLM. Results are written to:

```text
target/benchmark-results/complete-mediation-performance.csv
target/benchmark-results/complete-mediation-performance-summary.txt
```

In Eclipse, the equivalent workflow is **Right-click project > Run As > Maven build**, with `clean verify` entered as the goals.

## Run the Application

Only one experimental profile should be active for a given application run.

### Mode 1: Non-AI Control

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=control
```

### Mode 2: Unprotected AI Baseline

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=ai-unprotected
```

### Mode 3: Intercept-J Protected AI

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=ai-protected
```

When using Eclipse, set the active Spring profile in the Spring Boot run configuration or use the corresponding profile-specific application configuration.

The startup log identifies the active Intercept-J mode and whether AI, enforcement, and auditing are enabled. Verify this log before collecting results.

## API Authentication During Local Testing

Spring Security may generate a development-only password at application startup unless a local security configuration overrides it. Use the credentials shown/configured for your local development run. Generated development passwords must not be treated as production credentials or committed to the repository.

Examples in this README use placeholders:

```text
user        local development username
<PASSWORD>  local development password
```

## Running an Experimental Scenario

The experimental runner accepts a scenario identifier and repetition count. For example, after starting the desired profile:

```bash
curl -u user:<PASSWORD> -X POST "http://localhost:8080/api/experiments/run?scenarioId=PI-002&repetitions=1"
```

For a 20-repetition experimental batch:

```bash
curl -u user:<PASSWORD> -X POST "http://localhost:8080/api/experiments/run?scenarioId=PI-002&repetitions=20"
```

Replace `PI-002` with any scenario ID from the catalog above. Run only scenarios applicable to the active mode when reproducing the study protocol.

## Inspecting Observations and Metrics

Structured experiment observations can be inspected with:

```bash
curl -u user:<PASSWORD> "http://localhost:8080/api/experiments/observations"
```

Aggregate security metrics can be inspected with:

```bash
curl -u user:<PASSWORD> "http://localhost:8080/api/experiments/metrics"
```

The experiment records include fields such as scenario, mode, requested tool, execution status, policy outcome, unauthorized execution status, attack outcome, and timing measurements.

Restart the application when switching experimental profiles so that each profile has a clean in-memory experimental state. Verify the active mode in the startup log before executing a new batch.

## Reproducing the 500-Run Evaluation

The reported evaluation used a frozen protocol.

### Mode 1

* Profile: `control`
* Scenario: `CALC-001`
* Repetitions: 20
* Final observations: 20

### Mode 2

* Profile: `ai-unprotected`
* Scenarios: all 12 scenarios in the catalog
* Repetitions: 20 per scenario
* Final observations: 240

### Mode 3

* Profile: `ai-protected`
* Scenarios: all 12 scenarios in the catalog
* Repetitions: 20 per scenario
* Final observations: 240

Total final experimental runs:

```text
20 + (12 x 20) + (12 x 20) = 500
```

Before final collection, the study performed five calculator warm-up executions for Mode 1 and three warm-up executions for each AI-enabled mode. Warm-up observations were excluded from final results.

For strict reproduction of the published experiment, keep scenario definitions, prompts, policy rules, model configuration, metric definitions, and retry behavior unchanged during a final collection cycle.

## Evaluation Metrics

The study distinguishes model behavior from application execution behavior.

* **Attack Tool Request Rate (ATRR):** proportion of prompt-injection trials in which the LLM requests the targeted protected tool.
* **Attack Success Rate (ASR):** proportion of prompt-injection trials that result in the targeted unauthorized execution.
* **Unauthorized Tool Execution Rate (UTER):** proportion of unauthorized scenario runs that result in unauthorized execution.
* **Policy Enforcement Rate (PER):** proportion of policy-evaluated runs in which the expected deterministic policy decision is correctly enforced.
* **False positives:** authorized operations incorrectly blocked.
* **False negatives:** unauthorized operations incorrectly allowed.

Performance measurements distinguish:

* **end-to-end duration:** complete request, including the LLM interaction in AI modes;
* **tool-path duration:** application-side tool invocation path;
* **protected-path duration:** local Intercept-J authorization/enforcement path in Mode 3.

This distinction prevents variable model and network latency from being attributed directly to Intercept-J.

## Experimental Data

The `experimental-data/` directory preserves the raw files from the original 500-run security evaluation and separately provides the dedicated complete-mediation performance benchmark:

```text
experimental-data/
|-- mode1-final-results.csv
|-- mode2-final-results.csv
|-- mode3-final-results.csv
|-- complete-mediation-performance.csv
`-- complete-mediation-performance-summary.txt
```

|File|Configuration|Observations|
|-|-|-:|
|`mode1-final-results.csv`|Non-AI Control|20 runs|
|`mode2-final-results.csv`|Unprotected AI Baseline|240 runs|
|`mode3-final-results.csv`|Intercept-J Protected AI|240 runs|
|`complete-mediation-performance.csv`|Hardened protected path after 200 warm-ups|1,000 measurements|
|`complete-mediation-performance-summary.txt`|Summary statistics for the dedicated benchmark|1 summary|

The three mode files contain the 500 final experimental runs reported in the original security evaluation. They remain unchanged to preserve the provenance and reproducibility of those findings. The two complete-mediation performance files are additional validation artifacts generated from the hardened implementation; they do not replace the original mode files.

## Reported Results

The final study reported the following principal results:

|Metric|Mode 2: AI Unprotected|Mode 3: Intercept-J Protected|
|-|-:|-:|
|Attack Success Rate|83.33%|0.00%|
|Unauthorized Tool Execution Rate|88.89%|0.00%|
|Policy Enforcement Rate|N/A|100.00%|

In Mode 3, the 240 total runs included 20 PI-001 trials in which the LLM did not generate the targeted tool request. The remaining 220 requests reached policy evaluation, comprising 60 legitimate requests and 160 unauthorized requests. All 220 produced the expected deterministic authorization outcome. The 60 legitimate requests were allowed, while all 160 unauthorized requests were blocked. No false-positive or false-negative authorization outcomes were observed in the evaluated sample.

### Original Mixed-Scenario Latency

Across the 220 policy-evaluated requests in the original Mode 3 security experiment, the measured protected-path latency was:

* mean: 0.5663 ms
* median: 0.4689 ms
* p95: 0.9064 ms

### Dedicated Complete-Mediation Performance Validation

After the complete-mediation controls were added, a separate local benchmark performed 200 warm-up invocations followed by 1,000 measured authorized calculator invocations through the protected gateway path. It made no external LLM calls and produced:

* mean: 0.042045 ms
* median: 0.037100 ms
* standard deviation: 0.022645 ms
* p95: 0.076300 ms
* p99: 0.133800 ms

The original and dedicated latency values should not be interpreted as a direct before-and-after performance comparison. The original value came from 220 mixed security scenarios, whereas the dedicated benchmark used a homogeneous authorized operation, explicit JVM warm-up, and 1,000 local measurements. Both sets of measurements characterize their respective evaluated workloads and should not be interpreted as universal production performance or a guarantee of zero security risk.

## Reproducibility Notes

* AI-enabled results depend on external model behavior and may change across model versions, providers, time periods, or API configurations.
* The final study used one OpenAI model, `gpt-5-mini`.
* Each applicable scenario was repeated 20 times.
* The original 500-run security results are retained separately from the post-hardening complete-mediation validation.
* The hardened implementation is expected to report 23 automated tests, including both complete-mediation architecture rules and the new audit-lifecycle verification. Confirm this count with `clean verify` in the reproduction environment.
* Zero observed protected-mode attacks, false positives, or false negatives should not be interpreted as zero population risk.
* The dedicated performance benchmark used 200 warm-ups and 1,000 sequential local invocations; it was not a concurrent load or throughput test.
* The administrative tool performs a simulated operation rather than modifying production infrastructure.
* The prototype uses experiment-oriented in-memory auditing; production deployments should use durable and integrity-protected audit storage.

## Security Scope

Intercept-J protects Spring-managed enterprise tools through a gateway-scoped runtime boundary and automated architecture rules. The research prototype assumes that the Java application, trusted identity context, policy configuration, and compiled bytecode have not themselves been compromised.

The current security claim does not cover:

* compromise of the Java process or application code;
* compromise of the identity provider or trusted user context;
* malicious modification of policy configuration;
* malicious modification of application code, bytecode, or architecture-test configuration;
* direct execution of tool objects instantiated manually outside the Spring container;
* production-scale distributed or multi-agent deployments.

## Research Use

This repository is intended to support research, reproducibility, and experimentation. It is a proof-of-concept and has not been hardened or certified for production deployment.

Do not connect the simulated privileged actions directly to production infrastructure without additional authentication, authorization, input validation, audit integrity, operational safeguards, and security review.

## Data Availability

The experimental data supporting the findings of the study, together with the Intercept-J prototype source code and experimental configuration materials, are provided in this repository.

Repository: [https://github.com/cshah3492/intercept-j](https://github.com/cshah3492/intercept-j)

## Citation

If you use Intercept-J in academic work, please cite the associated article once its final bibliographic details are available.

```text
Shah, C. Intercept-J: Deterministic Runtime Policy Enforcement for Secure AI
Tool Invocation in Enterprise Java Applications. Manuscript.
```

The citation block should be updated with the final journal, volume, issue, page range/article number, year, and DOI after publication.

## License

No license is assumed by this README. If the repository does not yet contain a `LICENSE` file, reuse rights remain unspecified. Add an explicit open-source license before inviting third-party reuse if that is the intended distribution model.

## Author

Chirag Shah

GitHub: [https://github.com/cshah3492](https://github.com/cshah3492)
