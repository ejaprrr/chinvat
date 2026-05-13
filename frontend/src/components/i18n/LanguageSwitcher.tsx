<<<<<<< HEAD
import { Globe } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { languageLabels, type Locale } from '../../lib/i18n';
=======
import { Globe } from "lucide-react";
import { useTranslation } from "react-i18next";
import { languageLabels, type Locale } from "../../lib/i18n";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

function LanguageSwitcher() {
  const { i18n, t } = useTranslation();
  const locale = i18n.resolvedLanguage as Locale | undefined;

  return (
    <label
      className="inline-flex min-h-9 items-center gap-2 rounded-full border border-border-subtle bg-white px-2.5 text-[0.75rem] text-muted shadow-sm focus-within:outline-none focus-within:ring-4 focus-within:ring-brand-500/10"
      htmlFor="locale-select"
    >
<<<<<<< HEAD
      <span className="sr-only">{t('accessibility.languageLabel')}</span>
=======
      <span className="sr-only">{t("accessibility.languageLabel")}</span>
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      <span className="flex h-3.5 w-3.5 shrink-0 items-center justify-center">
        <Globe aria-hidden="true" size={12} strokeWidth={1.75} />
      </span>
      <select
        id="locale-select"
        value={locale}
        className="min-h-9 min-w-0 rounded-sm bg-transparent py-0.5 text-[0.75rem] font-medium text-inherit outline-none"
<<<<<<< HEAD
        onChange={(event) => void i18n.changeLanguage(event.target.value as Locale)}
=======
        onChange={(event) =>
          void i18n.changeLanguage(event.target.value as Locale)
        }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      >
        {Object.entries(languageLabels).map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
    </label>
  );
}

export default LanguageSwitcher;
