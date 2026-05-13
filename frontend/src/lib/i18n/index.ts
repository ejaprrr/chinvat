<<<<<<< HEAD
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ca from '../../locales/ca.json';
import en from '../../locales/en.json';
import es from '../../locales/es.json';
import eu from '../../locales/eu.json';
import gl from '../../locales/gl.json';
=======
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import ca from "../../locales/ca.json";
import en from "../../locales/en.json";
import es from "../../locales/es.json";
import eu from "../../locales/eu.json";
import gl from "../../locales/gl.json";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

export const resources = {
  es: { translation: es },
  en: { translation: en },
  ca: { translation: ca },
  eu: { translation: eu },
  gl: { translation: gl },
} as const;

export const languageLabels = {
<<<<<<< HEAD
  es: 'Español',
  en: 'English',
  ca: 'Català',
  eu: 'Euskara',
  gl: 'Galego',
=======
  es: "Español",
  en: "English",
  ca: "Català",
  eu: "Euskara",
  gl: "Galego",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
} as const;

export type Locale = keyof typeof resources;

function syncDocumentLanguage(language: string) {
<<<<<<< HEAD
  if (typeof document !== 'undefined') {
=======
  if (typeof document !== "undefined") {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    document.documentElement.lang = language;
  }
}

function detectLanguage(): Locale {
<<<<<<< HEAD
  if (typeof window !== 'undefined') {
    const storedLanguage = window.localStorage.getItem('chinvat-locale');
=======
  if (typeof window !== "undefined") {
    const storedLanguage = window.localStorage.getItem("chinvat-locale");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    if (storedLanguage && storedLanguage in resources) {
      return storedLanguage as Locale;
    }
  }

<<<<<<< HEAD
  if (typeof navigator === 'undefined') {
    return 'es';
=======
  if (typeof navigator === "undefined") {
    return "es";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  }

  const candidates = [navigator.language, ...(navigator.languages ?? [])]
    .filter(Boolean)
    .map((value) => value.toLowerCase());

  for (const value of candidates) {
<<<<<<< HEAD
    if (value.startsWith('ca')) return 'ca';
    if (value.startsWith('eu')) return 'eu';
    if (value.startsWith('gl')) return 'gl';
    if (value.startsWith('en')) return 'en';
    if (value.startsWith('es')) return 'es';
  }

  return 'en';
=======
    if (value.startsWith("ca")) return "ca";
    if (value.startsWith("eu")) return "eu";
    if (value.startsWith("gl")) return "gl";
    if (value.startsWith("en")) return "en";
    if (value.startsWith("es")) return "es";
  }

  return "en";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

const initialLanguage = detectLanguage();
syncDocumentLanguage(initialLanguage);

void i18n.use(initReactI18next).init({
  resources,
  lng: initialLanguage,
<<<<<<< HEAD
  fallbackLng: 'en',
=======
  fallbackLng: "en",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  supportedLngs: Object.keys(resources),
  interpolation: {
    escapeValue: false,
  },
});

<<<<<<< HEAD
i18n.on('languageChanged', (language) => {
  syncDocumentLanguage(language);

  if (typeof window !== 'undefined' && language in resources) {
    window.localStorage.setItem('chinvat-locale', language);
=======
i18n.on("languageChanged", (language) => {
  syncDocumentLanguage(language);

  if (typeof window !== "undefined" && language in resources) {
    window.localStorage.setItem("chinvat-locale", language);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  }
});

export default i18n;
