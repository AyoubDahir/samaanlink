import * as React from 'react';

import { cn } from '@/lib/utils';

export interface InputProps
  extends Omit<React.ComponentProps<'input'>, 'size'> {
  size?: 'sm' | 'default' | 'lg';
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, size = 'default', ...props }, ref) => {
    // Size styles - shadcn/ui standard sizes
    const sizeStyles = {
      sm: 'h-8 px-3 py-1 text-xs',
      default: 'h-10 px-3 py-2 text-sm',
      lg: 'h-11 px-3 py-2 text-sm'
    };

    return (
      <input
        type={type}
        data-slot='input'
        className={cn(
          'flex w-full min-w-0 rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 transition-colors outline-none placeholder:text-gray-500',
          'focus:border-primary focus:ring-primary/20 focus:ring-2',
          'dark:focus:border-secondary dark:focus:ring-secondary/20 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-100 dark:placeholder:text-gray-400',
          'disabled:cursor-not-allowed disabled:opacity-50',
          'file:text-foreground file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium',
          'selection:bg-primary selection:text-primary-foreground',
          sizeStyles[size],
          className
        )}
        ref={ref}
        {...props}
      />
    );
  }
);

Input.displayName = 'Input';

export { Input };
