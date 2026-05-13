import { useTranslation } from 'react-i18next';
import { useCallback } from 'react';
import type { AppHttpError, ErrorDisplay } from '@/shared/api/errors';

export type ErrorDisplayValue = AppHttpError | ErrorDisplay | string;

/**
 * Hook for displaying localized error messages.
 * Uses i18n to resolve error message keys to localized strings.
 */
export function useErrorDisplay() {
  const { t } = useTranslation();

  const getDisplayMessage = useCallback(
    (error: ErrorDisplayValue): string => {
      if (typeof error === 'string') {
        return error;
      }

      if (error.messageKey) {
        const translated = t(error.messageKey, {
          defaultValue: error.message,
        });
        if (translated !== error.messageKey) {
          return translated;
        }
      }

      if ('backendMessage' in error && error.backendMessage) {
        return error.backendMessage;
      }

      return error.message || t('error.common.internal-error');
    },
    [t],
  );

  return { getDisplayMessage };
}
