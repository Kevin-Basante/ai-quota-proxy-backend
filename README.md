# AI Quota Proxy · Backend

AI consumption platform where every text generation request travels through a chain of **Proxy** objects that enforce rate limiting and monthly token quotas before reaching a simulated AI service.

Built with **Java 21** and **Spring Boot 3**.

## Proxy pattern

| Role | Class |
| --- | --- |
| Subject | `AIGenerationService` |
| Real subject | `MockAIGenerationService` |
| Proxy 1 | `RateLimitProxyService` |
| Proxy 2 | `QuotaProxyService` |
| Client | `AIController` |

```
Client ──▶ RateLimitProxyService ──▶ QuotaProxyService ──▶ MockAIGenerationService
              │ 429 + Retry-After        │ 402 Payment Required
```

All three classes implement the same interface, so the controller never knows whether it talks to a proxy or to the real service.

- **RateLimitProxyService** counts requests per user inside a one-minute window. When the plan limit is exceeded it raises `429 Too Many Requests` with the `Retry-After` header.
- **QuotaProxyService** checks the monthly token budget, delegates the call, then deducts the consumed tokens and records daily usage. When the budget is exhausted it raises `402 Payment Required`.
- **MockAIGenerationService** waits 1200 ms with `Thread.sleep` and returns a random predefined text.

### Other patterns used

- **Builder**: `AIGenerationChainBuilder` assembles the proxy chain in request order.
- **Singleton**: every Spring bean (registries, repositories, services) is a single shared instance.

## Plans

| Plan | Requests / minute | Tokens / month | Price |
| --- | --- | --- | --- |
| FREE | 10 | 50,000 | $0 |
| PRO | 60 | 500,000 | $19 |
| ENTERPRISE | unlimited | unlimited | $199 |

## Endpoints

All user endpoints require the `X-User-Id` header.

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/ai/generate` | Sends `{ "prompt": "..." }` through the full proxy chain |
| GET | `/api/quota/status` | Tokens used, tokens remaining, reset date, plan and current minute usage |
| GET | `/api/quota/history` | Daily usage of the last 7 days |
| POST | `/api/quota/upgrade` | Upgrades the plan with a simulated payment `{ "targetPlan": "PRO", "cardHolder": "...", "cardNumber": "4242424242424242" }` |
| GET | `/api/users` | Demo accounts |
| GET | `/api/plans` | Plan catalog |
| POST | `/api/demo/reset` | Restores the demo data |
| GET | `/api/health` | Health check |

Cards ending in `0000` are declined by the payment simulator.

## Scheduled tasks

`QuotaResetScheduler` clears the rate limit window every minute (`0 * * * * *`) and resets every monthly quota on the first day of the month (`0 0 0 1 * *`).

## Demo accounts

| Id | Plan | State |
| --- | --- | --- |
| `aurora` | FREE | 49,420 tokens used, a few prompts away from the 402 |
| `nova` | FREE | Fresh account, ideal to hit the 429 |
| `atlas` | PRO | 212,300 tokens used |
| `orion` | ENTERPRISE | No limits |

Data lives in memory and is restored on every start.

## Run locally

```bash
mvn spring-boot:run
mvn test
```

The API starts on `http://localhost:8080`.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://*.vercel.app` | Allowed frontend origins, comma separated |
| `APP_TIME_ZONE` | `America/Bogota` | Zone used for the scheduler and daily usage |

## Deployment on Render

1. New **Web Service** connected to this repository.
2. Runtime **Docker** (the `Dockerfile` builds and runs the jar).
3. Health check path `/api/health`.
4. Set `CORS_ALLOWED_ORIGINS` with the Vercel URL of the frontend.
