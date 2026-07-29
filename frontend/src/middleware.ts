import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

function generateNonce(): string {
  const array = new Uint8Array(16);
  crypto.getRandomValues(array);
  return Buffer.from(array).toString('base64');
}

export async function middleware(request: NextRequest) {
  const nonce = generateNonce();
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set('x-nonce', nonce);

  const response = NextResponse.next({
    request: { headers: requestHeaders }
  });

  const isHttps =
    request.nextUrl.protocol === 'https:' ||
    request.headers.get('x-forwarded-proto') === 'https';

  const apiBase =
    process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, '') ||
    'http://localhost:8088';

  const cspDirectives = [
    "default-src 'self'",
    "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data: blob: https:",
    "font-src 'self' data:",
    `connect-src 'self' ${apiBase}`,
    "frame-src 'self'",
    "frame-ancestors 'none'",
    "base-uri 'self'",
    "form-action 'self'"
  ];

  if (isHttps) {
    cspDirectives.push('upgrade-insecure-requests');
  }

  response.headers.set('Content-Security-Policy', cspDirectives.join('; '));
  response.headers.set('x-nonce', nonce);
  if (isHttps) {
    response.headers.set(
      'Strict-Transport-Security',
      'max-age=63072000; includeSubDomains; preload'
    );
  }
  response.headers.set('X-Frame-Options', 'DENY');
  response.headers.set('X-Content-Type-Options', 'nosniff');
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
  response.headers.set(
    'Permissions-Policy',
    'accelerometer=(), camera=(), geolocation=(), microphone=(), payment=(), usb=(), magnetometer=(), gyroscope=()'
  );

  return response;
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon\\.ico).*)']
};
