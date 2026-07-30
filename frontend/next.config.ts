import type { NextConfig } from 'next';

/**
 * Server-side only: where Next.js forwards `/api/samaanlink/*` to the com.samaanlink backend.
 * Same host as `pnpm dev` in local dev; the backend container's service name in Docker Compose.
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
        source: '/api/samaanlink/:path*',
        destination: `${apiProxyTarget}/api/v1/:path*`
      }
    ];
  }
};

export default baseConfig;
