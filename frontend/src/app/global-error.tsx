'use client';

import NextError from 'next/error';

export default function GlobalError({
  error
}: {
  error: Error & { digest?: string };
}) {
  return (
    <html>
      <body>
        <p className='sr-only'>{error.message}</p>
        {/* `NextError` is the default Next.js error page component. Its type
        definition requires a `statusCode` prop. However, since the App Router
        does not expose status codes for errors, we simply pass 0 to render a
        generic error message. */}
        <NextError statusCode={0} />
      </body>
    </html>
  );
}
