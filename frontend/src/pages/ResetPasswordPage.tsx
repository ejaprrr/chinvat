import { useDocumentTitle } from '@/shared/lib/documentTitle';

import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { Mail } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { confirmPasswordReset, requestPasswordReset } from '@/features/auth/api';
import { useAuth } from '@/shared/auth';
import { getErrorDisplay, type ErrorDisplay } from '@/shared/api/errors';
import { useErrorDisplay } from '@/shared/hooks/useErrorDisplay';
import { isPasswordLongEnough, PASSWORD_MIN_LENGTH } from '@/shared/lib/validation/password';
import { appRoutes } from '../router/routes.ts';
import { ActionButton, ActionLink } from '@/shared/ui/Action';
import FormPage from '@/shared/ui/FormPage';
import { FlowStepForm, FormActions } from '@/shared/ui/FlowForm';
import CompletionMessage from '@/shared/ui/CompletionMessage';
import ProgressStepper from '@/shared/ui/ProgressStepper';
import LanguageSwitcher from '@/shared/ui/LanguageSwitcher';
import PasswordField from '@/shared/ui/PasswordField';
import TextInput from '@/shared/ui/TextInput';

type Step = 'email' | 'code' | 'password' | 'done';
type FieldErrors = {
  code?: string;
  confirmPassword?: string;
  email?: string;
  password?: string;
};

