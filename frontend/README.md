# SamaanLink — Food Express Web UI

Customer + admin web frontend for **FoodyExpress**, a Spring Boot food-ordering REST API. Built with **Next.js 15**, **React 19**, **TypeScript**, and **Tailwind CSS 4**.

This project started life as a hospital ERP shell (`prime-frontend`) and was stripped down to its generic Next.js/Tailwind/shadcn scaffold — layout, design system primitives, and security middleware — then rebuilt with pages specific to FoodyExpress.

## Tech Stack

- **Next.js 15** (App Router)
- **React 19** + **TypeScript 5**
- **Tailwind CSS 4** + **shadcn/ui** + **Radix UI**
- **pnpm** (package manager)

## Backend

Talks directly to the [FoodyExpress](../samaanlink/FoodyExpress) Spring Boot API. Auth is FoodyExpress's own scheme: `POST /app/login` returns a short-lived session `key` string (no JWT/OAuth), which is then passed as a query param on every subsequent request. See `src/lib/api.ts` and `src/lib/session.ts`.

## Project Structure

```
src/
├── app/
│   ├── (auth)/          # Sign in, sign up
│   ├── (app)/            # Authenticated customer + admin pages
│   └── layout.tsx        # Root layout
├── components/
│   ├── ui/                # Base UI (Button, Input, Card, Form)
│   ├── shared/             # PageHeader, etc.
│   └── layout/             # Sidebar/Navbar, theme toggle
├── lib/
│   ├── api.ts              # FoodyExpress API client (all endpoints)
│   ├── session.ts           # Client-side session (key/role/customerId) storage
│   └── utils.ts
└── types/
```

## Getting Started

### Prerequisites

- **Node.js 20+**
- **pnpm 9+**
- FoodyExpress backend running (see repo root `docker-compose.yml` — `docker compose up -d`), reachable at `http://localhost:8088` by default

### Install & Run

```bash
pnpm install
cp .env.example .env.local   # set NEXT_PUBLIC_API_URL if not http://localhost:8088
pnpm dev                      # starts on http://localhost:3000
```

### Build

```bash
pnpm build
pnpm start
```

## Docker

```bash
docker build -t samaanlink-frontend --build-arg NEXT_PUBLIC_API_URL=http://localhost:8088 .
docker run -p 3000:3000 samaanlink-frontend
```

Or run both frontend and backend together:

```bash
docker-compose up -d
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | FoodyExpress backend base URL | `http://localhost:8088` |

## Scripts

```bash
pnpm dev           # Dev server
pnpm build         # Production build
pnpm start         # Production server
pnpm lint          # ESLint
pnpm format        # Prettier
```
