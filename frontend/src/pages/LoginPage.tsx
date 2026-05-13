<<<<<<< HEAD
import { useId, useRef, useState, type FormEvent, type KeyboardEvent } from 'react';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';
import { useDocumentTitle } from '../lib/documentTitle';
import { useAuth } from '../contexts/auth';
import { eidasLogin } from '../lib/api/auth';
import { getErrorDisplay } from '../lib/http/errors';
import { setTokens } from '../lib/auth/tokenStorage';
import { isWellFormedEmail } from '../lib/validation/user';
import { appRoutes } from '../router/routes.ts';
import { ActionButton, ActionLink } from '../components/forms/Action';
import LanguageSwitcher from '../components/i18n/LanguageSwitcher';
import AuthPage from '../components/auth/AuthPage';
import { AuthStepForm } from '../components/auth/AuthForm';
import PasswordField from '../components/forms/PasswordField';
import TextInput from '../components/forms/TextInput';
=======
import {
  useId,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { useDocumentTitle } from "../lib/documentTitle";
import { useAuth } from "../contexts/auth";
import { eidasLogin } from "../lib/api/auth";
import { getErrorDisplay } from "../lib/http/errors";
import { setTokens } from "../lib/auth/tokenStorage";
import { isWellFormedEmail } from "../lib/validation/user";
import { appRoutes } from "../router/routes.ts";
import { ActionButton, ActionLink } from "../components/forms/Action";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import AuthPage from "../components/auth/AuthPage";
import { AuthStepForm } from "../components/auth/AuthForm";
import PasswordField from "../components/forms/PasswordField";
import TextInput from "../components/forms/TextInput";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type FieldErrors = {
  email?: string;
  password?: string;
};

function LoginPage() {
<<<<<<< HEAD
  useDocumentTitle('meta.pageTitle');
=======
  useDocumentTitle("meta.pageTitle");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login, refreshUser, error: authError, reportError } = useAuth();

<<<<<<< HEAD
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
=======
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [showPassword, setShowPassword] = useState(false);
  const [capsLockOn, setCapsLockOn] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isOpeningCert, setIsOpeningCert] = useState(false);

  const emailInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const emailHintId = useId();
  const passwordHintId = useId();
  const emailErrorId = useId();
  const passwordErrorId = useId();
  const capsLockWarningId = useId();

  const handleCapsLock = (event: KeyboardEvent<HTMLInputElement>) => {
<<<<<<< HEAD
    setCapsLockOn(event.getModifierState('CapsLock'));
=======
    setCapsLockOn(event.getModifierState("CapsLock"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
<<<<<<< HEAD
      nextErrors.email = 'auth.fields.email.required';
    } else if (!isWellFormedEmail(trimmedEmail)) {
      nextErrors.email = 'auth.fields.email.invalid';
    }

    if (!password) {
      nextErrors.password = 'auth.fields.password.required';
=======
      nextErrors.email = "auth.fields.email.required";
    } else if (!isWellFormedEmail(trimmedEmail)) {
      nextErrors.email = "auth.fields.email.invalid";
    }

    if (!password) {
      nextErrors.password = "auth.fields.password.required";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }

    setFieldErrors(nextErrors);

    if (nextErrors.email) {
      emailInputRef.current?.focus();
      return;
    }

    if (nextErrors.password) {
      passwordInputRef.current?.focus();
      return;
    }

    try {
      setIsSubmitting(true);
      await login(trimmedEmail, password);
      navigate(appRoutes.profile, { replace: true });
    } catch (error) {
      const detail = getErrorDisplay(error, {
<<<<<<< HEAD
        fallbackCode: 'AUTH_LOGIN_FAILED',
        fallbackMessage: t('auth.status.loginError'),
=======
        fallbackCode: "AUTH_LOGIN_FAILED",
        fallbackMessage: t("auth.status.loginError"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      });
      setStatusMessage(detail.message);
      reportError(detail.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEidas = async () => {
<<<<<<< HEAD
    setStatusMessage(t('auth.status.certificateOpening'));
=======
    setStatusMessage(t("auth.status.certificateOpening"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    setIsOpeningCert(true);
    try {
      const response = await eidasLogin();
      // Persist tokens and refresh auth state
      setTokens(response.tokens.accessToken, response.tokens.refreshToken);
      await refreshUser();
      navigate(appRoutes.profile, { replace: true });
    } catch (error) {
      const detail = getErrorDisplay(error, {
<<<<<<< HEAD
        fallbackCode: 'AUTH_EIDAS_LOGIN_FAILED',
        fallbackMessage: t('auth.status.loginError'),
=======
        fallbackCode: "AUTH_EIDAS_LOGIN_FAILED",
        fallbackMessage: t("auth.status.loginError"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      });
      setStatusMessage(detail.message);
      reportError(detail.message);
    } finally {
      setIsOpeningCert(false);
    }
  };

<<<<<<< HEAD
  const emailDescribedBy = [emailHintId, fieldErrors.email ? emailErrorId : '']
    .filter(Boolean)
    .join(' ');

  const passwordDescribedBy = [
    passwordHintId,
    capsLockOn ? capsLockWarningId : '',
    fieldErrors.password ? passwordErrorId : '',
  ]
    .filter(Boolean)
    .join(' ');
=======
  const emailDescribedBy = [emailHintId, fieldErrors.email ? emailErrorId : ""]
    .filter(Boolean)
    .join(" ");

  const passwordDescribedBy = [
    passwordHintId,
    capsLockOn ? capsLockWarningId : "",
    fieldErrors.password ? passwordErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  return (
    <AuthPage
      aria-labelledby="login-title"
      action={
        <div className="auth-floating-language">
          <LanguageSwitcher />
        </div>
      }
      titleId="login-title"
<<<<<<< HEAD
      title={t('auth.title')}
      intro={t('auth.intro', {
        defaultValue: 'Use your email and password to sign in.',
=======
      title={t("auth.title")}
      intro={t("auth.intro", {
        defaultValue: "Use your email and password to sign in.",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      })}
      status={
        statusMessage || authError
          ? {
              content: statusMessage || authError,
<<<<<<< HEAD
              tone: 'critical',
=======
              tone: "critical",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            }
          : null
      }
    >
      <AuthStepForm
        onSubmit={handleSubmit}
        aria-labelledby="login-title"
        aria-busy={isSubmitting}
        actions={
          <ActionButton
            type="submit"
            variant="primary"
            aria-busy={isSubmitting}
            disabled={isSubmitting}
          >
            {isSubmitting
<<<<<<< HEAD
              ? t('auth.actions.submitting', {
                  defaultValue: 'Signing in...',
                })
              : t('auth.actions.continue')}
=======
              ? t("auth.actions.submitting", {
                  defaultValue: "Signing in...",
                })
              : t("auth.actions.continue")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          </ActionButton>
        }
      >
        <TextInput
          ref={emailInputRef}
          id="email"
          type="email"
          name="email"
          autoComplete="email"
          inputMode="email"
          enterKeyHint="next"
          value={email}
          onChange={(event) => {
            setEmail(event.target.value);
            setFieldErrors((current) => ({
              ...current,
              email: undefined,
            }));
            setStatusMessage(null);
          }}
          error={Boolean(fieldErrors.email)}
          aria-describedby={emailDescribedBy}
          aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
<<<<<<< HEAD
          aria-invalid={fieldErrors.email ? 'true' : 'false'}
          aria-required="true"
          required
          label={t('auth.fields.email.label', {
            defaultValue: 'Email address',
          })}
          hint={t('auth.fields.email.hint', {
            defaultValue: 'Use the email address associated with your account.',
=======
          aria-invalid={fieldErrors.email ? "true" : "false"}
          aria-required="true"
          required
          label={t("auth.fields.email.label", {
            defaultValue: "Email address",
          })}
          hint={t("auth.fields.email.hint", {
            defaultValue: "Use the email address associated with your account.",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          })}
          hintId={emailHintId}
          fieldError={fieldErrors.email ? t(fieldErrors.email) : undefined}
          errorId={emailErrorId}
        />

        <PasswordField
          ref={passwordInputRef}
          id="password"
          name="password"
<<<<<<< HEAD
          label={t('auth.fields.password.label')}
=======
          label={t("auth.fields.password.label")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          value={password}
          show={showPassword}
          setShow={setShowPassword}
          onChange={(value) => {
            setPassword(value);
            setFieldErrors((current) => ({
              ...current,
              password: undefined,
            }));
            setStatusMessage(null);
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
<<<<<<< HEAD
              {t('auth.actions.resetPassword')}
            </ActionLink>
          }
          hint={t('auth.fields.password.hint')}
=======
              {t("auth.actions.resetPassword")}
            </ActionLink>
          }
          hint={t("auth.fields.password.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
                {t('auth.fields.password.capsLock', {
                  defaultValue: 'Caps Lock is on.',
=======
                {t("auth.fields.password.capsLock", {
                  defaultValue: "Caps Lock is on.",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                })}
              </p>
            ) : null
          }
          error={fieldErrors.password ? t(fieldErrors.password) : undefined}
          errorId={passwordErrorId}
<<<<<<< HEAD
          ariaLabelHide={t('auth.fields.password.hide', {
            defaultValue: 'Hide password',
          })}
          ariaLabelShow={t('auth.fields.password.show', {
            defaultValue: 'Show password',
=======
          ariaLabelHide={t("auth.fields.password.hide", {
            defaultValue: "Hide password",
          })}
          ariaLabelShow={t("auth.fields.password.show", {
            defaultValue: "Show password",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          })}
        />
      </AuthStepForm>

      <div className="mt-4">
        <ActionButton
          type="button"
          variant="secondary"
          onClick={handleEidas}
          aria-busy={isOpeningCert}
          disabled={isOpeningCert}
        >
<<<<<<< HEAD
          {isOpeningCert ? t('auth.status.certificateOpening') : t('auth.certificate.action')}
=======
          {isOpeningCert
            ? t("auth.status.certificateOpening")
            : t("auth.certificate.action")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        </ActionButton>
      </div>

      <p className="text-center text-sm text-muted">
<<<<<<< HEAD
        {t('auth.register.loginPrompt')}{' '}
=======
        {t("auth.register.loginPrompt")}{" "}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        <ActionLink
          to={appRoutes.register}
          variant="text"
          className="min-h-0 w-auto px-0 py-0 text-sm"
        >
<<<<<<< HEAD
          {t('auth.register.actions.open')}
=======
          {t("auth.register.actions.open")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        </ActionLink>
      </p>
    </AuthPage>
  );
}

export default LoginPage;
