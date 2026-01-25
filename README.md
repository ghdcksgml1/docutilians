<p align="center">
    <a href="https://github.com/your-org/docutilians">
        <img src="assets/banner.png" width="800px"
             alt="Docutilians - Auto-generate OpenAPI specs from source code using LLM">
    </a>
</p>

<p align="center">
    <a href="#-quickstart">Quickstart</a>
    ·
    <a href="#-how-it-works">How it Works</a>
</p>


<p align="center">
    <strong>🚀 Multi-Agent Pipeline • 📝 Language Agnostic • ⚡ Recursive Type Analysis</strong>
</p>

> **✨ Say goodbye to manual API documentation!** Docutilians uses LLM-powered multi-agent architecture to automatically
> generate accurate OpenAPI specs from your source code.

## Introduction

**Docutilians** is a CLI tool that automatically generates OpenAPI specifications from your source code using LLM. No
more manual documentation—just point to your project and get a complete `openapi.yaml`.

### ✨ Key Features

| Feature                         | Description                                                               |
|---------------------------------|---------------------------------------------------------------------------|
| **🌐 Language Agnostic**        | Supports Kotlin, Java, TypeScript controllers and more                    |
| **🔍 Auto Type Analysis**       | Recursively scans DTOs, Enums, and Entities to build complete schemas     |
| **🤖 Multi-Agent Architecture** | File Collector → YAML Generator → Aggregator pipeline for higher accuracy |
| **📦 Single Output**            | Generates a clean, merged `openapi.yaml` ready for Swagger UI             |

## 🚀 Quickstart

### Installation

```bash
# npm
npm install -g docutilians
```

### Basic Usage

```bash
# Generate OpenAPI spec from your project
docutilians ./target-project

# Custom output directory
docutilians ./target-project -o ./docs/openapi

# Anthropic Key
docutilians -k your-api-key
```

### Environment Setup

```bash
export ANTHROPIC_API_KEY=your-api-key
```

## 🔄 How it Works

```
┌─────────────────────────────────────────┐
│ 1. Project Scanner                      │
│    Discovers controller files           │
└────────────────────┬────────────────────┘
                     ▼
┌─────────────────────────────────────────┐
│ 2. File Collector Agent                 │
│    Gathers referenced DTOs & Enums      │
└────────────────────┬────────────────────┘
                     ▼
┌─────────────────────────────────────────┐
│ 3. YAML Generator Agent                 │
│    Creates OpenAPI YAML per controller  │
└────────────────────┬────────────────────┘
                     ▼
┌─────────────────────────────────────────┐
│ 4. Aggregator Agent                     │
│    Merges & generates metadata          │
└────────────────────┬────────────────────┘
                     ▼
              openapi.yaml ✅
```

## 📁 Output Structure

```
.docutilians/openapi/
├── components/
│   ├── chat_controller.yaml
│   ├── user_controller.yaml
│   └── order_controller.yaml
└── openapi.yaml          # Final merged file
```

## ⚖️ License

MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ by the Docutilians Team
</p>
