'use client';

import { useTheme } from 'next-themes';
import { Toaster as Sonner, ToasterProps } from 'sonner';

const Toaster = ({ ...props }: ToasterProps) => {
  const { theme = 'system' } = useTheme();

  return (
    <Sonner
      theme={theme as ToasterProps['theme']}
      className='toaster group'
      style={
        {
          '--normal-bg': 'var(--popover)',
          '--normal-text': 'var(--popover-foreground)',
          '--normal-border': 'var(--border)'
        } as React.CSSProperties
      }
      toastOptions={{
        classNames: {
          error:
            'bg-red-50 dark:bg-red-950 border-red-200 dark:border-red-800 text-red-900 dark:text-red-100',
          success:
            'bg-green-50 dark:bg-green-950 border-green-200 dark:border-green-800 text-green-900 dark:text-green-100',
          warning:
            'bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800 text-yellow-900 dark:text-yellow-100',
          info: 'bg-blue-50 dark:bg-blue-950 border-blue-200 dark:border-blue-800 text-blue-900 dark:text-blue-100'
        }
      }}
      {...props}
    />
  );
};

export { Toaster };
