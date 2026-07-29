import { cn } from '@/lib/utils';
import type { ReactNode } from 'react';

interface PageHeaderProps {
  title: string;
  description?: string | ReactNode;
  className?: string;
  actions?: ReactNode;
}

export function PageHeader({
  title,
  description,
  className,
  actions
}: PageHeaderProps) {
  return (
    <div
      className={cn(
        'mb-4 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between',
        className
      )}
    >
      <div className='min-w-0 flex-1'>
        <h2 className='text-xl font-bold text-gray-900 sm:text-2xl dark:text-gray-100'>
          {title}
        </h2>
        {description &&
          (typeof description === 'string' ? (
            <p className='mt-1 text-sm text-gray-600 dark:text-gray-300'>
              {description}
            </p>
          ) : (
            <div className='mt-1'>{description}</div>
          ))}
      </div>
      {actions && <div className='flex-shrink-0'>{actions}</div>}
    </div>
  );
}
