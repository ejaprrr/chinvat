import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import ca from './locales/ca.json'
import de from './locales/de.json'
import en from './locales/en.json'
import es from './locales/es.json'
import eu from './locales/eu.json'
import fr from './locales/fr.json'
import gl from './locales/gl.json'

export const resources = {
  es: { translation: es },
  en: { translation: en },
  ca: { translation: ca },
  eu: { translation: eu },
  gl: { translation: gl },
  fr: { translation: fr },
  de: { translation: de },
} as const

export const languageLabels = {
  es: 'Español',
  en: 'English',
  ca: 'Català',
  eu: 'Euskara',
  gl: 'Galego',
  fr: 'Français',
  de: 'Deutsch',
} as const

export type Locale = keyof typeof resources

function detectLanguage(): Locale {
  if (typeof window !== 'undefined') {
    const storedLanguage = window.localStorage.getItem('chinvat-locale')
    if (storedLanguage && storedLanguage in resources) {
      return storedLanguage as Locale
    }
  }

  if (typeof navigator === 'undefined') {
    return 'es'
  }

  const candidates = [navigator.language, ...(navigator.languages ?? [])]
    .filter(Boolean)
    .map((value) => value.toLowerCase())

  for (const value of candidates) {
    if (value.startsWith('ca')) return 'ca'
    if (value.startsWith('eu') || value.startsWith('ba')) return 'eu'
    if (value.startsWith('gl')) return 'gl'
    if (value.startsWith('fr')) return 'fr'
    if (value.startsWith('de')) return 'de'
    if (value.startsWith('en')) return 'en'
    if (value.startsWith('es')) return 'es'
  }

  return 'en'
}

void i18n.use(initReactI18next).init({
  resources,
  lng: detectLanguage(),
  fallbackLng: 'en',
  supportedLngs: Object.keys(resources),
  interpolation: {
    escapeValue: false,
  },
})

i18n.on('languageChanged', (language) => {
  if (typeof window !== 'undefined' && language in resources) {
    window.localStorage.setItem('chinvat-locale', language)
  }
})

export default i18n
