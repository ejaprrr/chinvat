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
import { cx } from "../lib/cx";
import { appRoutes } from "../router/paths";
import {
  ActionButton,
  ActionLink,
} from "./ui/Action";
import AuthPageHeader from "./ui/AuthPageHeader";
import InfoPanel from "./ui/InfoPanel";

type FieldErrors = {
  username?: string;
  password?: string;
};

type TranslationKey =
  | "auth.fields.username.required"
  | "auth.fields.password.required"
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

const styles = {
  form: {
    root: "flex flex-col gap-4",
    sectionStack: "flex flex-col gap-3.5",
    fieldStack: "space-y-1.5",
    label: "flex items-center gap-1 text-sm font-medium text-ink",
    requiredMark: "text-brand-600",
    hint: "text-[0.8125rem] text-muted",
    inlineError: "flex items-center gap-2 text-[0.8125rem] text-danger-700",
    warning:
      "rounded-lg border border-warning-border bg-warning-surface px-3 py-2 text-[0.8125rem] text-warning-ink",
    divider: "flex items-center gap-3 text-[0.75rem] text-muted-soft",
    dividerLine: "h-px flex-1 bg-border-subtle",
    supportingActionRow: "flex justify-end",
    auxiliaryRegion: "flex flex-col gap-3.5",
  },
  status: {
    base:
      "flex items-start gap-2 rounded-lg border px-3 py-2.5 text-[0.8125rem] leading-5",
    default: "border-border-subtle bg-surface-subtle text-muted",
    critical: "border-danger-200 bg-danger-50 text-danger-700",
    icon: "mt-0.5 shrink-0",
  },
  control: {
    inputBase:
      "block min-h-11 w-full rounded-lg border bg-white px-3.5 py-3 text-sm text-ink outline-none transition disabled:cursor-not-allowed disabled:opacity-60",
    inputDefault:
      "border-border-subtle focus:border-brand-500 focus:ring-3 focus:ring-brand-500/15",
    inputError:
      "border-danger-200 focus:border-danger-700 focus:ring-3 focus:ring-danger-700/10",
    iconButton:
      "absolute inset-y-0 right-0 inline-flex min-h-11 min-w-11 items-center justify-center rounded-r-lg px-3 text-muted transition hover:bg-surface-hover hover:text-ink focus-visible:z-10 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  },
} as const;

function getInputClassName(hasError: boolean, hasTrailingButton = false) {
  return cx(
    styles.control.inputBase,
    hasTrailingButton ? "pr-12" : "",
    hasError
      ? styles.control.inputError
      : styles.control.inputDefault,
  );
}

function AuthForm({
  isDesktopPlatform,
  onCertificateLogin,
}: AuthFormProps) {
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
      setStatusMessage(null);
      usernameInputRef.current?.focus();
      return;
    }

    if (nextErrors.password) {
      setStatusMessage(null);
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

  const clearTransientFeedback = () => {
    setStatusMessage(null);
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
      className={styles.form.root}
      noValidate
      onSubmit={handleSubmit}
      aria-labelledby="login-title"
      aria-busy={isSubmitting || certificateLaunching}
    >
      <AuthPageHeader
        id="login-title"
        title={t("auth.title")}
        intro={t("auth.intro")}
      />

      {/*
       * Status region:
       * role="alert" + aria-live="assertive" for errors — interrupts immediately.
       * role="status" + aria-live="polite" for confirmations — waits for silence.
       * aria-atomic="true" ensures the full message is read, not just the changed portion.
       */}
      <div
        className={statusMessage ? "block" : "hidden"}
        aria-hidden={statusMessage ? undefined : "true"}
      >
        {statusMessage ? (
          <p
            className={cx(
              styles.status.base,
              isErrorStatus
                ? styles.status.critical
                : styles.status.default,
            )}
            role={isErrorStatus ? "alert" : "status"}
            aria-live={isErrorStatus ? "assertive" : "polite"}
            aria-atomic="true"
          >
            {isErrorStatus && (
              <TriangleAlert
                size={15}
                aria-hidden="true"
                className={styles.status.icon}
              />
            )}
            {resolvedStatusMessage}
          </p>
        ) : null}
      </div>

      <div className={styles.form.sectionStack}>
        {/* ── Username ── */}
        <div className={styles.form.fieldStack}>
          <label htmlFor="username" className={styles.form.label}>
            {t("auth.fields.username.label")}
            {/*
             * The asterisk is purely visual. aria-required on the input
             * conveys "required" to assistive technology without
             * making screen readers say "asterisk".
             */}
            <span className={styles.form.requiredMark} aria-hidden="true">
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
              clearTransientFeedback();
            }}
            inputMode="text"
            className={getInputClassName(Boolean(fieldErrors.username))}
            aria-describedby={usernameDescribedBy}
            aria-errormessage={
              fieldErrors.username ? usernameErrorId : undefined
            }
            aria-invalid={fieldErrors.username ? "true" : "false"}
            aria-required="true"
            required
          />
          <p id={usernameHintId} className={styles.form.hint}>
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
              className={styles.form.inlineError}
              role="alert"
              aria-atomic="true"
            >
              <AlertCircle size={13} aria-hidden="true" />
              <span>{t(fieldErrors.username)}</span>
            </p>
          ) : null}
        </div>

        {/* ── Password ── */}
        <div className={styles.form.fieldStack}>
          <label htmlFor="password" className={styles.form.label}>
            {t("auth.fields.password.label")}
            <span className={styles.form.requiredMark} aria-hidden="true">
              *
            </span>
          </label>

          {/*
           * The wrapper groups the input and reveal toggle visually.
           * The toggle uses aria-pressed (a stateful button) and
           * aria-controls to associate it with the field it modifies.
           * Importantly it is type="button" to prevent form submission.
           */}
          <div className="relative">
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
                clearTransientFeedback();
              }}
              onKeyUp={handleCapsLock}
              onKeyDown={handleCapsLock}
              onBlur={() => setCapsLockOn(false)}
              className={getInputClassName(Boolean(fieldErrors.password), true)}
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
              className={styles.control.iconButton}
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

          <div className="flex flex-col gap-1">
            <p id={passwordHintId} className={styles.form.hint}>
              {t("auth.fields.password.hint")}
            </p>
            <div className={styles.form.supportingActionRow}>
              <ActionLink
                to={appRoutes.resetPassword}
                variant="text"
              >
                {t("auth.actions.resetPassword")}
              </ActionLink>
            </div>
          </div>

          {/*
           * Caps Lock warning is polite — it should not interrupt the user.
           * It is also wired into aria-describedby so it is read on field focus.
           */}
          {capsLockOn ? (
            <p
              id={capsLockWarningId}
              className={styles.form.warning}
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
              className={styles.form.inlineError}
              role="alert"
              aria-atomic="true"
            >
              <AlertCircle size={13} aria-hidden="true" />
              <span>{t(fieldErrors.password)}</span>
            </p>
          ) : null}
        </div>
      </div>

      {/*
       * aria-busy signals submission is in progress.
       * We use disabled to block double-submission but preserve tab stop
       * semantics. aria-busy additionally informs assistive tech.
       */}
      <ActionButton
        type="submit"
        variant="primary"
        aria-busy={isSubmitting}
        disabled={isSubmitting}
      >
        {isSubmitting
          ? t("auth.actions.submitting", {
              defaultValue: "Submitting…",
            })
          : t("auth.actions.continue")}
      </ActionButton>

      <div
        className={styles.form.auxiliaryRegion}
        aria-label={t("auth.certificate.action")}
      >
        <p className={styles.form.divider} aria-hidden="true">
          <span className={styles.form.dividerLine} />
          <span>{t("auth.divider")}</span>
          <span className={styles.form.dividerLine} />
        </p>

        {isDesktopPlatform ? (
          <>
            <ActionButton
              id="certificate-option"
              type="button"
              variant="secondary"
              onClick={handleCertificateLogin}
              aria-describedby={certificateHintId}
              aria-busy={certificateLaunching}
              disabled={certificateLaunching || isSubmitting}
            >
              {t("auth.certificate.action")}
            </ActionButton>
            <p
              id={certificateHintId}
              className={`${styles.form.hint} text-center leading-5`}
            >
              {t("auth.certificate.hint")}
            </p>
          </>
        ) : (
          <InfoPanel
            id="certificate-option"
            align="center"
            role="status"
            aria-live="polite"
          >
            {t("auth.certificate.unavailable")}
          </InfoPanel>
        )}
      </div>
    </form>
  );
}

export default AuthForm;
