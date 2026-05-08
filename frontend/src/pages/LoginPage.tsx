import {
  useEffect,
  useId,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../hooks/useDocumentTitle";
import { useAuth } from "../contexts/auth";
import { appRoutes } from "../router";
import { ActionButton, ActionLink } from "../components/forms/Action";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import AuthPage from "../components/auth/AuthPage";
import { AuthStepForm } from "../components/auth/AuthForm";
import PasswordField from "../components/forms/PasswordField";
import TextInput from "../components/forms/TextInput";

type FieldErrors = {
  email?: string;
  password?: string;
};

function LoginPage() {
  useDocumentTitle("meta.pageTitle");

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [capsLockOn, setCapsLockOn] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const emailInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const emailHintId = useId();
  const passwordHintId = useId();
  const emailErrorId = useId();
  const passwordErrorId = useId();
  const capsLockWarningId = useId();

  useEffect(() => {
    // Avoid setting state synchronously in the effect body
    Promise.resolve().then(() => setStatusMessage(null));
  }, [email, password]);

  const handleCapsLock = (event: KeyboardEvent<HTMLInputElement>) => {
    setCapsLockOn(event.getModifierState("CapsLock"));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      nextErrors.email = "auth.fields.email.required";
    }

    if (!password) {
      nextErrors.password = "auth.fields.password.required";
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
      setStatusMessage(
        error instanceof Error ? error.message : t("auth.status.loginError"),
      );
    } finally {
      setIsSubmitting(false);
    }
  };

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
      intro={t("auth.intro", {
        defaultValue: "Use your email and password to sign in.",
      })}
      status={
        statusMessage
          ? {
              content: statusMessage,
              tone: "critical",
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
              ? t("auth.actions.submitting", {
                  defaultValue: "Signing in...",
                })
              : t("auth.actions.continue")}
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
          }}
          error={Boolean(fieldErrors.email)}
          aria-describedby={emailDescribedBy}
          aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
          aria-invalid={fieldErrors.email ? "true" : "false"}
          aria-required="true"
          required
          label={t("auth.fields.email.label", {
            defaultValue: "Email address",
          })}
          hint={t("auth.fields.email.hint", {
            defaultValue: "Use the email address associated with your account.",
          })}
          hintId={emailHintId}
          fieldError={fieldErrors.email ? t(fieldErrors.email) : undefined}
          errorId={emailErrorId}
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
    </AuthPage>
  );
}

export default LoginPage;
