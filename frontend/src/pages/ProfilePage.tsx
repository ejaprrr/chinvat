<<<<<<< HEAD
import { useEffect, useId, useMemo, useState, type FormEvent, type KeyboardEvent } from 'react';
import { Languages, Mail, MapPin, Save, ShieldCheck, UserRound } from 'lucide-react';
import { getCountries, getCountryCallingCode, type CountryCode } from 'libphonenumber-js/min';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../contexts/auth';
import { languageLabels, type Locale } from '../lib/i18n';
import { ActionButton } from '../components/forms/Action';
import AuthPage from '../components/auth/AuthPage';
import { AuthForm, AuthFormSection, FormActions } from '../components/auth/AuthForm';
import FormField from '../components/forms/FormField';
import LocationLookup, { type LocationSuggestion } from '../components/forms/LocationLookup';
import PasswordField from '../components/forms/PasswordField';
import PhoneNumberField from '../components/forms/PhoneNumberField';
import type { PhoneCountryOption } from '../components/forms/PhoneCountrySelect';
import TextInput from '../components/forms/TextInput';
import LanguageSwitcher from '../components/i18n/LanguageSwitcher';
import { useDocumentTitle } from '../lib/documentTitle';
import { useGeocoding } from '../hooks/useGeocoding';
import { useProfile } from '../hooks/useProfile';
import { getErrorDisplay } from '../lib/http/errors';
import { isPasswordLongEnough } from '../lib/validation/password';
=======
import {
  useEffect,
  useId,
  useMemo,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import {
  Languages,
  Mail,
  MapPin,
  Save,
  ShieldCheck,
  UserRound,
} from "lucide-react";
import {
  getCountries,
  getCountryCallingCode,
  type CountryCode,
} from "libphonenumber-js/min";
import { useTranslation } from "react-i18next";
import { useAuth } from "../contexts/auth";
import { languageLabels, type Locale } from "../lib/i18n";
import { ActionButton } from "../components/forms/Action";
import AuthPage from "../components/auth/AuthPage";
import {
  AuthForm,
  AuthFormSection,
  FormActions,
} from "../components/auth/AuthForm";
import FormField from "../components/forms/FormField";
import LocationLookup, {
  type LocationSuggestion,
} from "../components/forms/LocationLookup";
import PasswordField from "../components/forms/PasswordField";
import PhoneNumberField from "../components/forms/PhoneNumberField";
import type { PhoneCountryOption } from "../components/forms/PhoneCountrySelect";
import TextInput from "../components/forms/TextInput";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";
import { useDocumentTitle } from "../lib/documentTitle";
import { useGeocoding } from "../hooks/useGeocoding";
import { useProfile } from "../hooks/useProfile";
import { getErrorDisplay } from "../lib/http/errors";
import { isPasswordLongEnough } from "../lib/validation/password";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
import {
  EMAIL_MAX_LENGTH,
  FULL_NAME_MAX_LENGTH,
  PHONE_MAX_LENGTH,
  USERNAME_MAX_LENGTH,
  isWellFormedEmail,
<<<<<<< HEAD
} from '../lib/validation/user';
=======
} from "../lib/validation/user";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type ProfileForm = {
  username: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  defaultLanguage: Locale;
  locationQuery: string;
};

type ProfileErrors = Partial<Record<keyof ProfileForm, string>>;

type PasswordErrors = Partial<
<<<<<<< HEAD
  Record<'currentPassword' | 'newPassword' | 'confirmPassword', string>
>;

const initialProfile: ProfileForm = {
  username: '',
  fullName: '',
  phoneNumber: '',
  email: '',
  defaultLanguage: 'en',
  locationQuery: '',
};

function countryCodeToFlag(countryCode: CountryCode) {
  return countryCode.replace(/./g, (char) => String.fromCodePoint(127397 + char.charCodeAt(0)));
}

