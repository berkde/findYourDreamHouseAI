# System Architecture

This document presents the application architecture using Mermaid diagrams, covering system context, containers/modules, AI agent orchestration, core request flows, and deployment views.

> **Tech versions**: Java 25, Spring Boot 3.5.4, LangChain4j 1.8.0, PostgreSQL + pgvector, Redis/Redisson + Caffeine, AWS SDK 2.31.x

---

## 1) System Context (C4 - Level 1)

```mermaid
flowchart LR
    user[End User / API Client]

    subgraph backend[Spring Boot Backend]
        direction TB
        api[REST API Layer]
        auth[JWT Auth & Security]
        ai[LLM/AI Module]
        house[House Ads Module]
        imagesvc[Image Processing]
        cache[Cache Layer]
    end

    db[(PostgreSQL + pgvector)]
    redis[(Redis / Redisson)]
    s3[(AWS S3)]
    secrets[(AWS Secrets Manager)]
    cw[(CloudWatch Metrics/Logs)]
    llm["Local LLM Runtime\n(Ollama/Qwen Chat & Embeddings)"]

    user -->|HTTPS REST| api
    api --> auth
    api --> ai
    api --> house
    api --> imagesvc
    api --> cache

    house --> db
    ai --> llm
    ai --> db
    imagesvc --> s3
    cache --> redis
    auth --> secrets
    backend --> cw
```

---

## 2) Containers & Modules (C4 - Level 2)

```mermaid
flowchart TB
    subgraph App[FindYourDreamHouseAI]
        subgraph Web[Web/API]
            controller["Controllers\n- auth\n- houseAds\n- ai"]
            security["Security\nJWT (A256GCM), RBAC"]
        end

        subgraph Domain[Domain & Services]
            userSvc[Authentication Service]
            houseSvc[House Ads Service]
            aiSvc["AI Services\n(Agents & Tools)"]
            msgSvc[Messaging Service]
        end

        subgraph Infra[Infrastructure]
            repo[JPA Repositories]
            cache["Caching\n(Caffeine & Redis/Redisson)"]
            storage[S3 Storage]
            secrets[AWS Secrets Manager]
            metrics[CloudWatch Metrics]
        end

        subgraph LLM[LLM Integration]
            config[LLMConfiguration]
            guardrails["Guardrails\n- Safety\n- Prompt Injection\n- Length/Rate\n- Output Formatting"]
            chatMem["Chat Memory\nMessageWindow & Store"]
            chatModel[OllamaChatModel]
            embedModel[OllamaEmbeddingModel]
            tools["LangChain4j Tools\n(HouseSearchTool, ImageSearchTool)"]
            agents["Agents\n(ConversationAgent, ImageSearchAgent, IntentAgent, FilterAgent)"]
        end
    end

    controller --> security
    controller --> userSvc
    controller --> houseSvc
    controller --> aiSvc
    controller --> msgSvc

    userSvc --> repo
    houseSvc --> repo
    aiSvc --> agents
    aiSvc --> tools
    aiSvc --> embedModel
    aiSvc --> chatModel
    aiSvc --> guardrails
    aiSvc --> chatMem

    cache -->|backing store| redis[(Redis)]
    repo --> db[(PostgreSQL + pgvector)]
    storage --> s3[(AWS S3)]
    secrets --> sm[(Secrets Manager)]
    metrics --> cw[(CloudWatch)]
```

> **Notes**:
> - Packages align with `README` module structure and `src/main/java/com/dreamhouse/ai/...`.
> - `LLMConfiguration` wires agents, tools, models, guardrails, and chat memory.

---

## 3) AI Agents Orchestration

