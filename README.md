# quadras-service

[![CI](https://github.com/natan1iel/quadras-service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/natan1iel/quadras-service/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=natan1iel_quadras-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=natan1iel_quadras-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=natan1iel_quadras-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=natan1iel_quadras-service)

Cadastro e disponibilidade das quadras do **Sistema de Reserva de Quadras de Vôlei**.

> A documentação geral do projeto — arquitetura, pipeline, ambientes e execução local — está no repositório [api-gateway](https://github.com/natan1iel/api-gateway).

---

## Requisitos implementados

**1. Cadastro com validação de integridade** — código único, tipo restrito a `AREIA` ou `COBERTA`, intervalo entre 0 e 60 minutos, grade de funcionamento coerente. Operação restrita a `ADMIN`.

**2. Grade de funcionamento por dia da semana** — a quadra não tem um horário único, tem um por dia. Só existe registro para os dias em que ela abre: sem registro para domingo, a quadra está fechada no domingo. O endpoint de disponibilidade recebe uma data, converte em dia da semana e resolve a janela correspondente.

**3. Intervalo obrigatório definido pelo ADMIN** — folga entre o fim de uma reserva e o início da próxima. Este serviço guarda e publica o valor; quem o aplica é o `reservas-service`, que é quem conhece reservas.

## Endpoints

| Método | Rota | Papel |
|---|---|---|
| POST | `/quadras` | ADMIN |
| GET | `/quadras` | qualquer |
| GET | `/quadras/{id}` | qualquer |
| PATCH | `/quadras/{id}/intervalo` | ADMIN |
| DELETE | `/quadras/{id}` | ADMIN |
| GET | `/quadras/{id}/disponibilidade?data=` | qualquer |

O `DELETE` não apaga o registro: marca `ativa = false`. Reservas antigas guardam o `quadraId`, e uma exclusão física deixaria esse histórico órfão.

## Contrato consumido pelo reservas-service

```json
GET /quadras/1/disponibilidade?data=2026-11-10

{
  "id": 1,
  "codigo": "Areia A",
  "tipo": "AREIA",
  "ativa": true,
  "intervaloMinutos": 10,
  "aberta": true,
  "abertura": "07:00",
  "fechamento": "22:00"
}
```

Em um dia sem funcionamento, a resposta é `200` com `aberta: false` — a quadra existe, apenas não abre naquele dia.

## Autorização

Este serviço não usa Spring Security. O gateway já validou o token e injetou `X-Usuario-Papel`; as rotas de escrita apenas conferem esse header e retornam `403` quando o papel não é `ADMIN`.

## Ambientes

| Ambiente | Branch | URL |
|---|---|---|
| DEV | `develop` | https://quadras-service-dev.onrender.com |
| HOMOL | `main` | https://quadras-service-homol-devops.onrender.com |

As chamadas de verdade passam pelo gateway; essas URLs servem para o Swagger e o health check.

## Observabilidade

`/actuator/health` · `/actuator/prometheus` · `/swagger-ui.html` (habilitado por `SWAGGER_ENABLED`)

As métricas também são enviadas ao Grafana Cloud por OTLP, com as tags `application=quadras-service` e `ambiente`. Detalhes no [api-gateway](https://github.com/natan1iel/api-gateway#observabilidade).

## Variáveis de ambiente

`PORT` · `DB_URL` · `DB_USER` · `DB_PASSWORD` · `SWAGGER_ENABLED` · `AMBIENTE` · `OTLP_ENABLED` · `OTLP_URL` · `OTLP_AUTH`