type StatusMessage = {
  text: string;
  tone: 'default' | 'warning';
} | null;

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const resetCodePattern = /^\d{6}$/;
function ResetPasswordPage() {
  useDocumentTitle('meta.resetPasswordPageTitle');

  const { t } = useTranslation();
  const { getDisplayMessage } = useErrorDisplay();
  const { error: authError, reportError, clearError } = useAuth();
  const [currentStep, setCurrentStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage | ErrorDisplay | string | null>(
    null,
  );

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

  const emailDescribedBy = [emailHintId, fieldErrors.email ? emailErrorId : '']
    .filter(Boolean)
    .join(' ');

  const codeDescribedBy = [codeHintId, fieldErrors.code ? codeErrorId : '']
    .filter(Boolean)
    .join(' ');

  const passwordDescribedBy = [passwordHintId, fieldErrors.password ? passwordErrorId : '']
    .filter(Boolean)
    .join(' ');

  const confirmPasswordDescribedBy = [
    confirmPasswordHintId,
    fieldErrors.confirmPassword ? confirmPasswordErrorId : '',
  ]
    .filter(Boolean)
    .join(' ');
  const clearStatus = () => {
    setStatusMessage(null);
    clearError();
  };

  const handleEmailSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setFieldErrors({
        email: t('auth.resetPassword.form.email.required'),
      });
      clearStatus();
      emailInputRef.current?.focus();
      return;
    }

    if (!emailPattern.test(trimmedEmail)) {
      setFieldErrors({
        email: t('auth.resetPassword.form.email.invalid'),
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
          tone: 'default',
          text: t('auth.resetPassword.form.codeSent', { email: trimmedEmail }),
        });
        setCurrentStep('code');
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: 'AUTH_PASSWORD_RESET_REQUEST_FAILED',
          fallbackMessage: t('auth.resetPassword.form.email.invalid'),
        });
        setFieldErrors({
          email: t('auth.resetPassword.form.email.invalid'),
        });
        setStatusMessage(detail);
        reportError(detail);
        emailInputRef.current?.focus();
      }
    })();
  };

  const handleCodeSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!code.trim()) {
      setFieldErrors({
        code: t('auth.resetPassword.form.code.required'),
      });
      clearStatus();
      codeInputRef.current?.focus();
      return;
    }

    if (!resetCodePattern.test(code.trim())) {
      setFieldErrors({
        code: t('auth.resetPassword.form.code.required'),
      });
      clearStatus();
      codeInputRef.current?.focus();
      return;
    }

    void (async () => {
      try {
        setFieldErrors({});
        clearStatus();
        setCurrentStep('password');
      } catch {
        // no-op
      }
    })();
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors: FieldErrors = {};

    if (!password) {
      nextErrors.password = t('auth.resetPassword.form.password.required');
    } else if (!isPasswordLongEnough(password)) {
      nextErrors.password = t('auth.resetPassword.form.password.length', {
        count: PASSWORD_MIN_LENGTH,
      });
    }

    if (!confirmPassword) {
      nextErrors.confirmPassword = t('auth.resetPassword.form.confirmPassword.required');
    } else if (password !== confirmPassword) {
      nextErrors.confirmPassword = t('auth.resetPassword.form.confirmPassword.mismatch');
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
        setCurrentStep('done');
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: 'AUTH_PASSWORD_RESET_CONFIRM_FAILED',
          fallbackMessage: t('auth.resetPassword.form.code.required'),
        });
        setStatusMessage(detail);
        reportError(detail);
      }
    })();
  };

  const goToEmailStep = () => {
    setFieldErrors({});
    clearStatus();
    setCurrentStep('email');
  };

  const stepOrder: Step[] = ['email', 'code', 'password', 'done'];
  const visibleStepOrder = stepOrder.filter((step) => step !== 'done');
  const visibleStepIndex =
    currentStep === 'done' ? visibleStepOrder.length - 1 : visibleStepOrder.indexOf(currentStep);

  const headerTitle =
    currentStep === 'email'
      ? t('auth.resetPassword.form.emailStepTitle')
      : currentStep === 'code'
        ? t('auth.resetPassword.form.codeStepTitle')
        : currentStep === 'password'
          ? t('auth.resetPassword.form.passwordStepTitle')
          : t('auth.resetPassword.form.doneTitle');

  const headerIntro =
    currentStep === 'email'
      ? t('auth.resetPassword.form.emailStepIntro')
      : currentStep === 'code'
        ? t('auth.resetPassword.form.codeStepIntro')
        : currentStep === 'password'
          ? t('auth.resetPassword.form.passwordStepIntro')
          : t('auth.resetPassword.form.doneIntro');
  useEffect(() => {
    if (previousStepRef.current === currentStep) {
      return;
    }

    previousStepRef.current = currentStep;
    stepHeadingRef.current?.focus();
  }, [currentStep]);

  return (
    <FormPage
      aria-labelledby="reset-password-title"
      progress={
        <div id={progressId}>
          <ProgressStepper
            steps={visibleStepOrder.map((step) => t(`auth.resetPassword.form.${step}StepTitle`))}
            currentStep={visibleStepIndex}
            totalSteps={visibleStepOrder.length}
            className="mb-2"
          />
        </div>
      }
      status={
        statusMessage || authError
          ? {
              content:
                statusMessage && typeof statusMessage !== 'string'
                  ? 'text' in statusMessage
                    ? statusMessage.text
                    : getDisplayMessage(statusMessage)
                  : authError
                    ? getDisplayMessage(authError)
                    : statusMessage || '',
              tone:
                statusMessage && typeof statusMessage !== 'string' && 'tone' in statusMessage
                  ? statusMessage.tone
                  : 'default',
            }
          : null
      }
      footer={
        <FormActions>
          <ActionLink
            to={appRoutes.login}
            variant={currentStep === 'done' ? 'primary' : 'secondary'}
          >
            {t('auth.actions.backToSignIn')}{' '}
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
      {currentStep === 'email' ? (
        <FlowStepForm
          onSubmit={handleEmailSubmit}
          actions={
            <ActionButton type="submit">{t('auth.resetPassword.form.email.submit')}</ActionButton>
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
            aria-invalid={fieldErrors.email ? 'true' : 'false'}
            required
            trailingIcon={<Mail aria-hidden="true" size={16} />}
            htmlFor="reset-email"
            label={t('auth.resetPassword.form.email.label')}
            hint={t('auth.resetPassword.form.email.hint')}
            hintId={emailHintId}
            fieldError={fieldErrors.email}
            errorId={emailErrorId}
          />
        </FlowStepForm>
      ) : null}

      {currentStep === 'code' ? (
        <FlowStepForm
          onSubmit={handleCodeSubmit}
          actions={
            <>
              <ActionButton type="submit">{t('auth.resetPassword.form.code.submit')}</ActionButton>
              <ActionButton type="button" variant="secondary" onClick={goToEmailStep}>
                {t('auth.resetPassword.form.code.changeEmail')}{' '}
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
            aria-invalid={fieldErrors.code ? 'true' : 'false'}
            required
            label={t('auth.resetPassword.form.code.label')}
            hint={t('auth.resetPassword.form.code.hint')}
            hintId={codeHintId}
            fieldError={fieldErrors.code}
            errorId={codeErrorId}
          />
        </FlowStepForm>
      ) : null}

      {currentStep === 'password' ? (
        <FlowStepForm
          onSubmit={handlePasswordSubmit}
          actions={
            <>
              <ActionButton type="submit">
                {t('auth.resetPassword.form.password.submit')}{' '}
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
                onClick={() => {
                  setFieldErrors({});
                  clearStatus();
                  setCurrentStep('code');
                }}
              >
                {t('auth.resetPassword.form.password.back')}{' '}
              </ActionButton>
            </>
          }
        >
          <PasswordField
            ref={passwordInputRef}
            id="new-password"
            name="newPassword"
            label={t('auth.resetPassword.form.password.label')}
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
            hint={t('auth.resetPassword.form.password.hint')}
            hintId={passwordHintId}
            error={fieldErrors.password}
            errorId={passwordErrorId}
            ariaLabelHide={t('auth.fields.password.hide')}
            ariaLabelShow={t('auth.fields.password.show')}
          />

          <PasswordField
            ref={confirmPasswordInputRef}
            id="confirm-password"
            name="confirmPassword"
            label={t('auth.resetPassword.form.confirmPassword.label')}
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
            hint={t('auth.resetPassword.form.confirmPassword.hint')}
            hintId={confirmPasswordHintId}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
            ariaLabelHide={t('auth.fields.password.hide')}
            ariaLabelShow={t('auth.fields.password.show')}
          />
        </FlowStepForm>
      ) : null}

      {currentStep === 'done' ? (
        <CompletionMessage id="reset-complete-title">
          {t('auth.resetPassword.form.doneBody')}{' '}
        </CompletionMessage>
      ) : null}
    </FormPage>
  );
}

export default ResetPasswordPage;
