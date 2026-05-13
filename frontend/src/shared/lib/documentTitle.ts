import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export type DocumentTitleKey =
  | 'meta.pageTitle'
  | 'meta.registerPageTitle'
  | 'meta.resetPasswordPageTitle'
  | 'meta.profilePageTitle';
export function useDocumentTitle(titleKey: DocumentTitleKey) {
  const { i18n, t } = useTranslation();

  useEffect(() => {
    document.title = t(titleKey);
  }, [i18n.language, i18n.resolvedLanguage, t, titleKey]);
}
