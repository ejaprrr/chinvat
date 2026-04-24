import {
  useEffect,
  useId,
  useRef,
  useState,
  type FormEvent,
} from "react";
import { Eye, EyeOff, Mail, ShieldCheck } from "lucide-react";
import { useTranslation } from "react-i18next";
import { cx } from "../lib/cx";
import { appRoutes } from "../router/paths";
import {
  ActionButton,
  ActionLink,
} from "./ui/Action";
import AuthPageHeader from "./ui/AuthPageHeader";
import FormField from "./ui/FormField";

type Step = "email" | "code" | "password" | "done";

type FieldErrors = {
  code?: string;
  confirmPassword?: string;
  email?: string;
  password?: string;
};

type StatusMessage =
  | {
      text: string;
      tone: "default" | "warning";
    }
  | null;

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const passwordMinLength = 8;

const styles = {
  root: "flex flex-col gap-5",
  progressText:
    "text-[0.6875rem] font-semibold uppercase tracking-[0.12em] text-muted-soft",
  form: "flex flex-col gap-4",
  inputBase:
    "block min-h-11 w-full rounded-lg border bg-white px-3.5 py-3 text-sm text-ink outline-none transition disabled:cursor-not-allowed disabled:opacity-60",
  inputDefault:
    "border-border-subtle focus:border-brand-500 focus:ring-3 focus:ring-brand-500/15",
  inputError:
    "border-danger-200 focus:border-danger-700 focus:ring-3 focus:ring-danger-700/10",
  iconButton:
    "absolute inset-y-0 right-0 inline-flex min-h-11 min-w-11 items-center justify-center rounded-r-lg px-3 text-muted transition hover:bg-surface-hover hover:text-ink focus-visible:z-10 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  statusMessage: "text-[0.8125rem] leading-5",
  statusMessageDefault: "text-muted",
  statusMessageWarning: "text-warning-ink",
  completion: "flex items-start gap-2 text-[0.8125rem] leading-5 text-muted",
  completionIcon: "mt-0.5 shrink-0 text-brand-600",
  actions: "flex flex-col gap-2.5",
} as const;

function getInputClassName(hasError: boolean, hasTrailingButton = false) {
  return cx(
    styles.inputBase,
    hasTrailingButton ? "pr-12" : "",
    hasError ? styles.inputError : styles.inputDefault,
  );
}

