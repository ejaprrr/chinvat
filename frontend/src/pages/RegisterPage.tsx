import { useDocumentTitle } from '@/shared/lib/documentTitle';
import {
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from 'react';
import { useNavigate } from 'react-router';
import { MapPin, UserRound } from 'lucide-react';
import { getCountries, getCountryCallingCode, type CountryCode } from 'libphonenumber-js/min';
import { useTranslation } from 'react-i18next';
import { languageLabels, type Locale } from '@/shared/i18n';
import { useErrorDisplay } from '@/shared/hooks/useErrorDisplay';
import type { ErrorDisplay } from '@/shared/api/errors';
import { appRoutes } from '../router/routes.ts';
import { ActionButton, ActionLink } from '@/shared/ui/Action';
import { FlowStepForm, FormActions } from '@/shared/ui/FlowForm';
import FormPage from '@/shared/ui/FormPage';
import CompletionMessage from '@/shared/ui/CompletionMessage';
import ProgressStepper from '@/shared/ui/ProgressStepper';
import LanguageSwitcher from '@/shared/ui/LanguageSwitcher';
import LocationLookup, { type LocationSuggestion } from '@/shared/ui/LocationLookup';
import PasswordField from '@/shared/ui/PasswordField';
import PhoneNumberField from '@/shared/ui/PhoneNumberField';
import type { PhoneCountryOption } from '@/shared/ui/PhoneCountrySelect';
import FormField from '@/shared/ui/FormField';
import TextInput from '@/shared/ui/TextInput';
import { useAuth } from '@/shared/auth';
import { useGeocoding } from '@/shared/hooks/useGeocoding';
import { getErrorDisplay } from '@/shared/api/errors';
import { isPasswordLongEnough, PASSWORD_MIN_LENGTH } from '@/shared/lib/validation/password';
import {
  EMAIL_MAX_LENGTH,
  FULL_NAME_MAX_LENGTH,
  PHONE_MAX_LENGTH,
  USERNAME_MAX_LENGTH,
  isWellFormedEmail,
} from '@/shared/lib/validation/user';

type Step = 'identity' | 'contact' | 'security' | 'done';
type FormValues = {
  username: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  defaultLanguage: Locale;
  password: string;
  confirmPassword: string;
};

type FieldErrors = Partial<Record<keyof FormValues | 'location', string>>;

type StatusMessage = {
  text: string;
  tone: 'default' | 'critical';
} | null;

const initialValues: FormValues = {
  username: '',
  fullName: '',
  phoneNumber: '',
  email: '',
  defaultLanguage: 'en',
  password: '',
  confirmPassword: '',
};

function countryCodeToFlag(countryCode: CountryCode) {
  return countryCode.replace(/./g, (char) => String.fromCodePoint(127397 + char.charCodeAt(0)));
}

function buildPhoneCountryOptions(language: string) {
  const displayNames = new Intl.DisplayNames([language, 'en'], {
    type: 'region',
  });

  return getCountries()
    .map((countryCode) => ({
      code: countryCode,
      dialCode: `+${getCountryCallingCode(countryCode)}`,
      flag: countryCodeToFlag(countryCode),
      label: displayNames.of(countryCode) || countryCode,
    }))
    .sort((left, right) => left.label.localeCompare(right.label));
}

function getDefaultPhoneCountry(language: string | undefined): CountryCode {
  try {
    const locale = new Intl.Locale(language || 'en').maximize();
    const region = locale.region as CountryCode | undefined;

    if (region && getCountries().includes(region)) {
      return region;
    }
  } catch {
    return 'ES';
  }

  return 'ES';
}

function RegisterPage() {
  useDocumentTitle('meta.registerPageTitle');
  const { t, i18n } = useTranslation();
  const { getDisplayMessage } = useErrorDisplay();
  const navigate = useNavigate();
  const { register, error: authError, reportError, clearError } = useAuth();
  const uid = useId();

  // ── IDs for accessibility ──────────────────────────────────────────────────
  const progressId = `${uid}-progress`;
  const headerIntroId = `${uid}-header-intro`;
  const usernameHintId = `${uid}-username-hint`;
  const usernameErrorId = `${uid}-username-error`;
  const fullNameHintId = `${uid}-fullname-hint`;
  const fullNameErrorId = `${uid}-fullname-error`;
  const phoneHintId = `${uid}-phone-hint`;
  const phoneCountryControlId = `${uid}-phone-country-control`;
  const phoneCountryHintId = `${uid}-phone-country-hint`;
  const emailHintId = `${uid}-email-hint`;
  const emailErrorId = `${uid}-email-error`;
  const locationHintId = `${uid}-location-hint`;
  const locationErrorId = `${uid}-location-error`;
  const locationStatusId = `${uid}-location-status`;
  const locationListId = `${uid}-location-list`;
  const defaultLanguageHintId = `${uid}-default-language-hint`;
  const passwordHintId = `${uid}-password-hint`;
  const passwordErrorId = `${uid}-password-error`;
  const confirmPasswordHintId = `${uid}-confirm-password-hint`;
  const confirmPasswordErrorId = `${uid}-confirm-password-error`;

  // ── Refs ───────────────────────────────────────────────────────────────────
  const stepHeadingRef = useRef<HTMLHeadingElement>(null);
  const previousStepRef = useRef<Step | null>(null);
  const usernameInputRef = useRef<HTMLInputElement>(null);
  const emailInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  // ── State ──────────────────────────────────────────────────────────────────
  const [currentStep, setCurrentStep] = useState<Step>('identity');
  const [values, setValues] = useState<FormValues>(() => ({
    ...initialValues,
    defaultLanguage:
      i18n.resolvedLanguage && i18n.resolvedLanguage in languageLabels
        ? (i18n.resolvedLanguage as Locale)
        : 'en',
  }));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage | ErrorDisplay | string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Location
  const [locationQuery, setLocationQuery] = useState('');
  const [resolvedLocation, setResolvedLocation] = useState<LocationSuggestion | null>(null);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

  // Password visibility
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Phone country
  const [phoneCountryCode, setPhoneCountryCode] = useState<CountryCode>(() =>
    getDefaultPhoneCountry(i18n.resolvedLanguage || i18n.language),
  );

  // ── Derived values ─────────────────────────────────────────────────────────
  const stepOrder: Step[] = ['identity', 'contact', 'security', 'done'];
  const currentStepIndex = stepOrder.indexOf(currentStep);

  const languageOptions = useMemo(
    () =>
      Object.entries(languageLabels).map(([value, label]) => ({
        value: value as Locale,
        label,
      })),
    [],
  );

  const phoneCountryOptions = useMemo(
    () => buildPhoneCountryOptions(i18n.resolvedLanguage || i18n.language || 'en'),
    [i18n.language, i18n.resolvedLanguage],
  );

  const phoneCountryOptionsByCode = useMemo(
    () =>
      Object.fromEntries(phoneCountryOptions.map((option) => [option.code, option])) as Record<
        string,
        PhoneCountryOption
      >,
    [phoneCountryOptions],
  );

  const selectedPhoneCountry =
    phoneCountryOptionsByCode[phoneCountryCode] ?? phoneCountryOptions[0];

  const geocodingQuery =
    resolvedLocation && locationQuery.trim() === resolvedLocation.displayName ? '' : locationQuery;
  const {
    data: locationSuggestions,
    error: locationLookupError,
    loading: locationLookupLoading,
  } = useGeocoding(geocodingQuery);

  // ── Helpers ────────────────────────────────────────────────────────────────
  const clearStatus = () => setStatusMessage(null);

  const formatLocationPreview = (loc: LocationSuggestion) => {
    if (!loc) return '';
    const parts: string[] = [];
    if (loc.address) parts.push(loc.address);
    if (loc.city && !parts.includes(loc.city)) parts.push(loc.city);
    if (loc.country) parts.push(loc.country);
    return parts.join(', ');
  };

  const setFieldValue = (field: keyof FormValues, value: string) => {
    setValues((prev) => ({ ...prev, [field]: value }));
    setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    clearStatus();
    clearError();
  };

  const goToStep = (step: Step) => {
    setFieldErrors({});
    clearStatus();
    setCurrentStep(step);
  };

  const getFieldDescribedBy = (hintId: string, error?: string, errorId?: string) =>
    [hintId, error ? errorId : ''].filter(Boolean).join(' ');

  const getLocationSuggestionId = (index: number) => `${locationListId}-option-${index}`;
  // ── Validation ─────────────────────────────────────────────────────────────
  const validateIdentityStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.username.trim()) {
      errors.username = t('auth.register.errors.usernameRequired');
    } else if (values.username.trim().length > USERNAME_MAX_LENGTH) {
      errors.username = t('auth.register.errors.usernameRequired');
    }
    if (!values.fullName.trim()) {
      errors.fullName = t('auth.register.errors.fullNameRequired');
    } else if (values.fullName.trim().length > FULL_NAME_MAX_LENGTH) {
      errors.fullName = t('auth.register.errors.fullNameRequired');
    }
    if (values.phoneNumber.trim().length > PHONE_MAX_LENGTH) {
      errors.phoneNumber = t('auth.register.errors.phoneRequired');
    }
    return errors;
  };

  const validateContactStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.email.trim()) {
      errors.email = t('auth.register.errors.emailRequired');
    } else if (
      !isWellFormedEmail(values.email.trim()) ||
      values.email.trim().length > EMAIL_MAX_LENGTH
    ) {
      errors.email = t('auth.register.errors.emailInvalid');
    }

    return errors;
  };

  const validateSecurityStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.password) {
      errors.password = t('auth.register.errors.passwordRequired');
    } else if (!isPasswordLongEnough(values.password)) {
      errors.password = t('auth.register.fields.password.length', { count: PASSWORD_MIN_LENGTH });
    }
    if (!values.confirmPassword) {
      errors.confirmPassword = t('auth.register.fields.confirmPassword.required');
    }
    if (values.confirmPassword && values.password !== values.confirmPassword) {
      errors.confirmPassword = t('auth.register.errors.passwordMismatch');
    }
    return errors;
  };

  // ── Form submit handlers ───────────────────────────────────────────────────
  const handleIdentitySubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateIdentityStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      usernameInputRef.current?.focus();
      return;
    }

    goToStep('contact');
  };

  const handleContactSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateContactStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      emailInputRef.current?.focus();
      return;
    }

    goToStep('security');
  };

  const handleSecuritySubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateSecurityStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      passwordInputRef.current?.focus();
      return;
    }

    setIsSubmitting(true);

    try {
      // Normalize phone: ensure international prefix present
      let phone = values.phoneNumber.trim();
      if (phone && !phone.startsWith('+')) {
        const dial = selectedPhoneCountry?.dialCode || '';
        // strip leading zeros and spaces
        phone = `${dial}${phone.replace(/^0+/, '')}`;
      }

      await register({
        username: values.username.trim(),
        fullName: values.fullName.trim(),
        phoneNumber: phone,
        email: values.email.trim(),
        userType: 'INDIVIDUAL',
        addressLine: resolvedLocation?.address || undefined,
        postalCode: resolvedLocation?.postalCode || undefined,
        city: resolvedLocation?.city || undefined,
        country: resolvedLocation?.countryCode || resolvedLocation?.country,
        defaultLanguage: values.defaultLanguage,
        password: values.password,
      });

      setStatusMessage({
        tone: 'default',
        text: t('auth.register.status.success'),
      });
      navigate(appRoutes.profile, { replace: true });
    } catch (error) {
      const detail = getErrorDisplay(error, {
        fallbackCode: 'AUTH_REGISTER_FAILED',
        fallbackMessage: t('auth.register.status.error'),
      });
      setStatusMessage(detail);
      reportError(detail);
    } finally {
      setIsSubmitting(false);
    }
  };

  // ── Location handlers ──────────────────────────────────────────────────────
  const handleLocationInputChange = (value: string) => {
    setLocationQuery(value);
    setFieldErrors((current) => ({ ...current, location: undefined }));
    clearStatus();

    if (resolvedLocation && value.trim() !== resolvedLocation.displayName) {
      setResolvedLocation(null);
    }
  };

  const handleLocationSuggestionSelect = (suggestion: LocationSuggestion) => {
    setLocationQuery(suggestion.displayName);
    setResolvedLocation({
      ...suggestion,
      isPrecise: suggestion.isPrecise ?? false,
    });
    setActiveSuggestionIndex(-1);
  };

  const handleLocationKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (locationSuggestions.length === 0) {
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current < locationSuggestions.length - 1 ? current + 1 : 0,
      );
      return;
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current > 0 ? current - 1 : locationSuggestions.length - 1,
      );
      return;
    }

    if (event.key === 'Enter' && activeSuggestionIndex >= 0) {
      event.preventDefault();
      handleLocationSuggestionSelect(locationSuggestions[activeSuggestionIndex]);
      return;
    }

    if (event.key === 'Escape') {
      setActiveSuggestionIndex(-1);
    }
  };

  // ── Phone country handlers ─────────────────────────────────────────────────
  const handlePhoneCountrySelect = (option: PhoneCountryOption) => {
    setPhoneCountryCode(option.code as CountryCode);
  };

  // ── Header copy ────────────────────────────────────────────────────────────
  const headerTitle =
    currentStep === 'identity'
      ? t('auth.register.steps.identity.title')
      : currentStep === 'contact'
        ? t('auth.register.steps.contact.title')
        : currentStep === 'security'
          ? t('auth.register.steps.security.title')
          : t('auth.register.steps.done.title');

  const headerIntro =
    currentStep === 'identity'
      ? t('auth.register.steps.identity.intro')
      : currentStep === 'contact'
        ? t('auth.register.steps.contact.intro')
        : currentStep === 'security'
          ? t('auth.register.steps.security.intro')
          : t('auth.register.steps.done.intro');
  // ── Effects ────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (previousStepRef.current === currentStep) {
      return;
    }

    previousStepRef.current = currentStep;
    stepHeadingRef.current?.focus();
  }, [currentStep]);

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <FormPage
      aria-labelledby="register-title"
      progress={
        <div id={progressId}>
          <ProgressStepper
            steps={stepOrder
              .filter((s) => s !== 'done')
              .map((s) => t(`auth.register.steps.${s}.title`))}
            currentStep={currentStepIndex}
            totalSteps={stepOrder.length - 1}
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
                  : 'critical',
            }
          : null
      }
      action={
        <div className="auth-floating-language">
          <LanguageSwitcher />
        </div>
      }
      titleId="register-title"
      introId={headerIntroId}
      title={headerTitle}
      titleRef={stepHeadingRef}
      titleTabIndex={-1}
      titleDescribedBy={`${progressId} ${headerIntroId}`}
      intro={headerIntro}
    >
      {currentStep === 'identity' ? (
        <FlowStepForm
          onSubmit={handleIdentitySubmit}
          actions={<ActionButton type="submit">{t('auth.register.actions.next')}</ActionButton>}
        >
          <TextInput
            ref={usernameInputRef}
            id="register-username"
            type="text"
            name="username"
            autoComplete="username"
            value={values.username}
            onChange={(event) => setFieldValue('username', event.target.value)}
            error={Boolean(fieldErrors.username)}
            aria-describedby={getFieldDescribedBy(
              usernameHintId,
              fieldErrors.username,
              usernameErrorId,
            )}
            aria-errormessage={fieldErrors.username ? usernameErrorId : undefined}
            aria-invalid={fieldErrors.username ? 'true' : 'false'}
            trailingIcon={<UserRound aria-hidden="true" size={16} />}
            required
            label={t('auth.register.fields.username.label')}
            hint={t('auth.register.fields.username.hint')}
            hintId={usernameHintId}
            fieldError={fieldErrors.username}
            errorId={usernameErrorId}
          />

          <TextInput
            id="register-full-name"
            type="text"
            name="fullName"
            autoComplete="name"
            value={values.fullName}
            onChange={(event) => setFieldValue('fullName', event.target.value)}
            error={Boolean(fieldErrors.fullName)}
            aria-describedby={getFieldDescribedBy(
              fullNameHintId,
              fieldErrors.fullName,
              fullNameErrorId,
            )}
            aria-errormessage={fieldErrors.fullName ? fullNameErrorId : undefined}
            aria-invalid={fieldErrors.fullName ? 'true' : 'false'}
            required
            label={t('auth.register.fields.fullName.label')}
            hint={t('auth.register.fields.fullName.hint')}
            hintId={fullNameHintId}
            fieldError={fieldErrors.fullName}
            errorId={fullNameErrorId}
          />

          <PhoneNumberField
            id="register-phone"
            name="phoneNumber"
            label={t('auth.register.fields.phoneNumber.label')}
            hint={t('auth.register.fields.phoneNumber.hint')}
            hintId={phoneHintId}
            value={values.phoneNumber}
            onNumberChange={(event) => setFieldValue('phoneNumber', event.target.value)}
            countryControlId={phoneCountryControlId}
            countryHintId={phoneCountryHintId}
            countryHint={t('auth.register.fields.phoneNumber.countryCodeHint', {
              dialCode: selectedPhoneCountry.dialCode,
            })}
            selectedCountry={selectedPhoneCountry}
            options={phoneCountryOptions}
            onCountrySelect={handlePhoneCountrySelect}
          />
        </FlowStepForm>
      ) : null}

      {currentStep === 'contact' ? (
        <FlowStepForm
          onSubmit={handleContactSubmit}
          actions={
            <>
              <ActionButton type="submit">{t('auth.register.actions.next')}</ActionButton>
              <ActionButton type="button" variant="secondary" onClick={() => goToStep('identity')}>
                {t('auth.register.actions.back')}{' '}
              </ActionButton>
            </>
          }
        >
          <TextInput
            ref={emailInputRef}
            id="register-email"
            type="email"
            name="email"
            autoComplete="email"
            inputMode="email"
            value={values.email}
            onChange={(event) => setFieldValue('email', event.target.value)}
            error={Boolean(fieldErrors.email)}
            aria-describedby={getFieldDescribedBy(emailHintId, fieldErrors.email, emailErrorId)}
            aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
            aria-invalid={fieldErrors.email ? 'true' : 'false'}
            required
            label={t('auth.register.fields.email.label')}
            hint={t('auth.register.fields.email.hint')}
            hintId={emailHintId}
            fieldError={fieldErrors.email}
            errorId={emailErrorId}
          />

          <FormField
            htmlFor="register-location"
            label={t('auth.register.fields.location.label')}
            hint={t('auth.register.fields.location.hint')}
            required
            hintId={locationHintId}
            error={fieldErrors.location}
            errorId={locationErrorId}
            status={
              <LocationLookup
                statusId={locationStatusId}
                loading={locationLookupLoading}
                loadingText={t('auth.register.fields.location.lookupLoading')}
                statusMessage={locationLookupError?.message || null}
                resolvedText={resolvedLocation ? formatLocationPreview(resolvedLocation) : ''}
                listId={locationListId}
                suggestions={locationSuggestions}
                activeSuggestionIndex={activeSuggestionIndex}
                getSuggestionId={getLocationSuggestionId}
                onSuggestionSelect={handleLocationSuggestionSelect}
              />
            }
          >
            <div className="relative">
              <TextInput
                id="register-location"
                type="text"
                name="location"
                autoComplete="off"
                enterKeyHint="next"
                value={locationQuery}
                onChange={(event) => handleLocationInputChange(event.target.value)}
                onKeyDown={handleLocationKeyDown}
                trailingIcon={<MapPin aria-hidden="true" size={16} />}
                aria-describedby={[
                  locationHintId,
                  locationStatusId,
                  fieldErrors.location ? locationErrorId : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                aria-errormessage={fieldErrors.location ? locationErrorId : undefined}
                aria-invalid={fieldErrors.location ? 'true' : 'false'}
                aria-autocomplete="list"
                aria-controls={locationSuggestions.length > 0 ? locationListId : undefined}
                aria-activedescendant={
                  activeSuggestionIndex >= 0
                    ? getLocationSuggestionId(activeSuggestionIndex)
                    : undefined
                }
                aria-expanded={locationSuggestions.length > 0}
                role="combobox"
                required
              />
            </div>
          </FormField>

          <FormField
            htmlFor="register-default-language"
            label={t('auth.register.fields.defaultLanguage.label')}
            hint={t('auth.register.fields.defaultLanguage.hint')}
            hintId={defaultLanguageHintId}
          >
            <select
              id="register-default-language"
              name="defaultLanguage"
              value={values.defaultLanguage}
              onChange={(event) => setFieldValue('defaultLanguage', event.target.value as Locale)}
              className="field-control"
              aria-describedby={defaultLanguageHintId}
            >
              {languageOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </FormField>
        </FlowStepForm>
      ) : null}

      {currentStep === 'security' ? (
        <FlowStepForm
          onSubmit={handleSecuritySubmit}
          actions={
            <>
              <ActionButton type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting
                  ? t('auth.register.actions.submitting')
                  : t('auth.register.actions.next')}{' '}
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
                onClick={() => goToStep('contact')}
                disabled={isSubmitting}
              >
                {t('auth.register.actions.back')}{' '}
              </ActionButton>
            </>
          }
        >
          <PasswordField
            ref={passwordInputRef}
            id="register-password"
            name="password"
            label={t('auth.register.fields.password.label')}
            value={values.password}
            onChange={(val) => setFieldValue('password', val)}
            show={showPassword}
            setShow={setShowPassword}
            error={fieldErrors.password}
            errorId={passwordErrorId}
            hint={t('auth.register.fields.password.hint')}
            hintId={passwordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t('auth.fields.password.show')}
            ariaLabelHide={t('auth.fields.password.hide')}
          />
          <PasswordField
            id="register-confirm-password"
            name="confirmPassword"
            label={t('auth.register.fields.confirmPassword.label')}
            value={values.confirmPassword}
            onChange={(val) => setFieldValue('confirmPassword', val)}
            show={showConfirmPassword}
            setShow={setShowConfirmPassword}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
            hint={t('auth.register.fields.confirmPassword.hint')}
            hintId={confirmPasswordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t('auth.fields.password.show')}
            ariaLabelHide={t('auth.fields.password.hide')}
          />
          <p className="helper-text">{t('auth.register.levelNotice')}</p>
        </FlowStepForm>
      ) : null}

      {currentStep === 'done' ? (
        <>
          <CompletionMessage>{t('auth.register.status.success')}</CompletionMessage>

          <FormActions>
            <ActionLink to={appRoutes.login}>{t('auth.actions.backToSignIn')}</ActionLink>{' '}
          </FormActions>
        </>
      ) : null}
    </FormPage>
  );
}

export default RegisterPage;
