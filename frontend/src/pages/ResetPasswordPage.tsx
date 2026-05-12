import { useDocumentTitle } from "../lib/documentTitle";

import { useEffect, useId, useRef, useState, type FormEvent } from "react";
import { Mail } from "lucide-react";
import { useTranslation } from "react-i18next";
import { confirmPasswordReset, requestPasswordReset } from "../lib/api";
import { useAuth } from "../contexts/auth";
import { getErrorDisplay } from "../lib/http/errors";
import {
  isPasswordLongEnough,
  PASSWORD_MIN_LENGTH,
} from "../lib/validation/password";
import { appRoutes } from "../router/routes.ts";
import { ActionButton, ActionLink } from "../components/forms/Action";
import AuthPage from "../components/auth/AuthPage";
import { AuthStepForm, FormActions } from "../components/auth/AuthForm";
import { AuthCompletion } from "../components/auth/AuthSupport";
import Stepper from "../components/auth/Stepper";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import PasswordField from "../components/forms/PasswordField";
import TextInput from "../components/forms/TextInput";

type Step = "email" | "code" | "password" | "done";

type FieldErrors = {
  code?: string;
  confirmPassword?: string;
  email?: string;
  password?: string;
};

type StatusMessage = {
  text: string;
  tone: "default" | "warning";
} | null;

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const resetCodePattern = /^\d{6}$/;
function ResetPasswordPage() {
  useDocumentTitle("meta.resetPasswordPageTitle");

  const { t } = useTranslation();
  const { error: authError, reportError, clearError } = useAuth();
  const [currentStep, setCurrentStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage>(null);

  const emailInputRef = useRef<HTMLInputElement>(null);
  const codeInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);
  const confirmPasswordInputRef = useRef<HTMLInputElement>(null);
  const stepHeadingRef = useRef<HTMLHeadingElement>(null);
  const previousStepRef = useRef<Step>(currentStep);

  const progressId = useId();
  const headerIntroId = useId();
  const emailHintId = useId();
  const codeHintId = useId();
  const passwordHintId = useId();
  const confirmPasswordHintId = useId();
  const emailErrorId = useId();
  const codeErrorId = useId();
  const passwordErrorId = useId();
  const confirmPasswordErrorId = useId();

  const emailDescribedBy = [emailHintId, fieldErrors.email ? emailErrorId : ""]
    .filter(Boolean)
    .join(" ");

  const codeDescribedBy = [codeHintId, fieldErrors.code ? codeErrorId : ""]
    .filter(Boolean)
    .join(" ");

  const passwordDescribedBy = [
    passwordHintId,
    fieldErrors.password ? passwordErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");

  const confirmPasswordDescribedBy = [
    confirmPasswordHintId,
    fieldErrors.confirmPassword ? confirmPasswordErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");

  const clearStatus = () => {
    setStatusMessage(null);
    clearError();
  };

  const handleEmailSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setFieldErrors({
        email: t("auth.resetPassword.form.email.required"),
      });
      clearStatus();
      emailInputRef.current?.focus();
      return;
    }

    if (!emailPattern.test(trimmedEmail)) {
      setFieldErrors({
        email: t("auth.resetPassword.form.email.invalid"),
      });
      clearStatus();
      emailInputRef.current?.focus();
      return;
    }

    void (async () => {
      try {
        await requestPasswordReset({ email: trimmedEmail });
        setFieldErrors({});
        setStatusMessage({
          tone: "default",
          text: t("auth.resetPassword.form.codeSent", { email: trimmedEmail }),
        });
        setCurrentStep("code");
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: "AUTH_PASSWORD_RESET_REQUEST_FAILED",
          fallbackMessage: t("auth.resetPassword.form.email.invalid"),
        });
        setFieldErrors({
          email: t("auth.resetPassword.form.email.invalid"),
        });
        setStatusMessage({
          tone: "warning",
          text: detail.message,
        });
        reportError(detail.message);
        emailInputRef.current?.focus();
      }
    })();
  };

  const handleCodeSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!code.trim()) {
      setFieldErrors({
        code: t("auth.resetPassword.form.code.required"),
      });
      clearStatus();
      codeInputRef.current?.focus();
      return;
    }

    if (!resetCodePattern.test(code.trim())) {
      setFieldErrors({
        code: t("auth.resetPassword.form.code.required"),
      });
      clearStatus();
      codeInputRef.current?.focus();
      return;
    }

    void (async () => {
      try {
        setFieldErrors({});
        clearStatus();
        setCurrentStep("password");
      } catch {
        // no-op
      }
    })();
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};

    if (!password) {
      nextErrors.password = t("auth.resetPassword.form.password.required");
    } else if (!isPasswordLongEnough(password)) {
      nextErrors.password = t("auth.resetPassword.form.password.length", {
        count: PASSWORD_MIN_LENGTH,
      });
    }

    if (!confirmPassword) {
      nextErrors.confirmPassword = t(
        "auth.resetPassword.form.confirmPassword.required",
      );
    } else if (password !== confirmPassword) {
      nextErrors.confirmPassword = t(
        "auth.resetPassword.form.confirmPassword.mismatch",
      );
    }

    setFieldErrors(nextErrors);

    if (nextErrors.password) {
      clearStatus();
      passwordInputRef.current?.focus();
      return;
    }

    if (nextErrors.confirmPassword) {
      clearStatus();
      confirmPasswordInputRef.current?.focus();
      return;
    }

    void (async () => {
      try {
        await confirmPasswordReset({
          email: email.trim(),
          resetCode: code.trim(),
          newPassword: password,
        });
        clearStatus();
        setCurrentStep("done");
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: "AUTH_PASSWORD_RESET_CONFIRM_FAILED",
          fallbackMessage: t("auth.resetPassword.form.code.required"),
        });
        setStatusMessage({
          tone: "warning",
          text: detail.message,
        });
        reportError(detail.message);
      }
    })();
  };

  const goToEmailStep = () => {
    setFieldErrors({});
    clearStatus();
    setCurrentStep("email");
  };

  const stepOrder: Step[] = ["email", "code", "password", "done"];
  const visibleStepOrder = stepOrder.filter((step) => step !== "done");
  const visibleStepIndex =
    currentStep === "done"
      ? visibleStepOrder.length - 1
      : visibleStepOrder.indexOf(currentStep);

  const headerTitle =
    currentStep === "email"
      ? t("auth.resetPassword.form.emailStepTitle")
      : currentStep === "code"
        ? t("auth.resetPassword.form.codeStepTitle")
        : currentStep === "password"
          ? t("auth.resetPassword.form.passwordStepTitle")
          : t("auth.resetPassword.form.doneTitle");

  const headerIntro =
    currentStep === "email"
      ? t("auth.resetPassword.form.emailStepIntro")
      : currentStep === "code"
        ? t("auth.resetPassword.form.codeStepIntro")
        : currentStep === "password"
          ? t("auth.resetPassword.form.passwordStepIntro")
          : t("auth.resetPassword.form.doneIntro");

  useEffect(() => {
    if (previousStepRef.current === currentStep) {
      return;
    }

    previousStepRef.current = currentStep;
    stepHeadingRef.current?.focus();
  }, [currentStep]);

  return (
    <AuthPage
      aria-labelledby="reset-password-title"
      progress={
        <div id={progressId}>
          <Stepper
            steps={visibleStepOrder.map((step) =>
              t(`auth.resetPassword.form.${step}StepTitle`),
            )}
            currentStep={visibleStepIndex}
            totalSteps={visibleStepOrder.length}
            className="mb-2"
          />
        </div>
      }
      status={
        statusMessage || authError
          ? {
              content: statusMessage?.text || authError,
              tone: statusMessage?.tone === "warning" ? "warning" : "default",
            }
          : null
      }
      footer={
        <FormActions>
          <ActionLink
            to={appRoutes.login}
            variant={currentStep === "done" ? "primary" : "secondary"}
          >
            {t("auth.actions.backToSignIn")}
          </ActionLink>
        </FormActions>
      }
      action={
        <div className="auth-floating-language">
          <LanguageSwitcher />
        </div>
      }
      titleId="reset-password-title"
      introId={headerIntroId}
      title={headerTitle}
      titleRef={stepHeadingRef}
      titleTabIndex={-1}
      titleDescribedBy={`${progressId} ${headerIntroId}`}
      intro={headerIntro}
    >
      {currentStep === "email" ? (
        <AuthStepForm
          onSubmit={handleEmailSubmit}
          actions={
            <ActionButton type="submit">
              {t("auth.resetPassword.form.email.submit")}
            </ActionButton>
          }
        >
          <TextInput
            ref={emailInputRef}
            id="reset-email"
            type="email"
            name="email"
            autoComplete="email"
            inputMode="email"
            enterKeyHint="send"
            value={email}
            onChange={(event) => {
              setEmail(event.target.value);
              setFieldErrors((current) => ({
                ...current,
                email: undefined,
              }));
              clearStatus();
            }}
            error={Boolean(fieldErrors.email)}
            aria-describedby={emailDescribedBy}
            aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
            aria-invalid={fieldErrors.email ? "true" : "false"}
            required
            trailingIcon={<Mail aria-hidden="true" size={16} />}
            htmlFor="reset-email"
            label={t("auth.resetPassword.form.email.label")}
            hint={t("auth.resetPassword.form.email.hint")}
            hintId={emailHintId}
            fieldError={fieldErrors.email}
            errorId={emailErrorId}
          />
        </AuthStepForm>
      ) : null}

      {currentStep === "code" ? (
        <AuthStepForm
          onSubmit={handleCodeSubmit}
          actions={
            <>
              <ActionButton type="submit">
                {t("auth.resetPassword.form.code.submit")}
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
                onClick={goToEmailStep}
              >
                {t("auth.resetPassword.form.code.changeEmail")}
              </ActionButton>
            </>
          }
        >
          <TextInput
            ref={codeInputRef}
            id="reset-code"
            type="text"
            name="code"
            autoComplete="one-time-code"
            inputMode="numeric"
            enterKeyHint="next"
            value={code}
            onChange={(event) => {
              setCode(event.target.value);
              setFieldErrors((current) => ({
                ...current,
                code: undefined,
              }));
              clearStatus();
            }}
            error={Boolean(fieldErrors.code)}
            aria-describedby={codeDescribedBy}
            aria-errormessage={fieldErrors.code ? codeErrorId : undefined}
            aria-invalid={fieldErrors.code ? "true" : "false"}
            required
            label={t("auth.resetPassword.form.code.label")}
            hint={t("auth.resetPassword.form.code.hint")}
            hintId={codeHintId}
            fieldError={fieldErrors.code}
            errorId={codeErrorId}
          />
        </AuthStepForm>
      ) : null}

      {currentStep === "password" ? (
        <AuthStepForm
          onSubmit={handlePasswordSubmit}
          actions={
            <>
              <ActionButton type="submit">
                {t("auth.resetPassword.form.password.submit")}
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
                onClick={() => {
                  setFieldErrors({});
                  clearStatus();
                  setCurrentStep("code");
                }}
              >
                {t("auth.resetPassword.form.password.back")}
              </ActionButton>
            </>
          }
        >
          <PasswordField
            ref={passwordInputRef}
            id="new-password"
            name="newPassword"
            label={t("auth.resetPassword.form.password.label")}
            value={password}
            show={showPassword}
            setShow={setShowPassword}
            onChange={(value) => {
              setPassword(value);
              setFieldErrors((current) => ({
                ...current,
                password: undefined,
              }));
              clearStatus();
            }}
            autoComplete="new-password"
            enterKeyHint="next"
            aria-describedby={passwordDescribedBy}
            required
            hint={t("auth.resetPassword.form.password.hint")}
            hintId={passwordHintId}
            error={fieldErrors.password}
            errorId={passwordErrorId}
            ariaLabelHide={t("auth.fields.password.hide")}
            ariaLabelShow={t("auth.fields.password.show")}
          />

          <PasswordField
            ref={confirmPasswordInputRef}
            id="confirm-password"
            name="confirmPassword"
            label={t("auth.resetPassword.form.confirmPassword.label")}
            value={confirmPassword}
            show={showConfirmPassword}
            setShow={setShowConfirmPassword}
            onChange={(value) => {
              setConfirmPassword(value);
              setFieldErrors((current) => ({
                ...current,
                confirmPassword: undefined,
              }));
              clearStatus();
            }}
            autoComplete="new-password"
            enterKeyHint="done"
            aria-describedby={confirmPasswordDescribedBy}
            required
            hint={t("auth.resetPassword.form.confirmPassword.hint")}
            hintId={confirmPasswordHintId}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
            ariaLabelHide={t("auth.fields.password.hide")}
            ariaLabelShow={t("auth.fields.password.show")}
          />
        </AuthStepForm>
      ) : null}

      {currentStep === "done" ? (
        <AuthCompletion id="reset-complete-title">
          {t("auth.resetPassword.form.doneBody")}
        </AuthCompletion>
      ) : null}
    </AuthPage>
  );
}

export default ResetPasswordPage;
