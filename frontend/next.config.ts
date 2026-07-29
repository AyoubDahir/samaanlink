import type { NextConfig } from 'next';

/**
 * Server-side only: where Next.js forwards `/api/foody/*`.
 * Same host as `pnpm dev` in local dev; the FoodyExpress container's service name in Docker Compose.
 */
const apiProxyTarget = (
  process.env.API_PROXY_TARGET || 'http://localhost:8088'
).replace(/\/$/, '');

const baseConfig: NextConfig = {
  output: process.env.DOCKER_BUILD === '1' ? 'standalone' : undefined,
  poweredByHeader: false,
  devIndicators: false,

  eslint: {
    ignoreDuringBuilds: true
  },
  typescript: {
    ignoreBuildErrors: false
  },

  images: {
    remotePatterns: []
  },

  async rewrites() {
    return [
      {
        source: '/api/foody/:path*',
        destination: `${apiProxyTarget}/:path*`
      },
      {
        // com.samaanlink JWT-secured API (new modular backend), separate from the legacy
        // session-key `/api/foody/*` bridge above.
        source: '/api/samaanlink/:path*',
        destination: `${apiProxyTarget}/api/v1/:path*`
      }
    ];
  }
};

export default baseConfig;