```mermaid
flowchart LR
    subgraph LangChain4j
        agents["Agents\n- ConversationAgent\n- ImageSearchAgent\n- IntentAgent\n- FilterAgent"]
        tools["Tools\n- HouseSearchTool\n- ImageSearchTool"]
        guards["Guardrails\nSafety · Prompt Injection · Length/Rate · Output Formatting"]
        memory[ChatMemoryProvider\nMessageWindowChatMemory]
    end

    chatModel["OllamaChatModel\n(Qwen Chat/VL)"]
    embedModel["OllamaEmbeddingModel\n(Qwen Embeddings)"]
    listeners["Listeners\n(HouseSearchListener, ImageSearchListener)"]

    agents --> tools
    agents --> guards
    agents --> memory
    agents --> chatModel
    chatModel --> listeners
    tools --> services[Application Services]
    services --> db[(PostgreSQL + pgvector)]
    services --> s3[(AWS S3)]
    services --> cache[(Caffeine/Redis)]
```

> **Key points**:
> - Input guardrails run **before** model/tool calls.
> - Output guardrails enforce **JSON formatting** and size limits.
> - `MessageWindowChatMemory` provides **per-session context** with a bounded window.
> - Listeners enable **tool-calling flows** for house and image search.

---

## 4) Sequence: Natural Language Property Search

```mermaid
sequenceDiagram
    participant C as Client
    participant API as AI Controller (REST)
    participant A as ConversationAgent
    participant G as Guardrails
    participant M as ChatMemory
    participant L as OllamaChatModel (Qwen)
    participant T as HouseSearchTool
    participant S as House Service / Repository
    participant DB as PostgreSQL + pgvector

    C->>API: POST /api/v1/ai/search { q }
    API->>A: query(q, sessionId)
    A->>G: validate/input guardrails
    A->>M: load recent messages (window)
    A->>L: chat(q) with tool-use enabled
    L-->>A: tool call -> HouseSearchTool
    A->>T: search(criteria)
    T->>S: findByVectorAndFilters()
    S->>DB: vector similarity search (pgvector)
    DB-->>S: results
    S-->>T: mapped results
    T-->>A: structured response
    A->>G: validate/output formatting guardrail (JSON)
    A-->>API: summary + results
    API-->>C: 200 OK JSON
```

---

## 5) Sequence: Image Similarity Search

```mermaid
sequenceDiagram
    participant C as Client
    participant API as AI Controller (REST)
    participant IA as ImageSearchAgent
    participant G as Guardrails
    participant M as ChatMemory
    participant IM as ImageSearchTool / Service
    participant E as EmbeddingModel (Qwen)
    participant DB as PostgreSQL + pgvector
    participant S3 as AWS S3

    C->>API: POST /api/v1/ai/similar (multipart image)
    API->>IA: analyze(image, hints)
    IA->>G: validate/input guardrails
    IA->>M: load recent messages
    IA->>IM: extract features + embeddings
    IM->>S3: (optional) upload/store image
    IM->>E: generate image/text embeddings
    E-->>IM: vector
    IM->>DB: ANN search via pgvector
    DB-->>IM: k-NN results
    IM-->>IA: inferred description + results
    IA->>G: output formatting guardrail (JSON)
    IA-->>API: structured response
    API-->>C: 200 OK JSON
```

---

## 6) Deployment View (Runtime Options)

```mermaid
flowchart TB
    subgraph Local
        devApp[Spring Boot App\nJava 25]
        ollama[Ollama Runtime\nQwen Chat/VL/Embeddings]
        postgres[(PostgreSQL + pgvector)]
        redis[(Redis)]
    end

    subgraph Docker
        img[openjdk:25-jdk-slim image]
        compose[Docker Compose: app + postgres + redis]
    end

    subgraph Cloud
        ecs[ECS/Fargate or EKS]
        rds[(RDS Postgres + pgvector)]
        elb[ALB/NLB]
        s3[(S3)]
        secrets[(Secrets Manager)]
        cw[(CloudWatch)]
    end

    devApp --> postgres
    devApp --> redis
    devApp --> ollama
    img --> compose
    ecs --> elb
    ecs --> s3
    ecs --> secrets
    ecs --> cw
    ecs --> rds
```
