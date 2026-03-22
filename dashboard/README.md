# EvolutionAI Dashboard

Metrics dashboard for AI-driven COBOL modernization of the AWS CardDemo application.

## Views

- **Executive** -- KPIs, module progress, cost tracking, ROI comparison
- **Architecture** -- Module status grid, agent assignments, test coverage
- **Operations** -- Agent performance, token consumption, velocity metrics
- **Compliance** -- Regulatory checks, audit trail, approval chain

## Development

```bash
npm install
npm run dev
```

Open http://localhost:3000 in your browser.

## Docker

```bash
docker build -t evolution-dashboard .
docker run -p 3000:3000 evolution-dashboard
```

## Tech Stack

- Next.js 14 (App Router, standalone output)
- React 18
- TypeScript
- Tailwind CSS
- Recharts
