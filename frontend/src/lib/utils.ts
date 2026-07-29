import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Backend error response format:
 * {
 *   traceId: string;
 *   errorCode: string;
 *   message: string;
 *   severity: 'LOW' | 'MEDIUM' | 'HIGH';
 *   timestamp: string;
 *   path: string;
 *   status: number;
 *   retryable: boolean;
 * }
 */
export interface BackendErrorResponse {
  traceId?: string;
  errorCode?: string;
  message?: string;
  severity?: 'LOW' | 'MEDIUM' | 'HIGH';
  timestamp?: string;
  path?: string;
  status?: number;
  retryable?: boolean;
}

/**
 * Extract error message from backend error response or Axios error.
 * Handles the standard backend error format with traceId, errorCode, message, etc.
 */
export function getErrorMessage(
  error: unknown,
  fallback = 'An unexpected error occurred. Please try again.'
): string {
  if (!error) return fallback;

  // Handle Axios error with response data
  if (typeof error === 'object' && error !== null) {
    const axiosError = error as {
      response?: { data?: BackendErrorResponse };
      message?: string;
    };

    // Backend error response format
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }

    // Direct error object with message
    if (axiosError.message) {
      return axiosError.message;
    }
  }

  // Standard Error instance
  if (error instanceof Error) {
    return error.message;
  }

  // String error
  if (typeof error === 'string') {
    return error;
  }

  return fallback;
}

/**
 * Get full backend error details for logging/debugging
 */
export function getBackendErrorDetails(
  error: unknown
): BackendErrorResponse | null {
  if (!error || typeof error !== 'object') return null;

  const axiosError = error as { response?: { data?: BackendErrorResponse } };
  const data = axiosError.response?.data;

  if (data && typeof data === 'object' && 'errorCode' in data) {
    return data;
  }

  return null;
}

export function formatBytes(
  bytes: number,
  opts: {
    decimals?: number;
    sizeType?: 'accurate' | 'normal';
  } = {}
) {
  const { decimals = 0, sizeType = 'normal' } = opts;

  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const accurateSizes = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB'];
  if (bytes === 0) return '0 Byte';
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(decimals)} ${
    sizeType === 'accurate'
      ? (accurateSizes[i] ?? 'Bytest')
      : (sizes[i] ?? 'Bytes')
  }`;
}

export function formatUSD(value: number) {
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  } catch {
    // Fallback: simple dollar formatting
    const sign = value < 0 ? '-' : '';
    const abs = Math.abs(value);
    return `${sign}$${abs.toLocaleString(undefined, { maximumFractionDigits: 2, minimumFractionDigits: 2 })}`;
  }
}

export type FormatMoneyOptions = {
  locale?: string;
  minimumFractionDigits?: number;
  maximumFractionDigits?: number;
  currency?: string | null;
  currencyDisplay?: 'symbol' | 'code' | 'name' | 'none';
};

export function formatMoney(
  value: number | string | null | undefined,
  opts: FormatMoneyOptions = {}
): string {
  const {
    locale = 'en-US',
    minimumFractionDigits = 2,
    maximumFractionDigits = 2,
    currency = null,
    currencyDisplay = 'none'
  } = opts;

  let num = 0;
  if (typeof value === 'number') num = value;
  else if (typeof value === 'string') num = Number(value.replace(/,/g, ''));
  if (!isFinite(num)) num = 0;

  try {
    if (currency && currencyDisplay !== 'none') {
      return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency,
        currencyDisplay,
        minimumFractionDigits,
        maximumFractionDigits
      }).format(num);
    }
    return new Intl.NumberFormat(locale, {
      style: 'decimal',
      minimumFractionDigits,
      maximumFractionDigits
    }).format(num);
  } catch {
    const sign = num < 0 ? '-' : '';
    const abs = Math.abs(num);
    const formatted = abs.toLocaleString(undefined, {
      minimumFractionDigits,
      maximumFractionDigits
    });
    return `${sign}${formatted}`;
  }
}
