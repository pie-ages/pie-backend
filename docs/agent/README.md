# Agente de desenvolvimento

A estrutura necessária é:

```text
AGENTS.md
.github/
└── agents/
    └── teacher-agent.agent.md
.vscode/
└── mcp.json
docs/
└── agent/
    ├── workflow.md
    ├── linear.md
    ├── teaching.md
    ├── planning.md
    ├── output-format.md
    ├── tools.md
    └── validation.md
```

## O que cada parte faz

- `AGENTS.md`: regras gerais do agente.
- `.github/agents/teacher-agent.agent.md`: agente chamado `teacher-agent`, que explica conceitos e alternativas sem alterar código.
- `.vscode/mcp.json`: conexão do VS Code com o Linear MCP.
```text
{
  "servers": {
    "linear": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "https://mcp.linear.app/mcp"]
    }
  }
}
```
- `docs/agent/`: detalhes do fluxo, planejamento e formato das respostas.

O agente recebe um ID como `PIE-123`, consulta a tarefa no Linear, analisa o código existente, explica o problema e prepara um plano. Ele só implementa depois de uma solicitação explícita.
