import {
  useId,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff, AlertCircle, TriangleAlert } from "lucide-react";
import {
  handleCertificateLoginStart,
  loginWithCredentials,
} from "../api/auth";

type FieldErrors = {
  username?: string;
  password?: string;
};

type TranslationKey =
  | "auth.fields.username.required"
  | "auth.fields.password.required"
  | "auth.status.errorSummaryTitle"
  | "auth.status.certificateOpening"
  | "auth.status.loginSuccess"
  | "auth.status.loginError"
  | "auth.status.networkError";

type StatusMessage =
  | {
      tone: "default" | "critical";
      text: string;
    }
  | {
      tone: "default" | "critical";
      translationKey: TranslationKey;
    }
  | null;

type AuthFormProps = {
  isDesktopPlatform: boolean;
  onCertificateLogin: () => void;
};

function AuthForm({ isDesktopPlatform, onCertificateLogin }: AuthFormProps) {
  const { t } = useTranslation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [capsLockOn, setCapsLockOn] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage>(null);
  const [certificateLaunching, setCertificateLaunching] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const usernameInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const usernameHintId = useId();
  const passwordHintId = useId();
  const certificateHintId = useId();
  const usernameErrorId = useId();
  const passwordErrorId = useId();
  const capsLockWarningId = useId();

  const handleCapsLock = (event: KeyboardEvent<HTMLInputElement>) => {
    setCapsLockOn(event.getModifierState("CapsLock"));
  };

  const toStatusMessage = (response: {
    ok: boolean;
    message?: string;
    messageKey?: string;
  }): StatusMessage => {
    if (response.messageKey) {
      return {
        tone: response.ok ? "default" : "critical",
        translationKey: response.messageKey as TranslationKey,
      };
    }

    if (response.message) {
      return {
        tone: response.ok ? "default" : "critical",
        text: response.message,
      };
    }

    return null;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};

    if (!username.trim()) {
      nextErrors.username = "auth.fields.username.required";
    }

    if (!password) {
      nextErrors.password = "auth.fields.password.required";
    }

    setFieldErrors(nextErrors);
    const hasErrors = Object.keys(nextErrors).length > 0;

    if (!hasErrors) {
      setIsSubmitting(true);
    }

    if (nextErrors.username) {
      setStatusMessage({
        tone: "critical",
        translationKey: "auth.status.errorSummaryTitle",
      });
      usernameInputRef.current?.focus();
      return;
    }

    if (nextErrors.password) {
      setStatusMessage({
        tone: "critical",
        translationKey: "auth.status.errorSummaryTitle",
      });
      passwordInputRef.current?.focus();
      return;
    }

    const response = await loginWithCredentials({
      username: username.trim(),
      password,
    });

    setStatusMessage(toStatusMessage(response));
    setIsSubmitting(false);
  };

  const handleCertificateLogin = () => {
    const response = handleCertificateLoginStart();

    setCertificateLaunching(true);
    setStatusMessage(toStatusMessage(response));
    onCertificateLogin();
  };

  const isErrorStatus = statusMessage?.tone === "critical";

  const resolvedStatusMessage = statusMessage
    ? "translationKey" in statusMessage
      ? t(statusMessage.translationKey)
      : statusMessage.text
    : "";

  // Build aria-describedby lists per field
  const usernameDescribedBy = [
    usernameHintId,
    fieldErrors.username ? usernameErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");

  const passwordDescribedBy = [
    passwordHintId,
    capsLockOn ? capsLockWarningId : "",
    fieldErrors.password ? passwordErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <form
      className="auth-form"
      noValidate
      onSubmit={handleSubmit}
      aria-labelledby="login-title"
    >
      <header className="auth-copy">
        <h1 id="login-title">{t("auth.title")}</h1>
        <p>{t("auth.intro")}</p>
      </header>

      {/*
       * Status region:
       * role="alert" + aria-live="assertive" for errors — interrupts immediately.
       * role="status" + aria-live="polite" for confirmations — waits for silence.
       * aria-atomic="true" ensures the full message is read, not just the changed portion.
       */}
      <div
        className="status-slot"
        aria-hidden={statusMessage ? undefined : "true"}
      >
        {statusMessage ? (
          <p
            className={`status-banner${isErrorStatus ? " status-banner-critical" : ""}`}
            role={isErrorStatus ? "alert" : "status"}
            aria-live={isErrorStatus ? "assertive" : "polite"}
            aria-atomic="true"
          >
            {isErrorStatus && (
              <TriangleAlert
                size={15}
                aria-hidden="true"
                className="status-icon"
              />
            )}
            {resolvedStatusMessage}
          </p>
        ) : null}
      </div>

      <div className="field-list">
        {/* ── Username ── */}
        <div className="field">
          <label htmlFor="username">
            {t("auth.fields.username.label")}
            {/*
             * The asterisk is purely visual. aria-required on the input
             * conveys "required" to assistive technology without
             * making screen readers say "asterisk".
             */}
            <span className="required-mark" aria-hidden="true">
              *
            </span>
          </label>
          <input
            ref={usernameInputRef}
            id="username"
            type="text"
            name="username"
            autoComplete="username"
            autoCapitalize="none"
            autoCorrect="off"
            spellCheck={false}
            enterKeyHint="next"
            value={username}
            onChange={(event) => {
              setUsername(event.target.value);
              setFieldErrors((current) => ({
                ...current,
                username: undefined,
              }));
            }}
            aria-describedby={usernameDescribedBy}
            aria-errormessage={
              fieldErrors.username ? usernameErrorId : undefined
            }
            aria-invalid={fieldErrors.username ? "true" : "false"}
            aria-required="true"
            required
          />
          <p id={usernameHintId} className="hint">
            {t("auth.fields.username.hint")}
          </p>
          {fieldErrors.username ? (
            /*
             * role="alert" announces inline on insertion.
             * The icon is aria-hidden — the text alone conveys the error.
             * aria-atomic ensures the whole message is announced together.
             */
            <p
              id={usernameErrorId}
              className="field-error"
              role="alert"
              aria-atomic="true"
            >
              <AlertCircle size={13} aria-hidden="true" />
              {t(fieldErrors.username)}
            </p>
          ) : null}
        </div>

        {/* ── Password ── */}
        <div className="field">
          <label htmlFor="password">
            {t("auth.fields.password.label")}
            <span className="required-mark" aria-hidden="true">
              *
            </span>
          </label>

          {/*
           * The wrapper groups the input and reveal toggle visually.
           * The toggle uses aria-pressed (a stateful button) and
           * aria-controls to associate it with the field it modifies.
           * Importantly it is type="button" to prevent form submission.
           */}
          <div className="input-wrapper">
            <input
              ref={passwordInputRef}
              id="password"
              type={showPassword ? "text" : "password"}
              name="password"
              autoComplete="current-password"
              enterKeyHint="go"
              value={password}
              onChange={(event) => {
                setPassword(event.target.value);
                setFieldErrors((current) => ({
                  ...current,
                  password: undefined,
                }));
              }}
              onKeyUp={handleCapsLock}
              onKeyDown={handleCapsLock}
              aria-describedby={passwordDescribedBy}
              aria-errormessage={
                fieldErrors.password ? passwordErrorId : undefined
              }
              aria-invalid={fieldErrors.password ? "true" : "false"}
              aria-required="true"
              required
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword((v) => !v)}
              aria-pressed={showPassword}
              aria-label={
                showPassword
                  ? t("auth.fields.password.hide", {
                      defaultValue: "Hide password",
                    })
                  : t("auth.fields.password.show", {
                      defaultValue: "Show password",
                    })
              }
              aria-controls="password"
            >
              {showPassword ? (
                <EyeOff size={16} aria-hidden="true" />
              ) : (
                <Eye size={16} aria-hidden="true" />
              )}
            </button>
          </div>

          <p id={passwordHintId} className="hint">
            {t("auth.fields.password.hint")}
          </p>

          {/*
           * Caps Lock warning is polite — it should not interrupt the user.
           * It is also wired into aria-describedby so it is read on field focus.
           */}
          {capsLockOn ? (
            <p
              id={capsLockWarningId}
              className="caps-warning"
              role="status"
              aria-live="polite"
              aria-atomic="true"
            >
              {t("auth.fields.password.capsLock", {
                defaultValue: "Caps Lock is on.",
              })}
            </p>
          ) : null}

          {fieldErrors.password ? (
            <p
              id={passwordErrorId}
              className="field-error"
              role="alert"
              aria-atomic="true"
            >
              <AlertCircle size={13} aria-hidden="true" />
              {t(fieldErrors.password)}
            </p>
          ) : null}
        </div>
      </div>

      {/*
       * aria-busy signals submission is in progress.
       * We use disabled to block double-submission but preserve tab stop
       * semantics. aria-busy additionally informs assistive tech.
       */}
      <button
        type="submit"
        className="primary-action"
        aria-busy={isSubmitting}
        disabled={isSubmitting}
      >
        {isSubmitting
          ? t("auth.actions.submitting", {
              defaultValue: "Submitting…",
            })
          : t("auth.actions.continue")}
      </button>

      <div className="alternate-actions" aria-labelledby="certificate-option">
        <p className="divider" aria-hidden="true">
          <span>{t("auth.divider")}</span>
        </p>

        {isDesktopPlatform ? (
          <>
            <button
              id="certificate-option"
              type="button"
              className="secondary"
              onClick={handleCertificateLogin}
              aria-describedby={certificateHintId}
              aria-busy={certificateLaunching}
              disabled={certificateLaunching}
            >
              {t("auth.certificate.action")}
            </button>
            <p id={certificateHintId} className="hint alternate-hint">
              {t("auth.certificate.hint")}
            </p>
          </>
        ) : (
          <p
            id="certificate-option"
            className="hint alternate-hint"
            role="status"
            aria-live="polite"
          >
            {t("auth.certificate.unavailable")}
          </p>
        )}
      </div>

      <p className="support-copy">{t("auth.support")}</p>
    </form>
  );
}

export default AuthForm;
