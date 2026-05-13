<<<<<<< HEAD
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export type DocumentTitleKey =
  | 'meta.pageTitle'
  | 'meta.registerPageTitle'
  | 'meta.resetPasswordPageTitle'
  | 'meta.profilePageTitle';
=======
import { useEffect } from "react";
import { useTranslation } from "react-i18next";

export type DocumentTitleKey =
  | "meta.pageTitle"
  | "meta.registerPageTitle"
  | "meta.resetPasswordPageTitle"
  | "meta.profilePageTitle";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

export function useDocumentTitle(titleKey: DocumentTitleKey) {
  const { i18n, t } = useTranslation();

  useEffect(() => {
    document.title = t(titleKey);
  }, [i18n.language, i18n.resolvedLanguage, t, titleKey]);
<<<<<<< HEAD
}
=======
}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
