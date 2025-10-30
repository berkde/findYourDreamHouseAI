# Security Policy — findYourDreamHouseAI

Last updated: 2025-10-30

This repository powers a commercial product. Protecting customer data, maintaining service availability, and reducing risk from AI/LLM misuse are top priorities. This document describes how we handle security issues, responsible disclosure, and our approach to secure development and operations.

---

## Reporting a Vulnerability

If you discover a security vulnerability in this project or its deployed services, please report it privately and promptly.

Preferred reporting channels:
- Email: berkdelibalta@gmail.com
- GitHub: open a private security issue to berkde/findYourDreamHouseAI (if available)
- PGP (optional): PGP key fingerprint: 9A3B 8F2C ... (contact berkdelibalta@gmail.com for the key)

Please do NOT create public GitHub issues, open pull requests, or share the vulnerability publicly until it is fixed and coordinated with our team.

When reporting, include:
- A clear, concise summary of the issue
- The affected component(s) and versions (repo paths, service endpoints, deployed versions)
- Reproduction steps, PoC code or curl commands (minimal and safe)
- Expected vs actual behavior
- Any exploitability or impact assessment (data exposure, privilege escalation, cost, availability)
- Your contact information and a preferred encrypted channel if you want confidentiality

We will acknowledge receipt within 72 hours and provide status updates during remediation.

---

## Vulnerability Handling & Timelines

To ensure responsible disclosure and remediation, we follow these target timelines:

