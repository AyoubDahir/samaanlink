# Build-time NEXT_PUBLIC_* is inlined into the client bundle.
FROM node:22-bookworm-slim AS base
RUN corepack enable && corepack prepare pnpm@10.28.2 --activate
WORKDIR /app

FROM base AS deps
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

FROM base AS builder
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# Baked into next.config rewrites — browser calls same-origin `/api/foody/*`,
# which the Next server forwards here. In Docker Compose use the FoodyExpress
# service name, e.g. http://app:8088.
ARG API_PROXY_TARGET=http://127.0.0.1:8088
ENV API_PROXY_TARGET=$API_PROXY_TARGET
ENV DOCKER_BUILD=1
ENV NEXT_TELEMETRY_DISABLED=1
RUN pnpm build

FROM base AS runner
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
RUN addgroup --system --gid 1001 nodejs && adduser --system --uid 1001 nextjs
COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static
USER nextjs
EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME=0.0.0.0
CMD ["node", "server.js"]