function buildPhoneCountryOptions(language: string) {
  const displayNames = new Intl.DisplayNames([language, 'en'], {
    type: 'region',
=======
  Record<"currentPassword" | "newPassword" | "confirmPassword", string>
>;

const initialProfile: ProfileForm = {
  username: "",
  fullName: "",
  phoneNumber: "",
  email: "",
  defaultLanguage: "en",
  locationQuery: "",
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
=======
    return "ES";
  }

  return "ES";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

function ProfilePage() {
  const { t } = useTranslation();
<<<<<<< HEAD
  const { user, changePassword, error: authError, reportError, clearError } = useAuth();
=======
  const {
    user,
    changePassword,
    error: authError,
    reportError,
    clearError,
  } = useAuth();
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const { profile: backendProfile, saveProfile } = useProfile(user?.id);
  const uid = useId();
  const [profile, setProfile] = useState<ProfileForm>(initialProfile);
  const [savedProfile, setSavedProfile] = useState<ProfileForm>(initialProfile);
  const [profileErrors, setProfileErrors] = useState<ProfileErrors>({});
  const [passwordErrors, setPasswordErrors] = useState<PasswordErrors>({});
  const [statusMessage, setStatusMessage] = useState<{
<<<<<<< HEAD
    tone: 'default' | 'critical' | 'warning';
    text: string;
  } | null>(null);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
=======
    tone: "default" | "critical" | "warning";
    text: string;
  } | null>(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [phoneCountryCode, setPhoneCountryCode] = useState<CountryCode>(() =>
    getDefaultPhoneCountry(initialProfile.defaultLanguage),
  );
<<<<<<< HEAD
  const [resolvedLocation, setResolvedLocation] = useState<LocationSuggestion | null>(null);
=======
  const [resolvedLocation, setResolvedLocation] =
    useState<LocationSuggestion | null>(null);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

  useEffect(() => {
    if (!backendProfile) {
      return;
    }

    const timer = window.setTimeout(() => {
      const nextProfile: ProfileForm = {
<<<<<<< HEAD
        username: backendProfile.username ?? '',
        fullName: backendProfile.fullName ?? '',
        phoneNumber: backendProfile.phoneNumber ?? '',
        email: backendProfile.email ?? '',
        defaultLanguage: (backendProfile.defaultLanguage as Locale) ?? 'en',
        locationQuery: [backendProfile.addressLine, backendProfile.city, backendProfile.country]
          .filter(Boolean)
          .join(', '),
=======
        username: backendProfile.username ?? "",
        fullName: backendProfile.fullName ?? "",
        phoneNumber: backendProfile.phoneNumber ?? "",
        email: backendProfile.email ?? "",
        defaultLanguage: (backendProfile.defaultLanguage as Locale) ?? "en",
        locationQuery: [
          backendProfile.addressLine,
          backendProfile.city,
          backendProfile.country,
        ]
          .filter(Boolean)
          .join(", "),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      };

      setProfile(nextProfile);
      setSavedProfile(nextProfile);
      setResolvedLocation(
<<<<<<< HEAD
        backendProfile.addressLine || backendProfile.city || backendProfile.country
=======
        backendProfile.addressLine ||
          backendProfile.city ||
          backendProfile.country
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          ? {
              displayName: nextProfile.locationQuery,
              address: backendProfile.addressLine,
              postalCode: backendProfile.postalCode,
              city: backendProfile.city,
              country: backendProfile.country,
              countryCode: undefined,
              isPrecise: true,
            }
          : null,
      );
    }, 0);

    return () => window.clearTimeout(timer);
  }, [backendProfile]);

  const isDirty = useMemo(
    () => JSON.stringify(profile) !== JSON.stringify(savedProfile),
    [profile, savedProfile],
  );

  const languageOptions = useMemo(
    () =>
      Object.entries(languageLabels).map(([value, label]) => ({
        value: value as Locale,
        label,
      })),
    [],
  );

  const phoneCountryOptions = useMemo(
    () => buildPhoneCountryOptions(profile.defaultLanguage),
    [profile.defaultLanguage],
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

<<<<<<< HEAD
  useDocumentTitle('meta.profilePageTitle');

  const geocodingQuery =
    resolvedLocation && profile.locationQuery.trim() === resolvedLocation.displayName
      ? ''
=======
  useDocumentTitle("meta.profilePageTitle");

  const geocodingQuery =
    resolvedLocation &&
    profile.locationQuery.trim() === resolvedLocation.displayName
      ? ""
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      : profile.locationQuery;

  const {
    data: locationSuggestions,
    error: locationLookupError,
    loading: locationLookupLoading,
  } = useGeocoding(geocodingQuery);

  const locationLookupMessage = useMemo(() => {
    if (locationLookupError) {
      return locationLookupError.message;
    }

    const query = profile.locationQuery.trim();

    if (!query || query.length < 3) {
      return null;
    }

    if (resolvedLocation && resolvedLocation.displayName === query) {
      return null;
    }

<<<<<<< HEAD
    return locationSuggestions.length ? null : t('profile.fields.location.noResults');
  }, [locationLookupError, locationSuggestions.length, profile.locationQuery, resolvedLocation, t]);

  const fieldId = (name: string) => `${uid}-${name}`;

  const locationHintId = fieldId('location-hint');
  const locationErrorId = fieldId('location-error');
  const locationStatusId = fieldId('location-status');
  const locationListId = fieldId('location-list');
  const phoneCountryControlId = fieldId('phone-country');
  const phoneCountryHintId = fieldId('phone-country-hint');
  const getLocationSuggestionId = (index: number) => `${locationListId}-option-${index}`;
=======
    return locationSuggestions.length
      ? null
      : t("profile.fields.location.noResults");
  }, [
    locationLookupError,
    locationSuggestions.length,
    profile.locationQuery,
    resolvedLocation,
    t,
  ]);

  const fieldId = (name: string) => `${uid}-${name}`;

  const locationHintId = fieldId("location-hint");
  const locationErrorId = fieldId("location-error");
  const locationStatusId = fieldId("location-status");
  const locationListId = fieldId("location-list");
  const phoneCountryControlId = fieldId("phone-country");
  const phoneCountryHintId = fieldId("phone-country-hint");
  const getLocationSuggestionId = (index: number) =>
    `${locationListId}-option-${index}`;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  const updateProfileField = <Field extends keyof ProfileForm>(
    field: Field,
    value: ProfileForm[Field],
  ) => {
    setProfile((current) => ({ ...current, [field]: value }));
    setProfileErrors((current) => ({ ...current, [field]: undefined }));
    setStatusMessage(null);
    clearError();
  };

  const validateProfile = () => {
    const errors: ProfileErrors = {};

<<<<<<< HEAD
    if (!profile.username.trim()) errors.username = t('profile.fields.username.required');
    else if (profile.username.trim().length > USERNAME_MAX_LENGTH)
      errors.username = t('profile.fields.username.required');

    if (!profile.fullName.trim()) errors.fullName = t('profile.fields.fullName.required');
    else if (profile.fullName.trim().length > FULL_NAME_MAX_LENGTH)
      errors.fullName = t('profile.fields.fullName.required');

    if (!profile.email.trim()) {
      errors.email = t('profile.fields.email.required');
=======
    if (!profile.username.trim())
      errors.username = t("profile.fields.username.required");
    else if (profile.username.trim().length > USERNAME_MAX_LENGTH)
      errors.username = t("profile.fields.username.required");

    if (!profile.fullName.trim())
      errors.fullName = t("profile.fields.fullName.required");
    else if (profile.fullName.trim().length > FULL_NAME_MAX_LENGTH)
      errors.fullName = t("profile.fields.fullName.required");

    if (!profile.email.trim()) {
      errors.email = t("profile.fields.email.required");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    } else if (
      !isWellFormedEmail(profile.email.trim()) ||
      profile.email.trim().length > EMAIL_MAX_LENGTH
    ) {
<<<<<<< HEAD
      errors.email = t('profile.fields.email.invalid');
    }

    if (profile.phoneNumber.trim().length > PHONE_MAX_LENGTH) {
      errors.phoneNumber = t('profile.fields.phoneNumber.hint');
=======
      errors.email = t("profile.fields.email.invalid");
    }

    if (profile.phoneNumber.trim().length > PHONE_MAX_LENGTH) {
      errors.phoneNumber = t("profile.fields.phoneNumber.hint");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }

    return errors;
  };

  const handleLocationInputChange = (value: string) => {
<<<<<<< HEAD
    updateProfileField('locationQuery', value);
=======
    updateProfileField("locationQuery", value);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

    if (resolvedLocation && value.trim() !== resolvedLocation.displayName) {
      setResolvedLocation(null);
    }

    setActiveSuggestionIndex(-1);
  };

  const handleLocationSuggestionSelect = (suggestion: LocationSuggestion) => {
    setProfile((current) => ({
      ...current,
      locationQuery: suggestion.displayName,
    }));
    setProfileErrors((current) => ({
      ...current,
      locationQuery: undefined,
    }));
    setStatusMessage(null);
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

  const handlePhoneCountrySelect = (option: PhoneCountryOption) => {
    setPhoneCountryCode(option.code as CountryCode);
  };

  const handleProfileSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateProfile();
    setProfileErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setStatusMessage({
<<<<<<< HEAD
        tone: 'critical',
        text: t('profile.status.reviewFields'),
=======
        tone: "critical",
        text: t("profile.status.reviewFields"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      });
      return;
    }

    void (async () => {
      try {
        if (!user?.id) {
<<<<<<< HEAD
          throw new Error('User not authenticated');
=======
          throw new Error("User not authenticated");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        }

        const updated = await saveProfile({
          username: profile.username.trim(),
          fullName: profile.fullName.trim(),
          phoneNumber: profile.phoneNumber.trim() || undefined,
<<<<<<< HEAD
          userType: 'INDIVIDUAL',
          accessLevel: 'NORMAL',
=======
          userType: "INDIVIDUAL",
          accessLevel: "NORMAL",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          addressLine: resolvedLocation?.address,
          postalCode: resolvedLocation?.postalCode,
          city: resolvedLocation?.city,
          country: resolvedLocation?.country,
          defaultLanguage: profile.defaultLanguage,
        });

        const nextProfile: ProfileForm = {
          username: updated.username,
          fullName: updated.fullName,
<<<<<<< HEAD
          phoneNumber: updated.phoneNumber ?? '',
=======
          phoneNumber: updated.phoneNumber ?? "",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          email: updated.email,
          defaultLanguage: updated.defaultLanguage as Locale,
          locationQuery: [updated.addressLine, updated.city, updated.country]
            .filter(Boolean)
<<<<<<< HEAD
            .join(', '),
=======
            .join(", "),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        };

        setProfile(nextProfile);
        setSavedProfile(nextProfile);
        setStatusMessage({
<<<<<<< HEAD
          tone: 'default',
          text: t('profile.status.saveSuccess'),
        });
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: 'PROFILE_UPDATE_FAILED',
          fallbackMessage: t('profile.status.saveError'),
        });
        setStatusMessage({
          tone: 'critical',
=======
          tone: "default",
          text: t("profile.status.saveSuccess"),
        });
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: "PROFILE_UPDATE_FAILED",
          fallbackMessage: t("profile.status.saveError"),
        });
        setStatusMessage({
          tone: "critical",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          text: detail.message,
        });
        reportError(detail.message);
      }
    })();
  };

  const resetProfileChanges = () => {
    setProfile(savedProfile);
    setProfileErrors({});
    setStatusMessage({
<<<<<<< HEAD
      tone: 'warning',
      text: t('profile.status.changesDiscarded'),
=======
      tone: "warning",
      text: t("profile.status.changesDiscarded"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    });
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: PasswordErrors = {};

<<<<<<< HEAD
    if (!currentPassword) nextErrors.currentPassword = t('profile.fields.currentPassword.required');
    if (!newPassword) {
      nextErrors.newPassword = t('profile.fields.newPassword.required');
    } else if (!isPasswordLongEnough(newPassword)) {
      nextErrors.newPassword = t('profile.fields.newPassword.length');
    }
    if (newPassword !== confirmPassword) {
      nextErrors.confirmPassword = t('profile.fields.confirmPassword.mismatch');
=======
    if (!currentPassword)
      nextErrors.currentPassword = t("profile.fields.currentPassword.required");
    if (!newPassword) {
      nextErrors.newPassword = t("profile.fields.newPassword.required");
    } else if (!isPasswordLongEnough(newPassword)) {
      nextErrors.newPassword = t("profile.fields.newPassword.length");
    }
    if (newPassword !== confirmPassword) {
      nextErrors.confirmPassword = t("profile.fields.confirmPassword.mismatch");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    }

    setPasswordErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setStatusMessage({
<<<<<<< HEAD
        tone: 'critical',
        text: t('profile.status.reviewPasswordFields'),
=======
        tone: "critical",
        text: t("profile.status.reviewPasswordFields"),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      });
      return;
    }

    void (async () => {
      try {
        await changePassword(currentPassword, newPassword);
<<<<<<< HEAD
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
        setStatusMessage({
          tone: 'default',
          text: t('profile.status.passwordChangeSuccess'),
        });
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: 'AUTH_PASSWORD_CHANGE_FAILED',
          fallbackMessage: t('profile.status.passwordChangeError'),
        });
        setStatusMessage({
          tone: 'critical',
=======
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
        setStatusMessage({
          tone: "default",
          text: t("profile.status.passwordChangeSuccess"),
        });
      } catch (error) {
        const detail = getErrorDisplay(error, {
          fallbackCode: "AUTH_PASSWORD_CHANGE_FAILED",
          fallbackMessage: t("profile.status.passwordChangeError"),
        });
        setStatusMessage({
          tone: "critical",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          text: detail.message,
        });
        reportError(detail.message);
      }
    })();
  };

  return (
    <main className="auth-popup-shell items-center bg-canvas py-6 lg:py-8">
      <section className="auth-popup max-w-6xl p-4 sm:p-5 lg:p-6">
        <AuthPage
<<<<<<< HEAD
          aria-labelledby={fieldId('title')}
=======
          aria-labelledby={fieldId("title")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          action={
            <div className="auth-floating-language">
              <LanguageSwitcher />
            </div>
          }
<<<<<<< HEAD
          titleId={fieldId('title')}
          title={t('profile.title')}
          intro={t('profile.intro')}
=======
          titleId={fieldId("title")}
          title={t("profile.title")}
          intro={t("profile.intro")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          status={
            statusMessage || authError
              ? {
                  content: statusMessage?.text || authError,
<<<<<<< HEAD
                  tone: statusMessage?.tone || 'critical',
=======
                  tone: statusMessage?.tone || "critical",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                }
              : null
          }
        >
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_20rem] lg:items-stretch xl:grid-cols-[minmax(0,1fr)_21rem]">
<<<<<<< HEAD
            <AuthForm className="flex min-w-0 flex-col gap-4" onSubmit={handleProfileSubmit}>
              <AuthFormSection className="grid gap-3.5 md:grid-cols-2">
                <div className="flex flex-col gap-1 md:col-span-2">
                  <p className="auth-progress-text">{t('profile.sections.accountDetails')}</p>
                </div>

                <TextInput
                  id={fieldId('username')}
                  name="username"
                  label={t('profile.fields.username.label')}
                  value={profile.username}
                  onChange={(event) => updateProfileField('username', event.target.value)}
                  error={Boolean(profileErrors.username)}
                  fieldError={profileErrors.username}
                  errorId={fieldId('username-error')}
=======
            <AuthForm
              className="flex min-w-0 flex-col gap-4"
              onSubmit={handleProfileSubmit}
            >
              <AuthFormSection className="grid gap-3.5 md:grid-cols-2">
                <div className="flex flex-col gap-1 md:col-span-2">
                  <p className="auth-progress-text">
                    {t("profile.sections.accountDetails")}
                  </p>
                </div>

                <TextInput
                  id={fieldId("username")}
                  name="username"
                  label={t("profile.fields.username.label")}
                  value={profile.username}
                  onChange={(event) =>
                    updateProfileField("username", event.target.value)
                  }
                  error={Boolean(profileErrors.username)}
                  fieldError={profileErrors.username}
                  errorId={fieldId("username-error")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  required
                  trailingIcon={<UserRound size={16} aria-hidden="true" />}
                />

                <TextInput
<<<<<<< HEAD
                  id={fieldId('full-name')}
                  name="fullName"
                  label={t('profile.fields.fullName.label')}
                  value={profile.fullName}
                  onChange={(event) => updateProfileField('fullName', event.target.value)}
                  error={Boolean(profileErrors.fullName)}
                  fieldError={profileErrors.fullName}
                  errorId={fieldId('full-name-error')}
=======
                  id={fieldId("full-name")}
                  name="fullName"
                  label={t("profile.fields.fullName.label")}
                  value={profile.fullName}
                  onChange={(event) =>
                    updateProfileField("fullName", event.target.value)
                  }
                  error={Boolean(profileErrors.fullName)}
                  fieldError={profileErrors.fullName}
                  errorId={fieldId("full-name-error")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  required
                />

                <TextInput
<<<<<<< HEAD
                  id={fieldId('email')}
                  name="email"
                  type="email"
                  label={t('profile.fields.email.label')}
                  value={profile.email}
                  onChange={(event) => updateProfileField('email', event.target.value)}
                  error={Boolean(profileErrors.email)}
                  fieldError={profileErrors.email}
                  errorId={fieldId('email-error')}
=======
                  id={fieldId("email")}
                  name="email"
                  type="email"
                  label={t("profile.fields.email.label")}
                  value={profile.email}
                  onChange={(event) =>
                    updateProfileField("email", event.target.value)
                  }
                  error={Boolean(profileErrors.email)}
                  fieldError={profileErrors.email}
                  errorId={fieldId("email-error")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  required
                  trailingIcon={<Mail size={16} aria-hidden="true" />}
                />

                <PhoneNumberField
<<<<<<< HEAD
                  id={fieldId('phone')}
                  name="phoneNumber"
                  label={t('profile.fields.phoneNumber.label')}
                  hint={t('profile.fields.phoneNumber.hint')}
                  hintId={fieldId('phone-hint')}
                  value={profile.phoneNumber}
                  onNumberChange={(event) => updateProfileField('phoneNumber', event.target.value)}
                  countryControlId={phoneCountryControlId}
                  countryHintId={phoneCountryHintId}
                  countryHint={t('profile.fields.phoneNumber.countryHint', {
=======
                  id={fieldId("phone")}
                  name="phoneNumber"
                  label={t("profile.fields.phoneNumber.label")}
                  hint={t("profile.fields.phoneNumber.hint")}
                  hintId={fieldId("phone-hint")}
                  value={profile.phoneNumber}
                  onNumberChange={(event) =>
                    updateProfileField("phoneNumber", event.target.value)
                  }
                  countryControlId={phoneCountryControlId}
                  countryHintId={phoneCountryHintId}
                  countryHint={t("profile.fields.phoneNumber.countryHint", {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                    dialCode: selectedPhoneCountry.dialCode,
                  })}
                  selectedCountry={selectedPhoneCountry}
                  options={phoneCountryOptions}
                  onCountrySelect={handlePhoneCountrySelect}
                />

                <FormField
<<<<<<< HEAD
                  htmlFor={fieldId('location')}
                  label={t('profile.fields.location.label')}
                  hint={t('profile.fields.location.hint')}
=======
                  htmlFor={fieldId("location")}
                  label={t("profile.fields.location.label")}
                  hint={t("profile.fields.location.hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  hintId={locationHintId}
                  error={profileErrors.locationQuery}
                  errorId={locationErrorId}
                  status={
                    <LocationLookup
                      statusId={locationStatusId}
                      loading={locationLookupLoading}
<<<<<<< HEAD
                      loadingText={t('profile.fields.location.loading')}
                      statusMessage={locationLookupMessage}
                      resolvedText={
                        resolvedLocation
                          ? t('profile.fields.location.resolved', {
                              location: resolvedLocation.displayName,
                            })
                          : ''
=======
                      loadingText={t("profile.fields.location.loading")}
                      statusMessage={locationLookupMessage}
                      resolvedText={
                        resolvedLocation
                          ? t("profile.fields.location.resolved", {
                              location: resolvedLocation.displayName,
                            })
                          : ""
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                      }
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
<<<<<<< HEAD
                      id={fieldId('location')}
=======
                      id={fieldId("location")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                      type="text"
                      name="location"
                      autoComplete="off"
                      enterKeyHint="next"
                      value={profile.locationQuery}
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
                        profileErrors.locationQuery ? locationErrorId : '',
                      ]
                        .filter(Boolean)
                        .join(' ')}
                      aria-errormessage={profileErrors.locationQuery ? locationErrorId : undefined}
                      aria-invalid={profileErrors.locationQuery ? 'true' : 'false'}
                      aria-autocomplete="list"
                      aria-controls={locationSuggestions.length > 0 ? locationListId : undefined}
=======
                        profileErrors.locationQuery ? locationErrorId : "",
                      ]
                        .filter(Boolean)
                        .join(" ")}
                      aria-errormessage={
                        profileErrors.locationQuery
                          ? locationErrorId
                          : undefined
                      }
                      aria-invalid={
                        profileErrors.locationQuery ? "true" : "false"
                      }
                      aria-autocomplete="list"
                      aria-controls={
                        locationSuggestions.length > 0
                          ? locationListId
                          : undefined
                      }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                      aria-activedescendant={
                        activeSuggestionIndex >= 0
                          ? getLocationSuggestionId(activeSuggestionIndex)
                          : undefined
                      }
                      aria-expanded={locationSuggestions.length > 0}
                      role="combobox"
                    />
                  </div>
                </FormField>

                <FormField
<<<<<<< HEAD
                  htmlFor={fieldId('default-language')}
                  label={t('profile.fields.defaultLanguage.label')}
                >
                  <div className="relative">
                    <select
                      id={fieldId('default-language')}
                      name="defaultLanguage"
                      value={profile.defaultLanguage}
                      onChange={(event) =>
                        updateProfileField('defaultLanguage', event.target.value as Locale)
=======
                  htmlFor={fieldId("default-language")}
                  label={t("profile.fields.defaultLanguage.label")}
                >
                  <div className="relative">
                    <select
                      id={fieldId("default-language")}
                      name="defaultLanguage"
                      value={profile.defaultLanguage}
                      onChange={(event) =>
                        updateProfileField(
                          "defaultLanguage",
                          event.target.value as Locale,
                        )
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                      }
                      className="field-control field-control--trailing appearance-none"
                    >
                      {languageOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                    <span className="inline-trailing-icon">
                      <Languages size={16} aria-hidden="true" />
                    </span>
                  </div>
                </FormField>
              </AuthFormSection>

              <FormActions className="mt-auto border-t border-border-subtle pt-4 sm:grid sm:grid-cols-2">
<<<<<<< HEAD
                <ActionButton type="submit" className="gap-2" disabled={!isDirty}>
                  <Save size={16} aria-hidden="true" />
                  {t('profile.actions.save')}
=======
                <ActionButton
                  type="submit"
                  className="gap-2"
                  disabled={!isDirty}
                >
                  <Save size={16} aria-hidden="true" />
                  {t("profile.actions.save")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                </ActionButton>
                <ActionButton
                  type="button"
                  variant="secondary"
                  className="gap-2"
                  disabled={!isDirty}
                  onClick={resetProfileChanges}
                >
<<<<<<< HEAD
                  {t('profile.actions.discard')}
=======
                  {t("profile.actions.discard")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                </ActionButton>
              </FormActions>
            </AuthForm>

            <AuthForm
              className="flex min-w-0 flex-col gap-4 border-t border-border-subtle pt-5 lg:border-l lg:border-t-0 lg:pl-5 lg:pt-0"
              onSubmit={handlePasswordSubmit}
            >
              <AuthFormSection>
                <div className="flex flex-col gap-1">
<<<<<<< HEAD
                  <p className="auth-progress-text">{t('profile.actions.changePassword')}</p>
                </div>

                <PasswordField
                  id={fieldId('current-password')}
                  name="currentPassword"
                  label={t('profile.fields.currentPassword.label')}
=======
                  <p className="auth-progress-text">
                    {t("profile.actions.changePassword")}
                  </p>
                </div>

                <PasswordField
                  id={fieldId("current-password")}
                  name="currentPassword"
                  label={t("profile.fields.currentPassword.label")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  value={currentPassword}
                  show={showCurrentPassword}
                  setShow={setShowCurrentPassword}
                  onChange={(value) => {
                    setCurrentPassword(value);
                    setPasswordErrors((current) => ({
                      ...current,
                      currentPassword: undefined,
                    }));
                    setStatusMessage(null);
                  }}
                  autoComplete="current-password"
                  error={passwordErrors.currentPassword}
<<<<<<< HEAD
                  errorId={fieldId('current-password-error')}
                  required
                  ariaLabelHide={t('profile.fields.currentPassword.hide', {
                    defaultValue: 'Hide current password',
                  })}
                  ariaLabelShow={t('profile.fields.currentPassword.show', {
                    defaultValue: 'Show current password',
=======
                  errorId={fieldId("current-password-error")}
                  required
                  ariaLabelHide={t("profile.fields.currentPassword.hide", {
                    defaultValue: "Hide current password",
                  })}
                  ariaLabelShow={t("profile.fields.currentPassword.show", {
                    defaultValue: "Show current password",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  })}
                />

                <PasswordField
<<<<<<< HEAD
                  id={fieldId('new-password')}
                  name="newPassword"
                  label={t('profile.fields.newPassword.label')}
                  hint={t('profile.fields.newPassword.hint')}
                  hintId={fieldId('new-password-hint')}
=======
                  id={fieldId("new-password")}
                  name="newPassword"
                  label={t("profile.fields.newPassword.label")}
                  hint={t("profile.fields.newPassword.hint")}
                  hintId={fieldId("new-password-hint")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  value={newPassword}
                  show={showNewPassword}
                  setShow={setShowNewPassword}
                  onChange={(value) => {
                    setNewPassword(value);
                    setPasswordErrors((current) => ({
                      ...current,
                      newPassword: undefined,
                    }));
                    setStatusMessage(null);
                  }}
                  autoComplete="new-password"
                  error={passwordErrors.newPassword}
<<<<<<< HEAD
                  errorId={fieldId('new-password-error')}
                  required
                  ariaLabelHide={t('profile.fields.newPassword.hide', {
                    defaultValue: 'Hide new password',
                  })}
                  ariaLabelShow={t('profile.fields.newPassword.show', {
                    defaultValue: 'Show new password',
=======
                  errorId={fieldId("new-password-error")}
                  required
                  ariaLabelHide={t("profile.fields.newPassword.hide", {
                    defaultValue: "Hide new password",
                  })}
                  ariaLabelShow={t("profile.fields.newPassword.show", {
                    defaultValue: "Show new password",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  })}
                />

                <PasswordField
<<<<<<< HEAD
                  id={fieldId('confirm-password')}
                  name="confirmPassword"
                  label={t('profile.fields.confirmPassword.label')}
=======
                  id={fieldId("confirm-password")}
                  name="confirmPassword"
                  label={t("profile.fields.confirmPassword.label")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  value={confirmPassword}
                  show={showConfirmPassword}
                  setShow={setShowConfirmPassword}
                  onChange={(value) => {
                    setConfirmPassword(value);
                    setPasswordErrors((current) => ({
                      ...current,
                      confirmPassword: undefined,
                    }));
                    setStatusMessage(null);
                  }}
                  autoComplete="new-password"
                  error={passwordErrors.confirmPassword}
<<<<<<< HEAD
                  errorId={fieldId('confirm-password-error')}
=======
                  errorId={fieldId("confirm-password-error")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                  required
                  ariaLabelHide="Hide confirmation password"
                  ariaLabelShow="Show confirmation password"
                />
              </AuthFormSection>

              <FormActions className="mt-auto border-t border-border-subtle pt-4">
                <ActionButton type="submit" className="gap-2">
                  <ShieldCheck size={16} aria-hidden="true" />
<<<<<<< HEAD
                  {t('profile.actions.updatePassword')}
=======
                  {t("profile.actions.updatePassword")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                </ActionButton>
              </FormActions>
            </AuthForm>
          </div>
        </AuthPage>
      </section>
    </main>
  );
}

export default ProfilePage;
