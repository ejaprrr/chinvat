import { Globe } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { languageLabels, type Locale } from '../i18n'

function LanguageSwitcher() {
  const { i18n, t } = useTranslation()
  const locale = i18n.resolvedLanguage as Locale | undefined

  return (
    <label className="locale-switcher" htmlFor="locale-select">
      <span className="locale-icon">
        <Globe aria-hidden="true" strokeWidth={1.75} />
      </span>
      <select
        id="locale-select"
        value={locale}
        onChange={(event) => void i18n.changeLanguage(event.target.value as Locale)}
        aria-label={t('accessibility.languageLabel')}
      >
        {Object.entries(languageLabels).map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
    </label>
  )
}

export default LanguageSwitcher
