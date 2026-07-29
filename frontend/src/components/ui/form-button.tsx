import * as React from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

export interface FormButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
  loadingText?: string;
  icon?: React.ReactNode;
  iconPosition?: 'left' | 'right';
  variant?:
    | 'default'
    | 'destructive'
    | 'outline'
    | 'secondary'
    | 'ghost'
    | 'link';
  size?: 'default' | 'sm' | 'lg' | 'icon';
  fullWidth?: boolean;
  rounded?: 'default' | 'full' | 'lg';
  withShimmer?: boolean;
  withAnimatedBorder?: boolean;
}

const FormButton = React.forwardRef<HTMLButtonElement, FormButtonProps>(
  (
    {
      className,
      children,
      loading = false,
      loadingText = 'Loading...',
      icon,
      iconPosition = 'left',
      disabled,
      variant = 'default',
      size = 'default',
      fullWidth = true,
      rounded = 'full',
      withShimmer = true,
      withAnimatedBorder = false,
      ...props
    },
    ref
  ) => {
    // Determine if this is the primary variant
    const isPrimary = variant === 'default';

    // Rounded styles
    const roundedStyles = {
      default: 'rounded-md',
      lg: 'rounded-lg',
      full: 'rounded-full'
    };

    // Size styles - shadcn/ui standard
    const sizeStyles = {
      default: 'h-10 px-4 text-sm',
      sm: 'h-9 px-3 text-sm',
      lg: 'h-11 px-8 text-sm',
      icon: 'size-10'
    };

    return (
      <Button
        className={cn(
          'group relative cursor-pointer overflow-hidden font-semibold transition-all duration-300',
          // Beautiful hover effects for primary buttons with better dark mode visibility
          isPrimary &&
            'bg-primary text-primary-foreground hover:bg-primary/90 hover:scale-[1.02] active:scale-[0.98] dark:bg-[#0ea5e9] dark:hover:bg-[#0284c7]',
          // Outline variant with dynamic color
          variant === 'outline' &&
            'border-primary/20 hover:border-primary hover:bg-primary/5 border-2',
          roundedStyles[rounded],
          sizeStyles[size],
          fullWidth && 'w-full',
          className
        )}
        ref={ref}
        disabled={disabled || loading}
        variant={variant}
        size={size}
        {...props}
      >
        {isPrimary && withAnimatedBorder && !loading && (
          <span
            aria-hidden
            className='pointer-events-none absolute inset-0 animate-spin rounded-[inherit] [mask-composite:exclude] p-[2px] [animation-duration:2.5s] [background:conic-gradient(#29296e,#12ade4,#29296e)] [mask:linear-gradient(#000_0_0)_content-box,linear-gradient(#000_0_0)]'
          />
        )}
        {/* Shimmer effect for primary buttons */}
        {isPrimary && withShimmer && !loading && (
          <span className='absolute inset-0 translate-x-[-100%] bg-gradient-to-r from-white/0 via-white/10 to-white/0 transition-transform duration-700 group-hover:translate-x-[100%]' />
        )}

        {/* Content */}
        <div className='relative z-10 flex items-center justify-center space-x-3'>
          {loading ? (
            <>
              <Loader2 className='h-5 w-5 animate-spin' />
              <span>{loadingText}</span>
            </>
          ) : (
            <>
              {icon && iconPosition === 'left' && (
                <span className='transition-transform duration-300 group-hover:scale-110'>
                  {icon}
                </span>
              )}
              {children && <span>{children}</span>}
              {icon && iconPosition === 'right' && (
                <span className='transition-transform duration-300 group-hover:translate-x-1'>
                  {icon}
                </span>
              )}
            </>
          )}
        </div>
      </Button>
    );
  }
);
FormButton.displayName = 'FormButton';

export { FormButton };
