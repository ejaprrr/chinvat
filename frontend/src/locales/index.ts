import ca from './ca.json'
import de from './de.json'
import en from './en.json'
import es from './es.json'
import eu from './eu.json'
import fr from './fr.json'
import gl from './gl.json'

export const messages = {
  es,
  en,
  ca,
  eu,
  gl,
  fr,
  de,
} as const

export type Locale = keyof typeof messages

export const languageLabels: Record<Locale, string> = {
  es: 'Español',
  en: 'English',
  ca: 'Català',
  eu: 'Euskara',
  gl: 'Galego',
  fr: 'Français',
  de: 'Deutsch',
}
