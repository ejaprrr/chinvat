import { useEffect, useId, useState, type FormEvent } from 'react'
import { languageLabels, messages, type Locale } from './locales'

type FieldErrors = {
  username?: string
  password?: string
}

function detectLocale(): Locale {
  if (typeof window !== 'undefined') {
    const storedLocale = window.localStorage.getItem('chinvat-locale')
    if (storedLocale && storedLocale in messages) {
      return storedLocale as Locale
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

function App() {
  const [locale, setLocale] = useState<Locale>(detectLocale)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [statusMessage, setStatusMessage] = useState('')
  const [certificateLaunching, setCertificateLaunching] = useState(false)
  const copy = messages[locale]

  const usernameHintId = useId()
  const passwordHintId = useId()
  const certificateHintId = useId()
  const usernameErrorId = useId()
  const passwordErrorId = useId()

  useEffect(() => {
    document.documentElement.lang = locale
    document.title = copy.pageTitle
    window.localStorage.setItem('chinvat-locale', locale)
  }, [copy.pageTitle, locale])

  const isDesktopPlatform =
    typeof window !== 'undefined' &&
    window.matchMedia('(pointer: fine) and (hover: hover) and (min-width: 768px)')
      .matches

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const nextErrors: FieldErrors = {}
    const trimmedUsername = username.trim()

    if (!trimmedUsername) {
      nextErrors.username = copy.usernameRequired
    }

    if (!password) {
      nextErrors.password = copy.passwordRequired
    }

    setFieldErrors(nextErrors)
    setStatusMessage(
      Object.keys(nextErrors).length === 0 ? copy.formReady : copy.errorSummaryTitle,
    )
  }

  const startCertificateLogin = () => {
    setCertificateLaunching(true)
    setStatusMessage(copy.certificateOpening)
    window.location.href = '/cert-login'
  }

  return (
    <>
      <a className="skip-link" href="#main-content">
        {copy.skipToContent}
      </a>

      <main id="main-content" className="page-shell">
        <section className="auth-panel" aria-labelledby="login-title">
          <header className="panel-header">
            <div>
              <p className="eyebrow">{copy.projectName}</p>
              <p className="brand-mark">{copy.brand}</p>
            </div>

            <label className="locale-switcher" htmlFor="locale-select">
              <span className="locale-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                  <path
                    d="M12 2.75a9.25 9.25 0 1 0 0 18.5a9.25 9.25 0 0 0 0-18.5Zm6.98 8.5h-3.16a14.9 14.9 0 0 0-1.24-5.08a7.77 7.77 0 0 1 4.4 5.08ZM12 4.4c.88 1.03 1.82 3.19 2.2 6.85H9.8C10.18 7.59 11.12 5.43 12 4.4Zm-2.58 1.77a14.9 14.9 0 0 0-1.24 5.08H5.02a7.77 7.77 0 0 1 4.4-5.08Zm-4.4 6.58h3.16c.09 1.8.51 3.56 1.24 5.08a7.77 7.77 0 0 1-4.4-5.08Zm4.78 0h4.4c-.38 3.66-1.32 5.82-2.2 6.85c-.88-1.03-1.82-3.19-2.2-6.85Zm5.78 5.08a14.9 14.9 0 0 0 1.24-5.08h3.16a7.77 7.77 0 0 1-4.4 5.08Z"
                    fill="currentColor"
                  />
                </svg>
              </span>
              <select
                id="locale-select"
                value={locale}
                onChange={(event) => setLocale(event.target.value as Locale)}
                aria-label={copy.languageLabel}
              >
                {Object.entries(languageLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
          </header>

          <form className="auth-form" noValidate onSubmit={handleSubmit}>
            <header className="auth-copy">
              <h1 id="login-title">{copy.title}</h1>
              <p>{copy.intro}</p>
            </header>

            {statusMessage ? (
              <p className="status-banner" role="status" aria-live="polite">
                {statusMessage}
              </p>
            ) : null}

            <div className="field">
              <label htmlFor="username">{copy.usernameLabel}</label>
              <input
                id="username"
                type="text"
                name="username"
                autoComplete="username"
                autoCapitalize="none"
                autoCorrect="off"
                spellCheck={false}
                value={username}
                onChange={(event) => {
                  setUsername(event.target.value)
                  setFieldErrors((current) => ({ ...current, username: undefined }))
                }}
                aria-describedby={
                  fieldErrors.username ? `${usernameHintId} ${usernameErrorId}` : usernameHintId
                }
                aria-invalid={fieldErrors.username ? 'true' : 'false'}
                required
              />
              <p id={usernameHintId} className="hint">
                {copy.usernameHint}
              </p>
              {fieldErrors.username ? (
                <p id={usernameErrorId} className="field-error" role="alert">
                  {fieldErrors.username}
                </p>
              ) : null}
            </div>

            <div className="field">
              <label htmlFor="password">{copy.passwordLabel}</label>
              <input
                id="password"
                type="password"
                name="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value)
                  setFieldErrors((current) => ({ ...current, password: undefined }))
                }}
                aria-describedby={
                  fieldErrors.password ? `${passwordHintId} ${passwordErrorId}` : passwordHintId
                }
                aria-invalid={fieldErrors.password ? 'true' : 'false'}
                required
              />
              <p id={passwordHintId} className="hint">
                {copy.passwordHint}
              </p>
              {fieldErrors.password ? (
                <p id={passwordErrorId} className="field-error" role="alert">
                  {fieldErrors.password}
                </p>
              ) : null}
            </div>

            <button type="submit">{copy.continue}</button>

            <div className="divider" aria-hidden="true">
              <span />
              <small>{copy.divider}</small>
              <span />
            </div>

            {isDesktopPlatform ? (
              <>
                <button
                  type="button"
                  className="secondary"
                  onClick={startCertificateLogin}
                  aria-describedby={certificateHintId}
                  disabled={certificateLaunching}
                >
                  {copy.certificate}
                </button>
                <p id={certificateHintId} className="hint">
                  {copy.certificateHint}
                </p>
              </>
            ) : (
              <p id={certificateHintId} className="hint" role="status" aria-live="polite">
                {copy.certificateUnavailable}
              </p>
            )}
          </form>

          <footer className="panel-footer">
            <p>{copy.support}</p>
          </footer>
        </section>
      </main>
    </>
  )
}

export default App
