<<<<<<< HEAD
import { useDocumentTitle } from '../lib/documentTitle';
=======
import { useDocumentTitle } from "../lib/documentTitle";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

import {
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
<<<<<<< HEAD
} from 'react';
import { useNavigate } from 'react-router';
import { MapPin, UserRound } from 'lucide-react';
import { getCountries, getCountryCallingCode, type CountryCode } from 'libphonenumber-js/min';
import { useTranslation } from 'react-i18next';
import { languageLabels, type Locale } from '../lib/i18n';
import { appRoutes } from '../router/routes.ts';
import { ActionButton, ActionLink } from '../components/forms/Action';
import { AuthStepForm, FormActions } from '../components/auth/AuthForm';
import AuthPage from '../components/auth/AuthPage';
import { AuthCompletion } from '../components/auth/AuthSupport';
import Stepper from '../components/auth/Stepper';
import LanguageSwitcher from '../components/i18n/LanguageSwitcher';
import LocationLookup, { type LocationSuggestion } from '../components/forms/LocationLookup';
import PasswordField from '../components/forms/PasswordField';
import PhoneNumberField from '../components/forms/PhoneNumberField';
import type { PhoneCountryOption } from '../components/forms/PhoneCountrySelect';
import FormField from '../components/forms/FormField';
import TextInput from '../components/forms/TextInput';
import { useAuth } from '../contexts/auth';
import { useGeocoding } from '../hooks/useGeocoding';
import { getErrorDisplay } from '../lib/http/errors';
import { isPasswordLongEnough, PASSWORD_MIN_LENGTH } from '../lib/validation/password';
=======
} from "react";
import { useNavigate } from "react-router";
import { MapPin, UserRound } from "lucide-react";
import {
  getCountries,
  getCountryCallingCode,
  type CountryCode,
} from "libphonenumber-js/min";
import { useTranslation } from "react-i18next";
import { languageLabels, type Locale } from "../lib/i18n";
import { appRoutes } from "../router/routes.ts";
import { ActionButton, ActionLink } from "../components/forms/Action";
import { AuthStepForm, FormActions } from "../components/auth/AuthForm";
import AuthPage from "../components/auth/AuthPage";
import { AuthCompletion } from "../components/auth/AuthSupport";
import Stepper from "../components/auth/Stepper";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import LocationLookup, {
  type LocationSuggestion,
} from "../components/forms/LocationLookup";
import PasswordField from "../components/forms/PasswordField";
import PhoneNumberField from "../components/forms/PhoneNumberField";
import type { PhoneCountryOption } from "../components/forms/PhoneCountrySelect";
import FormField from "../components/forms/FormField";
import TextInput from "../components/forms/TextInput";
import { useAuth } from "../contexts/auth";
import { useGeocoding } from "../hooks/useGeocoding";
import { getErrorDisplay } from "../lib/http/errors";
import {
  isPasswordLongEnough,
  PASSWORD_MIN_LENGTH,
} from "../lib/validation/password";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
import {
  EMAIL_MAX_LENGTH,
  FULL_NAME_MAX_LENGTH,
  PHONE_MAX_LENGTH,
  USERNAME_MAX_LENGTH,
  isWellFormedEmail,
<<<<<<< HEAD
} from '../lib/validation/user';

type Step = 'identity' | 'contact' | 'security' | 'done';
=======
} from "../lib/validation/user";

type Step = "identity" | "contact" | "security" | "done";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type FormValues = {
  username: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  defaultLanguage: Locale;
  password: string;
  confirmPassword: string;
};

<<<<<<< HEAD
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
=======
type FieldErrors = Partial<Record<keyof FormValues | "location", string>>;

type StatusMessage = {
  text: string;
  tone: "default" | "critical";
} | null;

const initialValues: FormValues = {
  username: "",
  fullName: "",
  phoneNumber: "",
  email: "",
  defaultLanguage: "en",
  password: "",
  confirmPassword: "",
};