function ResetPasswordForm() {
  const { t } = useTranslation();
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

  const emailDescribedBy = [
    emailHintId,
    fieldErrors.email ? emailErrorId : "",
  ]
    .filter(Boolean)
    .join(" ");

  const codeDescribedBy = [
    codeHintId,
    fieldErrors.code ? codeErrorId : "",
  ]
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

    setFieldErrors({});
    setStatusMessage({
      tone: "default",
      text: t("auth.resetPassword.form.codeSent", { email: trimmedEmail }),
    });
    setCurrentStep("code");
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

    setFieldErrors({});
    clearStatus();
    setCurrentStep("password");
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};

    if (!password) {
      nextErrors.password = t("auth.resetPassword.form.password.required");
    } else if (password.length < passwordMinLength) {
      nextErrors.password = t("auth.resetPassword.form.password.length", {
        count: passwordMinLength,
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

    clearStatus();
    setCurrentStep("done");
  };

  const goToEmailStep = () => {
    setFieldErrors({});
    clearStatus();
    setCurrentStep("email");
  };

  const stepOrder: Step[] = ["email", "code", "password", "done"];
  const currentStepIndex = stepOrder.indexOf(currentStep);
  const activeStepNumber = currentStep === "done" ? 3 : currentStepIndex + 1;

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
    <div className={styles.root} aria-labelledby="reset-password-title">
      <p id={progressId} className={styles.progressText}>
        {t("auth.resetPassword.progress.current", {
          current: activeStepNumber,
          total: 3,
        })}
      </p>

      <AuthPageHeader
        id="reset-password-title"
        introId={headerIntroId}
        title={headerTitle}
        titleRef={stepHeadingRef}
        titleTabIndex={-1}
        titleDescribedBy={`${progressId} ${headerIntroId}`}
        intro={headerIntro}
      />

      <div
        className={statusMessage ? "block" : "hidden"}
        aria-hidden={statusMessage ? undefined : "true"}
      >
        {statusMessage ? (
          <p
            className={cx(
              styles.statusMessage,
              statusMessage.tone === "warning"
                ? styles.statusMessageWarning
                : styles.statusMessageDefault,
            )}
            role={statusMessage.tone === "warning" ? "alert" : "status"}
            aria-live={statusMessage.tone === "warning" ? "assertive" : "polite"}
            aria-atomic="true"
          >
            {statusMessage.text}
          </p>
        ) : null}
      </div>

      {currentStep === "email" ? (
        <form className={styles.form} noValidate onSubmit={handleEmailSubmit}>
          <FormField
            htmlFor="reset-email"
            label={t("auth.resetPassword.form.email.label")}
            hint={t("auth.resetPassword.form.email.hint")}
            hintId={emailHintId}
            error={fieldErrors.email}
            errorId={emailErrorId}
          >
            <div className="relative">
              <input
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
                className={getInputClassName(Boolean(fieldErrors.email), true)}
                aria-describedby={emailDescribedBy}
                aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
                aria-invalid={fieldErrors.email ? "true" : "false"}
                required
              />
              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-muted">
                <Mail aria-hidden="true" size={16} />
              </span>
            </div>
          </FormField>

          <div className={styles.actions}>
            <ActionButton type="submit">
              {t("auth.resetPassword.form.email.submit")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "code" ? (
        <form className={styles.form} noValidate onSubmit={handleCodeSubmit}>
          <FormField
            htmlFor="reset-code"
            label={t("auth.resetPassword.form.code.label")}
            hint={t("auth.resetPassword.form.code.hint")}
            hintId={codeHintId}
            error={fieldErrors.code}
            errorId={codeErrorId}
          >
            <input
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
              className={getInputClassName(Boolean(fieldErrors.code))}
              aria-describedby={codeDescribedBy}
              aria-errormessage={fieldErrors.code ? codeErrorId : undefined}
              aria-invalid={fieldErrors.code ? "true" : "false"}
              required
            />
          </FormField>

          <div className={styles.actions}>
            <ActionButton type="submit">{t("auth.resetPassword.form.code.submit")}</ActionButton>
            <ActionButton
              type="button"
              variant="secondary"
              onClick={goToEmailStep}
            >
              {t("auth.resetPassword.form.code.changeEmail")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "password" ? (
        <form className={styles.form} noValidate onSubmit={handlePasswordSubmit}>
          <FormField
            htmlFor="new-password"
            label={t("auth.resetPassword.form.password.label")}
            hint={t("auth.resetPassword.form.password.hint")}
            hintId={passwordHintId}
            error={fieldErrors.password}
            errorId={passwordErrorId}
          >
            <div className="relative">
              <input
                ref={passwordInputRef}
                id="new-password"
                type={showPassword ? "text" : "password"}
                name="newPassword"
                autoComplete="new-password"
                enterKeyHint="next"
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value);
                  setFieldErrors((current) => ({
                    ...current,
                    password: undefined,
                  }));
                  clearStatus();
                }}
                className={getInputClassName(Boolean(fieldErrors.password), true)}
                aria-describedby={passwordDescribedBy}
                aria-errormessage={
                  fieldErrors.password ? passwordErrorId : undefined
                }
                aria-invalid={fieldErrors.password ? "true" : "false"}
                required
              />
              <button
                type="button"
                className={styles.iconButton}
                onClick={() => setShowPassword((value) => !value)}
                aria-pressed={showPassword}
                aria-label={
                  showPassword
                    ? t("auth.fields.password.hide")
                    : t("auth.fields.password.show")
                }
                aria-controls="new-password"
              >
                {showPassword ? (
                  <EyeOff size={16} aria-hidden="true" />
                ) : (
                  <Eye size={16} aria-hidden="true" />
                )}
              </button>
            </div>
          </FormField>

          <FormField
            htmlFor="confirm-password"
            label={t("auth.resetPassword.form.confirmPassword.label")}
            hint={t("auth.resetPassword.form.confirmPassword.hint")}
            hintId={confirmPasswordHintId}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
          >
            <div className="relative">
              <input
                ref={confirmPasswordInputRef}
                id="confirm-password"
                type={showConfirmPassword ? "text" : "password"}
                name="confirmPassword"
                autoComplete="new-password"
                enterKeyHint="done"
                value={confirmPassword}
                onChange={(event) => {
                  setConfirmPassword(event.target.value);
                  setFieldErrors((current) => ({
                    ...current,
                    confirmPassword: undefined,
                  }));
                  clearStatus();
                }}
                className={getInputClassName(
                  Boolean(fieldErrors.confirmPassword),
                  true,
                )}
                aria-describedby={confirmPasswordDescribedBy}
                aria-errormessage={
                  fieldErrors.confirmPassword
                    ? confirmPasswordErrorId
                    : undefined
                }
                aria-invalid={fieldErrors.confirmPassword ? "true" : "false"}
                required
              />
              <button
                type="button"
                className={styles.iconButton}
                onClick={() => setShowConfirmPassword((value) => !value)}
                aria-pressed={showConfirmPassword}
                aria-label={
                  showConfirmPassword
                    ? t("auth.fields.password.hide")
                    : t("auth.fields.password.show")
                }
                aria-controls="confirm-password"
              >
                {showConfirmPassword ? (
                  <EyeOff size={16} aria-hidden="true" />
                ) : (
                  <Eye size={16} aria-hidden="true" />
                )}
              </button>
            </div>
          </FormField>

          <div className={styles.actions}>
            <ActionButton type="submit">{t("auth.resetPassword.form.password.submit")}</ActionButton>
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
          </div>
        </form>
      ) : null}

      {currentStep === "done" ? (
        <p
          id="reset-complete-title"
          className={styles.completion}
          role="status"
          aria-live="polite"
          aria-atomic="true"
        >
          <ShieldCheck
            aria-hidden="true"
            size={16}
            className={styles.completionIcon}
          />
          <span>{t("auth.resetPassword.form.doneBody")}</span>
        </p>
      ) : null}

      <div className={styles.actions}>
        <ActionLink
          to={appRoutes.login}
          variant={currentStep === "done" ? "primary" : "secondary"}
        >
          {t("auth.actions.backToSignIn")}
        </ActionLink>
      </div>
    </div>
  );
}

export default ResetPasswordForm;
