import { getCertificateLoginUrl } from "../api/auth";
import useDocumentTitle from "../hooks/useDocumentTitle";
import useIsDesktopPlatform from "../hooks/useIsDesktopPlatform";

import {
  useId,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import { useTranslation } from "react-i18next";
import { handleCertificateLoginStart, loginWithCredentials } from "../api/auth";
import { appRoutes } from "../router/paths";
import { ActionButton, ActionLink } from "../components/actions/Action";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import AuthPage from "../components/auth/AuthPage";
import { AuthStepForm } from "../components/auth/AuthForm";
import { AuthDivider } from "../components/auth/AuthSupport";
import PasswordField from "../components/fields/PasswordField";
import TextInput from "../components/fields/TextInput";

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

function LoginPage() {
  useDocumentTitle("meta.pageTitle");
  const isDesktopPlatform = useIsDesktopPlatform();

  const startCertificateLogin = () => {
    window.location.assign(getCertificateLoginUrl());
  };

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
    startCertificateLogin();
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
    <AuthPage
      aria-labelledby="login-title"
      action={
        <div className="auth-floating-language">
          <LanguageSwitcher />
        </div>
      }
      titleId="login-title"
      title={t("auth.title")}
      intro={t("auth.intro")}
      status={
        statusMessage
          ? {
              content: resolvedStatusMessage,
              tone: isErrorStatus ? "critical" : "default",
            }
          : null
      }
    >
      <AuthStepForm
        onSubmit={handleSubmit}
        aria-labelledby="login-title"
        aria-busy={isSubmitting || certificateLaunching}
        actions={
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
        }
      >
        <TextInput
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
          error={Boolean(fieldErrors.username)}
          aria-describedby={usernameDescribedBy}
          aria-errormessage={fieldErrors.username ? usernameErrorId : undefined}
          aria-invalid={fieldErrors.username ? "true" : "false"}
          aria-required="true"
          required
          label={t("auth.fields.username.label")}
          hint={t("auth.fields.username.hint")}
          hintId={usernameHintId}
          fieldError={
            fieldErrors.username ? t(fieldErrors.username) : undefined
          }
          errorId={usernameErrorId}
        />

        <PasswordField
          ref={passwordInputRef}
          id="password"
          name="password"
          label={t("auth.fields.password.label")}
          value={password}
          show={showPassword}
          setShow={setShowPassword}
          onChange={(value) => {
            setPassword(value);
            setFieldErrors((current) => ({
              ...current,
              password: undefined,
            }));
            clearTransientFeedback();
          }}
          autoComplete="current-password"
          enterKeyHint="go"
          onKeyUp={handleCapsLock}
          onKeyDown={handleCapsLock}
          onBlur={() => setCapsLockOn(false)}
          aria-describedby={passwordDescribedBy}
          aria-required="true"
          required
          labelAction={
            <ActionLink
              to={appRoutes.resetPassword}
              variant="text"
              className="w-auto px-0 py-0 text-sm"
            >
              {t("auth.actions.resetPassword")}
            </ActionLink>
          }
          hint={t("auth.fields.password.hint")}
          hintId={passwordHintId}
          status={
            capsLockOn ? (
              <p
                id={capsLockWarningId}
                className="text-[0.8125rem] leading-5 text-warning-ink"
                role="status"
                aria-live="polite"
                aria-atomic="true"
              >
                {t("auth.fields.password.capsLock", {
                  defaultValue: "Caps Lock is on.",
                })}
              </p>
            ) : null
          }
          error={fieldErrors.password ? t(fieldErrors.password) : undefined}
          errorId={passwordErrorId}
          ariaLabelHide={t("auth.fields.password.hide", {
            defaultValue: "Hide password",
          })}
          ariaLabelShow={t("auth.fields.password.show", {
            defaultValue: "Show password",
          })}
        />
      </AuthStepForm>

      <p className="text-center text-sm text-muted">
        {t("auth.register.loginPrompt")}{" "}
        <ActionLink
          to={appRoutes.register}
          variant="text"
          className="min-h-0 w-auto px-0 py-0 text-sm"
        >
          {t("auth.register.actions.open")}
        </ActionLink>
      </p>

      <div
        className="flex flex-col gap-2.5"
        aria-label={t("auth.certificate.action")}
      >
        <AuthDivider>{t("auth.divider")}</AuthDivider>

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
              className="helper-text px-2 text-center leading-5"
            >
              {t("auth.certificate.hint")}
            </p>
          </>
        ) : (
          <p
            id="certificate-option"
            className="helper-text text-center"
            role="status"
            aria-live="polite"
          >
            {t("auth.certificate.unavailable")}
          </p>
        )}
      </div>
    </AuthPage>
  );
}

export default LoginPage;
