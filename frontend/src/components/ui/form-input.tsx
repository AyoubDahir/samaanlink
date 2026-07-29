import * as React from 'react';
import { Input, InputProps } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { cn } from '@/lib/utils';
import { Eye, EyeOff } from 'lucide-react';

export interface FormInputProps extends Omit<InputProps, 'size'> {
  label?: string;
  description?: string;
  error?: string;
  hideErrorMessage?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  iconPosition?: 'left' | 'right';
  size?: 'sm' | 'default' | 'lg';
  required?: boolean;
  showPasswordToggle?: boolean;
  fullWidth?: boolean;
  wrapperClassName?: string;
}

const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  (
    {
      className,
      label,
      description,
      error,
      hideErrorMessage = false,
      leftIcon,
      rightIcon,
      iconPosition = 'left',
      size = 'default',
      required = false,
      showPasswordToggle = false,
      fullWidth = true,
      wrapperClassName,
      type,
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = React.useState(false);
    const isPasswordField = type === 'password';

    // Icon size based on input size - shadcn/ui standard
    const iconSizeClass = {
      sm: 'h-4 w-4',
      default: 'h-4 w-4',
      lg: 'h-4 w-4'
    };

    // Icon position - shadcn/ui standard (consistent left-3 for all sizes)
    const iconPositionClass = {
      sm: 'left-3',
      default: 'left-3',
      lg: 'left-3'
    };

    const rightIconPositionClass = {
      sm: 'right-3',
      default: 'right-3',
      lg: 'right-3'
    };

    // Padding adjustments for icons - shadcn/ui standard
    const paddingClass = {
      left: {
        sm: 'pl-9',
        default: 'pl-9',
        lg: 'pl-9'
      },
      right: {
        sm: 'pr-9',
        default: 'pr-9',
        lg: 'pr-9'
      }
    };

    const actualType = isPasswordField && showPassword ? 'text' : type;
    const hasRightContent =
      rightIcon || (isPasswordField && showPasswordToggle);

    return (
      <div className={cn('space-y-2', fullWidth && 'w-full', wrapperClassName)}>
        {/* Label */}
        {label && (
          <Label
            className={cn(
              'text-sm font-medium text-gray-900 dark:text-gray-100',
              error && 'text-destructive'
            )}
          >
            {label}
            {required && <span className='text-destructive ml-1'>*</span>}
          </Label>
        )}

        {/* Input Container */}
        <div className='relative'>
          {/* Left Icon */}
          {leftIcon && iconPosition === 'left' && (
            <div
              className={cn(
                'text-muted-foreground pointer-events-none absolute top-1/2 flex -translate-y-1/2 items-center justify-center',
                iconPositionClass[size],
                iconSizeClass[size],
                error && 'text-destructive',
                '[&>svg]:size-4'
              )}
            >
              {leftIcon}
            </div>
          )}

          {/* Input Field */}
          <Input
            type={actualType}
            size={size}
            className={cn(
              'transition-all duration-200',
              leftIcon && iconPosition === 'left' && paddingClass.left[size],
              hasRightContent && paddingClass.right[size],
              error &&
                'border-destructive focus-visible:border-destructive focus-visible:ring-destructive/20',
              className
            )}
            aria-invalid={!!error}
            aria-describedby={
              error
                ? `${props.id}-error`
                : description
                  ? `${props.id}-description`
                  : undefined
            }
            ref={ref}
            {...props}
          />

          {/* Right Icon or Password Toggle */}
          {hasRightContent && (
            <span
              className={cn(
                'absolute top-1/2 -translate-y-1/2 transform',
                rightIconPositionClass[size]
              )}
            >
              {isPasswordField && showPasswordToggle ? (
                <button
                  type='button'
                  onClick={() => setShowPassword(!showPassword)}
                  className='text-muted-foreground hover:text-foreground flex cursor-pointer items-center justify-center transition-colors focus:outline-none'
                  tabIndex={-1}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? (
                    <EyeOff className={iconSizeClass[size]} />
                  ) : (
                    <Eye className={iconSizeClass[size]} />
                  )}
                </button>
              ) : (
                rightIcon && (
                  <div
                    className={cn(
                      'text-muted-foreground pointer-events-none flex items-center justify-center',
                      iconSizeClass[size],
                      error && 'text-destructive',
                      '[&>svg]:size-4'
                    )}
                  >
                    {rightIcon}
                  </div>
                )
              )}
            </span>
          )}
        </div>

        {error && !hideErrorMessage ? (
          <p
            id={`${props.id}-error`}
            className='text-destructive text-xs font-medium'
          >
            {error}
          </p>
        ) : description ? (
          <p
            id={`${props.id}-description`}
            className='text-muted-foreground text-xs'
          >
            {description}
          </p>
        ) : null}
      </div>
    );
  }
);

FormInput.displayName = 'FormInput';

export { FormInput };