function countryCodeToFlag(countryCode: CountryCode) {
  return countryCode.replace(/./g, (char) =>
    String.fromCodePoint(127397 + char.charCodeAt(0)),
  );
}

function buildPhoneCountryOptions(language: string) {
  const displayNames = new Intl.DisplayNames([language, "en"], {
    type: "region",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
    const locale = new Intl.Locale(language || 'en').maximize();
=======
    const locale = new Intl.Locale(language || "en").maximize();
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    const region = locale.region as CountryCode | undefined;

    if (region && getCountries().includes(region)) {
      return region;
    }
  } catch {
<<<<<<< HEAD
    return 'ES';
  }

  return 'ES';
}

function RegisterPage() {
  useDocumentTitle('meta.registerPageTitle');
=======
    return "ES";
  }

  return "ES";
}

function RegisterPage() {
  useDocumentTitle("meta.registerPageTitle");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  const { t, i18n } = useTranslation();
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
<<<<<<< HEAD
  const [currentStep, setCurrentStep] = useState<Step>('identity');
=======
  const [currentStep, setCurrentStep] = useState<Step>("identity");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [values, setValues] = useState<FormValues>(() => ({
    ...initialValues,
    defaultLanguage:
      i18n.resolvedLanguage && i18n.resolvedLanguage in languageLabels
        ? (i18n.resolvedLanguage as Locale)
<<<<<<< HEAD
        : 'en',
=======
        : "en",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  }));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Location
<<<<<<< HEAD
  const [locationQuery, setLocationQuery] = useState('');
  const [resolvedLocation, setResolvedLocation] = useState<LocationSuggestion | null>(null);
=======
  const [locationQuery, setLocationQuery] = useState("");
  const [resolvedLocation, setResolvedLocation] =
    useState<LocationSuggestion | null>(null);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

  // Password visibility
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Phone country
  const [phoneCountryCode, setPhoneCountryCode] = useState<CountryCode>(() =>
    getDefaultPhoneCountry(i18n.resolvedLanguage || i18n.language),
  );

  // ── Derived values ─────────────────────────────────────────────────────────
<<<<<<< HEAD
  const stepOrder: Step[] = ['identity', 'contact', 'security', 'done'];
=======
  const stepOrder: Step[] = ["identity", "contact", "security", "done"];
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
    () => buildPhoneCountryOptions(i18n.resolvedLanguage || i18n.language || 'en'),
=======
    () =>
      buildPhoneCountryOptions(i18n.resolvedLanguage || i18n.language || "en"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    [i18n.language, i18n.resolvedLanguage],
  );

  const phoneCountryOptionsByCode = useMemo(
    () =>
<<<<<<< HEAD
      Object.fromEntries(phoneCountryOptions.map((option) => [option.code, option])) as Record<
        string,
        PhoneCountryOption
      >,
=======
      Object.fromEntries(
        phoneCountryOptions.map((option) => [option.code, option]),
      ) as Record<string, PhoneCountryOption>,
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    [phoneCountryOptions],
  );

  const selectedPhoneCountry =
    phoneCountryOptionsByCode[phoneCountryCode] ?? phoneCountryOptions[0];

  const geocodingQuery =
<<<<<<< HEAD
    resolvedLocation && locationQuery.trim() === resolvedLocation.displayName ? '' : locationQuery;
=======
    resolvedLocation && locationQuery.trim() === resolvedLocation.displayName
      ? ""
      : locationQuery;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  const {
    data: locationSuggestions,
    error: locationLookupError,
    loading: locationLookupLoading,
  } = useGeocoding(geocodingQuery);

  // ── Helpers ────────────────────────────────────────────────────────────────
  const clearStatus = () => setStatusMessage(null);

  const formatLocationPreview = (loc: LocationSuggestion) => {
<<<<<<< HEAD
    if (!loc) return '';
=======
    if (!loc) return "";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    const parts: string[] = [];
    if (loc.address) parts.push(loc.address);
    if (loc.city && !parts.includes(loc.city)) parts.push(loc.city);
    if (loc.country) parts.push(loc.country);
<<<<<<< HEAD
    return parts.join(', ');
=======
    return parts.join(", ");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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

<<<<<<< HEAD
  const getFieldDescribedBy = (hintId: string, error?: string, errorId?: string) =>
    [hintId, error ? errorId : ''].filter(Boolean).join(' ');

  const getLocationSuggestionId = (index: number) => `${locationListId}-option-${index}`;
=======
  const getFieldDescribedBy = (
    hintId: string,
    error?: string,
    errorId?: string,
  ) => [hintId, error ? errorId : ""].filter(Boolean).join(" ");

  const getLocationSuggestionId = (index: number) =>
    `${locationListId}-option-${index}`;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  // ── Validation ─────────────────────────────────────────────────────────────
  const validateIdentityStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.username.trim()) {
<<<<<<< HEAD
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
=======
      errors.username = t("auth.register.errors.usernameRequired");
    } else if (values.username.trim().length > USERNAME_MAX_LENGTH) {
      errors.username = t("auth.register.errors.usernameRequired");
    }
    if (!values.fullName.trim()) {
      errors.fullName = t("auth.register.errors.fullNameRequired");
    } else if (values.fullName.trim().length > FULL_NAME_MAX_LENGTH) {
      errors.fullName = t("auth.register.errors.fullNameRequired");
    }
    if (values.phoneNumber.trim().length > PHONE_MAX_LENGTH) {
      errors.phoneNumber = t("auth.register.errors.phoneRequired");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }
    return errors;
  };

  const validateContactStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.email.trim()) {
<<<<<<< HEAD
      errors.email = t('auth.register.errors.emailRequired');
=======
      errors.email = t("auth.register.errors.emailRequired");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    } else if (
      !isWellFormedEmail(values.email.trim()) ||
      values.email.trim().length > EMAIL_MAX_LENGTH
    ) {
<<<<<<< HEAD
      errors.email = t('auth.register.errors.emailInvalid');
=======
      errors.email = t("auth.register.errors.emailInvalid");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }

    return errors;
  };

  const validateSecurityStep = (): FieldErrors => {
    const errors: FieldErrors = {};
    if (!values.password) {
<<<<<<< HEAD
      errors.password = t('auth.register.errors.passwordRequired');
    } else if (!isPasswordLongEnough(values.password)) {
      errors.password = t('auth.register.fields.password.length', {
=======
      errors.password = t("auth.register.errors.passwordRequired");
    } else if (!isPasswordLongEnough(values.password)) {
      errors.password = t("auth.register.fields.password.length", {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        count: PASSWORD_MIN_LENGTH,
      });
    }
    if (!values.confirmPassword) {
<<<<<<< HEAD
      errors.confirmPassword = t('auth.register.fields.confirmPassword.required');
    }
    if (values.confirmPassword && values.password !== values.confirmPassword) {
      errors.confirmPassword = t('auth.register.errors.passwordMismatch');
=======
      errors.confirmPassword = t(
        "auth.register.fields.confirmPassword.required",
      );
    }
    if (values.confirmPassword && values.password !== values.confirmPassword) {
      errors.confirmPassword = t("auth.register.errors.passwordMismatch");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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

<<<<<<< HEAD
    goToStep('contact');
=======
    goToStep("contact");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  };

  const handleContactSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateContactStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      emailInputRef.current?.focus();
      return;
    }

<<<<<<< HEAD
    goToStep('security');
=======
    goToStep("security");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
      if (phone && !phone.startsWith('+')) {
        const dial = selectedPhoneCountry?.dialCode || '';
        // strip leading zeros and spaces
        phone = `${dial}${phone.replace(/^0+/, '')}`;
=======
      if (phone && !phone.startsWith("+")) {
        const dial = selectedPhoneCountry?.dialCode || "";
        // strip leading zeros and spaces
        phone = `${dial}${phone.replace(/^0+/, "")}`;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      }

      await register({
        username: values.username.trim(),
        fullName: values.fullName.trim(),
        phoneNumber: phone,
        email: values.email.trim(),
<<<<<<< HEAD
        userType: 'INDIVIDUAL',
=======
        userType: "INDIVIDUAL",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        addressLine: resolvedLocation?.address || undefined,
        postalCode: resolvedLocation?.postalCode || undefined,
        city: resolvedLocation?.city || undefined,
        country: resolvedLocation?.countryCode || resolvedLocation?.country,
        defaultLanguage: values.defaultLanguage,
        password: values.password,
      });

      setStatusMessage({
<<<<<<< HEAD
        tone: 'default',
        text: t('auth.register.status.success'),
=======
        tone: "default",
        text: t("auth.register.status.success"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      });
      navigate(appRoutes.profile, { replace: true });
    } catch (error) {
      const detail = getErrorDisplay(error, {
<<<<<<< HEAD
        fallbackCode: 'AUTH_REGISTER_FAILED',
        fallbackMessage: t('auth.register.status.error'),
      });
      setStatusMessage({
        tone: 'critical',
=======
        fallbackCode: "AUTH_REGISTER_FAILED",
        fallbackMessage: t("auth.register.status.error"),
      });
      setStatusMessage({
        tone: "critical",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        text: detail.message,
      });
      reportError(detail.message);
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

<<<<<<< HEAD
    if (event.key === 'ArrowDown') {
=======
    if (event.key === "ArrowDown") {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current < locationSuggestions.length - 1 ? current + 1 : 0,
      );
      return;
    }

<<<<<<< HEAD
    if (event.key === 'ArrowUp') {
=======
    if (event.key === "ArrowUp") {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current > 0 ? current - 1 : locationSuggestions.length - 1,
      );
      return;
    }

<<<<<<< HEAD
    if (event.key === 'Enter' && activeSuggestionIndex >= 0) {
      event.preventDefault();
      handleLocationSuggestionSelect(locationSuggestions[activeSuggestionIndex]);
      return;
    }

    if (event.key === 'Escape') {
=======
    if (event.key === "Enter" && activeSuggestionIndex >= 0) {
      event.preventDefault();
      handleLocationSuggestionSelect(
        locationSuggestions[activeSuggestionIndex],
      );
      return;
    }

    if (event.key === "Escape") {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      setActiveSuggestionIndex(-1);
    }
  };

  // ── Phone country handlers ─────────────────────────────────────────────────
  const handlePhoneCountrySelect = (option: PhoneCountryOption) => {
    setPhoneCountryCode(option.code as CountryCode);
  };

  // ── Header copy ────────────────────────────────────────────────────────────
  const headerTitle =
<<<<<<< HEAD
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
=======
    currentStep === "identity"
      ? t("auth.register.steps.identity.title")
      : currentStep === "contact"
        ? t("auth.register.steps.contact.title")
        : currentStep === "security"
          ? t("auth.register.steps.security.title")
          : t("auth.register.steps.done.title");

  const headerIntro =
    currentStep === "identity"
      ? t("auth.register.steps.identity.intro")
      : currentStep === "contact"
        ? t("auth.register.steps.contact.intro")
        : currentStep === "security"
          ? t("auth.register.steps.security.intro")
          : t("auth.register.steps.done.intro");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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
    <AuthPage
      aria-labelledby="register-title"
      progress={
        <div id={progressId}>
          <Stepper
            steps={stepOrder
<<<<<<< HEAD
              .filter((s) => s !== 'done')
=======
              .filter((s) => s !== "done")
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
              content: statusMessage?.text || authError,
<<<<<<< HEAD
              tone: statusMessage?.tone === 'critical' ? 'critical' : 'default',
=======
              tone: statusMessage?.tone === "critical" ? "critical" : "default",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
      {currentStep === 'identity' ? (
        <AuthStepForm
          onSubmit={handleIdentitySubmit}
          actions={<ActionButton type="submit">{t('auth.register.actions.next')}</ActionButton>}
=======
      {currentStep === "identity" ? (
        <AuthStepForm
          onSubmit={handleIdentitySubmit}
          actions={
            <ActionButton type="submit">
              {t("auth.register.actions.next")}
            </ActionButton>
          }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        >
          <TextInput
            ref={usernameInputRef}
            id="register-username"
            type="text"
            name="username"
            autoComplete="username"
            value={values.username}
<<<<<<< HEAD
            onChange={(event) => setFieldValue('username', event.target.value)}
=======
            onChange={(event) => setFieldValue("username", event.target.value)}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            error={Boolean(fieldErrors.username)}
            aria-describedby={getFieldDescribedBy(
              usernameHintId,
              fieldErrors.username,
              usernameErrorId,
            )}
<<<<<<< HEAD
            aria-errormessage={fieldErrors.username ? usernameErrorId : undefined}
            aria-invalid={fieldErrors.username ? 'true' : 'false'}
            trailingIcon={<UserRound aria-hidden="true" size={16} />}
            required
            label={t('auth.register.fields.username.label')}
            hint={t('auth.register.fields.username.hint')}
=======
            aria-errormessage={
              fieldErrors.username ? usernameErrorId : undefined
            }
            aria-invalid={fieldErrors.username ? "true" : "false"}
            trailingIcon={<UserRound aria-hidden="true" size={16} />}
            required
            label={t("auth.register.fields.username.label")}
            hint={t("auth.register.fields.username.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
            onChange={(event) => setFieldValue('fullName', event.target.value)}
=======
            onChange={(event) => setFieldValue("fullName", event.target.value)}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            error={Boolean(fieldErrors.fullName)}
            aria-describedby={getFieldDescribedBy(
              fullNameHintId,
              fieldErrors.fullName,
              fullNameErrorId,
            )}
<<<<<<< HEAD
            aria-errormessage={fieldErrors.fullName ? fullNameErrorId : undefined}
            aria-invalid={fieldErrors.fullName ? 'true' : 'false'}
            required
            label={t('auth.register.fields.fullName.label')}
            hint={t('auth.register.fields.fullName.hint')}
=======
            aria-errormessage={
              fieldErrors.fullName ? fullNameErrorId : undefined
            }
            aria-invalid={fieldErrors.fullName ? "true" : "false"}
            required
            label={t("auth.register.fields.fullName.label")}
            hint={t("auth.register.fields.fullName.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            hintId={fullNameHintId}
            fieldError={fieldErrors.fullName}
            errorId={fullNameErrorId}
          />

          <PhoneNumberField
            id="register-phone"
            name="phoneNumber"
<<<<<<< HEAD
            label={t('auth.register.fields.phoneNumber.label')}
            hint={t('auth.register.fields.phoneNumber.hint')}
            hintId={phoneHintId}
            value={values.phoneNumber}
            onNumberChange={(event) => setFieldValue('phoneNumber', event.target.value)}
            countryControlId={phoneCountryControlId}
            countryHintId={phoneCountryHintId}
            countryHint={t('auth.register.fields.phoneNumber.countryCodeHint', {
=======
            label={t("auth.register.fields.phoneNumber.label")}
            hint={t("auth.register.fields.phoneNumber.hint")}
            hintId={phoneHintId}
            value={values.phoneNumber}
            onNumberChange={(event) =>
              setFieldValue("phoneNumber", event.target.value)
            }
            countryControlId={phoneCountryControlId}
            countryHintId={phoneCountryHintId}
            countryHint={t("auth.register.fields.phoneNumber.countryCodeHint", {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
              dialCode: selectedPhoneCountry.dialCode,
            })}
            selectedCountry={selectedPhoneCountry}
            options={phoneCountryOptions}
            onCountrySelect={handlePhoneCountrySelect}
          />
        </AuthStepForm>
      ) : null}

<<<<<<< HEAD
      {currentStep === 'contact' ? (
=======
      {currentStep === "contact" ? (
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        <AuthStepForm
          onSubmit={handleContactSubmit}
          actions={
            <>
<<<<<<< HEAD
              <ActionButton type="submit">{t('auth.register.actions.next')}</ActionButton>
              <ActionButton type="button" variant="secondary" onClick={() => goToStep('identity')}>
                {t('auth.register.actions.back')}
=======
              <ActionButton type="submit">
                {t("auth.register.actions.next")}
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
                onClick={() => goToStep("identity")}
              >
                {t("auth.register.actions.back")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
            onChange={(event) => setFieldValue('email', event.target.value)}
            error={Boolean(fieldErrors.email)}
            aria-describedby={getFieldDescribedBy(emailHintId, fieldErrors.email, emailErrorId)}
            aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
            aria-invalid={fieldErrors.email ? 'true' : 'false'}
            required
            label={t('auth.register.fields.email.label')}
            hint={t('auth.register.fields.email.hint')}
=======
            onChange={(event) => setFieldValue("email", event.target.value)}
            error={Boolean(fieldErrors.email)}
            aria-describedby={getFieldDescribedBy(
              emailHintId,
              fieldErrors.email,
              emailErrorId,
            )}
            aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
            aria-invalid={fieldErrors.email ? "true" : "false"}
            required
            label={t("auth.register.fields.email.label")}
            hint={t("auth.register.fields.email.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            hintId={emailHintId}
            fieldError={fieldErrors.email}
            errorId={emailErrorId}
          />

          <FormField
            htmlFor="register-location"
<<<<<<< HEAD
            label={t('auth.register.fields.location.label')}
            hint={t('auth.register.fields.location.hint')}
=======
            label={t("auth.register.fields.location.label")}
            hint={t("auth.register.fields.location.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            required
            hintId={locationHintId}
            error={fieldErrors.location}
            errorId={locationErrorId}
            status={
              <LocationLookup
                statusId={locationStatusId}
                loading={locationLookupLoading}
<<<<<<< HEAD
                loadingText={t('auth.register.fields.location.lookupLoading')}
                statusMessage={locationLookupError?.message || null}
                resolvedText={resolvedLocation ? formatLocationPreview(resolvedLocation) : ''}
=======
                loadingText={t("auth.register.fields.location.lookupLoading")}
                statusMessage={locationLookupError?.message || null}
                resolvedText={
                  resolvedLocation
                    ? formatLocationPreview(resolvedLocation)
                    : ""
                }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
                onChange={(event) => handleLocationInputChange(event.target.value)}
=======
                onChange={(event) =>
                  handleLocationInputChange(event.target.value)
                }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                onKeyDown={handleLocationKeyDown}
                trailingIcon={<MapPin aria-hidden="true" size={16} />}
                aria-describedby={[
                  locationHintId,
                  locationStatusId,
<<<<<<< HEAD
                  fieldErrors.location ? locationErrorId : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                aria-errormessage={fieldErrors.location ? locationErrorId : undefined}
                aria-invalid={fieldErrors.location ? 'true' : 'false'}
                aria-autocomplete="list"
                aria-controls={locationSuggestions.length > 0 ? locationListId : undefined}
=======
                  fieldErrors.location ? locationErrorId : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
                aria-errormessage={
                  fieldErrors.location ? locationErrorId : undefined
                }
                aria-invalid={fieldErrors.location ? "true" : "false"}
                aria-autocomplete="list"
                aria-controls={
                  locationSuggestions.length > 0 ? locationListId : undefined
                }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
            label={t('auth.register.fields.defaultLanguage.label')}
            hint={t('auth.register.fields.defaultLanguage.hint')}
=======
            label={t("auth.register.fields.defaultLanguage.label")}
            hint={t("auth.register.fields.defaultLanguage.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            hintId={defaultLanguageHintId}
          >
            <select
              id="register-default-language"
              name="defaultLanguage"
              value={values.defaultLanguage}
<<<<<<< HEAD
              onChange={(event) => setFieldValue('defaultLanguage', event.target.value as Locale)}
=======
              onChange={(event) =>
                setFieldValue("defaultLanguage", event.target.value as Locale)
              }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
        </AuthStepForm>
      ) : null}

<<<<<<< HEAD
      {currentStep === 'security' ? (
=======
      {currentStep === "security" ? (
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        <AuthStepForm
          onSubmit={handleSecuritySubmit}
          actions={
            <>
<<<<<<< HEAD
              <ActionButton type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting
                  ? t('auth.register.actions.submitting')
                  : t('auth.register.actions.next')}
=======
              <ActionButton
                type="submit"
                disabled={isSubmitting}
                aria-busy={isSubmitting}
              >
                {isSubmitting
                  ? t("auth.register.actions.submitting")
                  : t("auth.register.actions.next")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
              </ActionButton>
              <ActionButton
                type="button"
                variant="secondary"
<<<<<<< HEAD
                onClick={() => goToStep('contact')}
                disabled={isSubmitting}
              >
                {t('auth.register.actions.back')}
=======
                onClick={() => goToStep("contact")}
                disabled={isSubmitting}
              >
                {t("auth.register.actions.back")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
              </ActionButton>
            </>
          }
        >
          <PasswordField
            ref={passwordInputRef}
            id="register-password"
            name="password"
<<<<<<< HEAD
            label={t('auth.register.fields.password.label')}
            value={values.password}
            onChange={(val) => setFieldValue('password', val)}
=======
            label={t("auth.register.fields.password.label")}
            value={values.password}
            onChange={(val) => setFieldValue("password", val)}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            show={showPassword}
            setShow={setShowPassword}
            error={fieldErrors.password}
            errorId={passwordErrorId}
<<<<<<< HEAD
            hint={t('auth.register.fields.password.hint')}
            hintId={passwordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t('auth.fields.password.show')}
            ariaLabelHide={t('auth.fields.password.hide')}
=======
            hint={t("auth.register.fields.password.hint")}
            hintId={passwordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t("auth.fields.password.show")}
            ariaLabelHide={t("auth.fields.password.hide")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          />
          <PasswordField
            id="register-confirm-password"
            name="confirmPassword"
<<<<<<< HEAD
            label={t('auth.register.fields.confirmPassword.label')}
            value={values.confirmPassword}
            onChange={(val) => setFieldValue('confirmPassword', val)}
=======
            label={t("auth.register.fields.confirmPassword.label")}
            value={values.confirmPassword}
            onChange={(val) => setFieldValue("confirmPassword", val)}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            show={showConfirmPassword}
            setShow={setShowConfirmPassword}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
<<<<<<< HEAD
            hint={t('auth.register.fields.confirmPassword.hint')}
            hintId={confirmPasswordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t('auth.fields.password.show')}
            ariaLabelHide={t('auth.fields.password.hide')}
          />
          <p className="helper-text">{t('auth.register.levelNotice')}</p>
        </AuthStepForm>
      ) : null}

      {currentStep === 'done' ? (
        <>
          <AuthCompletion>{t('auth.register.status.success')}</AuthCompletion>

          <FormActions>
            <ActionLink to={appRoutes.login}>{t('auth.actions.backToSignIn')}</ActionLink>
=======
            hint={t("auth.register.fields.confirmPassword.hint")}
            hintId={confirmPasswordHintId}
            autoComplete="new-password"
            required
            ariaLabelShow={t("auth.fields.password.show")}
            ariaLabelHide={t("auth.fields.password.hide")}
          />
          <p className="helper-text">{t("auth.register.levelNotice")}</p>
        </AuthStepForm>
      ) : null}

      {currentStep === "done" ? (
        <>
          <AuthCompletion>{t("auth.register.status.success")}</AuthCompletion>

          <FormActions>
            <ActionLink to={appRoutes.login}>
              {t("auth.actions.backToSignIn")}
            </ActionLink>
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          </FormActions>
        </>
      ) : null}
    </AuthPage>
  );
}

export default RegisterPage;