- Acknowledgement: within 72 hours of receipt.
- Initial triage and severity classification: within 7 days.
- Fix or mitigation plan: within 30 days for high-severity issues; longer for complex supply-chain or architecture changes (we'll provide milestones).
- Coordinated public disclosure: typically within 90 days of initial report, or sooner if required by law, or later if both parties agree.

If you are a security researcher, please include whether you request a coordinated disclosure window or wish for a credit line in our acknowledgements.

---

## Safe Harbor / Legal

We welcome responsible security research. As long as you follow these rules we will not pursue legal action:
- Limit testing to the minimum required to reproduce the issue.
- Do not access, exfiltrate, or modify data beyond what is necessary to demonstrate the vulnerability.
- Do not create or distribute proof-of-concept exploits that would enable mass exploitation.
- Do not attempt social-engineering or attacks on third parties as part of testing.

If you are unsure, contact berkdelibalta@gmail.com first.

---

## Severity Classification

We use a simplified severity model to prioritize response:

- Critical: Remote code execution, complete data exfiltration of production customer data, or an active exploit against production systems.
- High: Privilege escalation, unauthenticated access to sensitive services, or serious injection vulnerabilities that could lead to data loss.
- Medium: Information disclosure, authentication bypass with limited impact, or significant misconfiguration.
- Low: Non-sensitive information leaks, security headers missing, or minor misconfigurations.
- Informational: Best-practice suggestions, low-impact hardening, or documentation improvements.

Remediation timelines will be proportional to severity.

---

## Privacy & Data Handling

This project interacts with user inputs and LLM outputs. Treat all user prompts, model outputs, logs, and analytics as potentially sensitive.

Key rules:
- Do not log full prompts or PII to insecure logs. Redact PII before persistent storage.
- Any storage of user content must be justified, access-controlled, and retention-limited.
- Production secrets (API keys, DB credentials) MUST never be committed to source control.
- Use environment variables, secret managers (e.g., HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager), and enforce least privilege.
- Follow applicable data protection laws (GDPR, CCPA) for user data processing. Contact legal for cross-border transfers.

---

## Secure Development & CI/CD

We adhere to secure-by-default practices in development and release pipelines:

- Branch protection: require PR reviews and passing CI to merge to main.
- Code review: require at least one security-aware reviewer for sensitive components (auth, AI pipelines, integrations).
- Static analysis: run SAST in CI (e.g., SpotBugs, PMD, or configured tools for JVM).
- Dependency scanning: run SCA (e.g., Dependabot, Snyk) and address critical/known exploitable vulnerabilities promptly.
- Secrets scanning: run secret-detection in CI and pre-commit hooks (e.g., git-secrets).
- Reproducible builds: where applicable, record build metadata and hashes to support supply-chain audits.
- Release notes: include security-relevant changes in release notes when applicable.

---

## Runtime Protection & Observability

To protect production services and detect incidents:

- Monitoring & Alerts: Prometheus + alerting for anomalies (latency, error spikes, authentication failures).
- Guardrails: implement input sanitization and AI output moderation as core defenses (see repo issues #13 and #14).
- Rate limiting & quotas: per-user and per-api-key rate limits for AI services; defense against abuse and cost explosions.
- Logging: structured logs (JSON) with sensitive fields redacted. Retention and access controls applied.
- Auditing: record administrative operations and configuration changes.
- Incident Response: runbooks for containment, forensics, and recovery. Critical incidents triaged immediately with an on-call rota.

---

## AI / LLM Specific Security

Because this project uses LLMs and agent chains (LangChain4j), additional controls are required:

- Prompt & Response Guardrails: implement pre- and post-processing moderation, length limits, injection detection, relevance checks, and formatting validation (see issue #13).
- Input Sanitization: controller-level sanitization to prevent hidden/obfuscated payloads, XSS, and command-like injections (see issue #14).
- Model & API Management: pin model versions where required, rotate API keys regularly, and monitor costs and anomalous model behavior.
- Data Minimization: avoid sending PII or secrets in prompts. If sensitive data must be used, consider on-prem or private model hosting and encryption in transit.
- Fallbacks: define safe default behaviors for moderation system failures (allow-list/deny-list policies).
- Explainability & Logging: keep reproducible prompt+system-message+model-version records for debugging incidents without storing sensitive data long-term.

---

## Third-Party Services & Supply Chain

We rely on external packages and cloud services. Security guidance:

- Vendor evaluation: document third-party AI providers, moderation APIs, and their security/compliance posture.
- Pin dependencies: avoid floating versions for critical libraries. Use lockfiles and periodic dependency updates.
- Container security: scan images for CVEs and rebuild base images regularly.
- Least privilege: cloud IAM policy least-privilege for services and CI runners.
- Webhooks & Integrations: verify signatures, rotate secrets, and restrict allowed IPs where possible.

---

## Incident Response & Disclosure

If an incident occurs that affects customer data or availability:

- Triage & Containment: immediate steps to contain impact and mitigate ongoing exploitation.
- Communication: notify affected customers and regulators as required by law. Provide an incident timeline and remediation steps.
- Post-mortem: publish a blameless root-cause analysis for internal stakeholders; public summary when appropriate.
- Remediation: implement permanent fixes and update runbooks.

---

## Tests & Validation

We maintain tests and validation to reduce regressions:

- Security unit/integration tests for guardrails (OWASP LLM Top 10 test suites and prompt-injection tests).
- Synthetic traffic & fuzzing for agent pipelines.
- Regular pentests: schedule manual penetration tests at least annually or before major releases.
- CI gates: block merges on failing security checks.

---

## Rewards & Acknowledgements

We appreciate responsible disclosure. At this time we do not offer a formal bug bounty. We will acknowledge researchers who follow responsible disclosure and opt-in to public recognition. Contact security@findyourdreamhouse.ai to discuss recognition.

---

## Contact & Escalation

- Security contact: berkdelibalta@gmail.com
- Owner/GitHub: @berkde
- Emergency (incident affecting production confidentiality/integrity/availability): use the security email with subject prefix "INCIDENT:"

---

## Appendix — Quick Checklist for Contributors

- Do not commit credentials, API keys or PII.
- Add tests for security-related changes.
- Run local linters, SAST, and secret scanners before opening PRs.
- Document external services and any security considerations in PR descriptions.
- Add configuration schema entries in application.yml and document secure defaults.

---

Thank you for helping keep findYourDreamHouseAI secure. If you have suggestions to improve this policy, please file a security report or contact berkdelibalta@gmail.com.
