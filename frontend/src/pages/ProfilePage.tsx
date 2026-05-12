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

function isValidEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function countryCodeToFlag(countryCode: CountryCode) {
  return countryCode.replace(/./g, (char) =>
    String.fromCodePoint(127397 + char.charCodeAt(0)),
  );
}

function buildPhoneCountryOptions(language: string) {
  const displayNames = new Intl.DisplayNames([language, "en"], {
    type: "region",
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
    const locale = new Intl.Locale(language || "en").maximize();
    const region = locale.region as CountryCode | undefined;

    if (region && getCountries().includes(region)) {
      return region;
    }
  } catch {
    return "ES";
  }

  return "ES";
}

function ProfilePage() {
  const { t } = useTranslation();
  const { user, changePassword } = useAuth();
  const { profile: backendProfile, saveProfile } = useProfile(user?.id);
  const uid = useId();
  const [profile, setProfile] = useState<ProfileForm>(initialProfile);
  const [savedProfile, setSavedProfile] = useState<ProfileForm>(initialProfile);
  const [profileErrors, setProfileErrors] = useState<ProfileErrors>({});
  const [passwordErrors, setPasswordErrors] = useState<PasswordErrors>({});
  const [statusMessage, setStatusMessage] = useState<{
    tone: "default" | "critical" | "warning";
    text: string;
  } | null>(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [phoneCountryCode, setPhoneCountryCode] = useState<CountryCode>(() =>
    getDefaultPhoneCountry(initialProfile.defaultLanguage),
  );
  const [resolvedLocation, setResolvedLocation] =
    useState<LocationSuggestion | null>(null);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

  useEffect(() => {
    if (!backendProfile) {
      return;
    }

    const timer = window.setTimeout(() => {
      const nextProfile: ProfileForm = {
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
      };

      setProfile(nextProfile);
      setSavedProfile(nextProfile);
      setResolvedLocation(
        backendProfile.addressLine ||
          backendProfile.city ||
          backendProfile.country
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
      Object.fromEntries(
        phoneCountryOptions.map((option) => [option.code, option]),
      ) as Record<string, PhoneCountryOption>,
    [phoneCountryOptions],
  );

  const selectedPhoneCountry =
    phoneCountryOptionsByCode[phoneCountryCode] ?? phoneCountryOptions[0];

  useDocumentTitle("meta.profilePageTitle");

  const geocodingQuery =
    resolvedLocation &&
    profile.locationQuery.trim() === resolvedLocation.displayName
      ? ""
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

  const updateProfileField = <Field extends keyof ProfileForm>(
    field: Field,
    value: ProfileForm[Field],
  ) => {
    setProfile((current) => ({ ...current, [field]: value }));
    setProfileErrors((current) => ({ ...current, [field]: undefined }));
    setStatusMessage(null);
  };

  const validateProfile = () => {
    const errors: ProfileErrors = {};

    if (!profile.username.trim())
      errors.username = t("profile.fields.username.required");
    if (!profile.fullName.trim())
      errors.fullName = t("profile.fields.fullName.required");

    if (!profile.email.trim()) {
      errors.email = t("profile.fields.email.required");
    } else if (!isValidEmail(profile.email)) {
      errors.email = t("profile.fields.email.invalid");
    }

    if (!profile.locationQuery.trim()) {
      errors.locationQuery = t("profile.fields.location.required");
    }

    return errors;
  };

  const handleLocationInputChange = (value: string) => {
    updateProfileField("locationQuery", value);

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

    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current < locationSuggestions.length - 1 ? current + 1 : 0,
      );
      return;
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();
      setActiveSuggestionIndex((current) =>
        current > 0 ? current - 1 : locationSuggestions.length - 1,
      );
      return;
    }

    if (event.key === "Enter" && activeSuggestionIndex >= 0) {
      event.preventDefault();
      handleLocationSuggestionSelect(
        locationSuggestions[activeSuggestionIndex],
      );
      return;
    }

    if (event.key === "Escape") {
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
        tone: "critical",
        text: t("profile.status.reviewFields"),
      });
      return;
    }

    void (async () => {
      try {
        if (!user?.id) {
          throw new Error("User not authenticated");
        }

        const updated = await saveProfile({
          username: profile.username.trim(),
          fullName: profile.fullName.trim(),
          phoneNumber: profile.phoneNumber.trim() || undefined,
          userType: "INDIVIDUAL",
          accessLevel: "NORMAL",
          addressLine: resolvedLocation?.address,
          postalCode: resolvedLocation?.postalCode,
          city: resolvedLocation?.city,
          country: resolvedLocation?.country,
          defaultLanguage: profile.defaultLanguage,
        });

        const nextProfile: ProfileForm = {
          username: updated.username,
          fullName: updated.fullName,
          phoneNumber: updated.phoneNumber ?? "",
          email: updated.email,
          defaultLanguage: updated.defaultLanguage as Locale,
          locationQuery: [updated.addressLine, updated.city, updated.country]
            .filter(Boolean)
            .join(", "),
        };

        setProfile(nextProfile);
        setSavedProfile(nextProfile);
        setStatusMessage({
          tone: "default",
          text: t("profile.status.saveSuccess"),
        });
      } catch (error) {
        setStatusMessage({
          tone: "critical",
          text:
            error instanceof Error
              ? error.message
              : t("profile.status.saveError"),
        });
      }
    })();
  };

  const resetProfileChanges = () => {
    setProfile(savedProfile);
    setProfileErrors({});
    setStatusMessage({
      tone: "warning",
      text: t("profile.status.changesDiscarded"),
    });
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: PasswordErrors = {};

    if (!currentPassword)
      nextErrors.currentPassword = t("profile.fields.currentPassword.required");
    if (!newPassword) {
      nextErrors.newPassword = t("profile.fields.newPassword.required");
    } else if (newPassword.length < 8) {
      nextErrors.newPassword = t("profile.fields.newPassword.length");
    }
    if (newPassword !== confirmPassword) {
      nextErrors.confirmPassword = t("profile.fields.confirmPassword.mismatch");
    }

    setPasswordErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setStatusMessage({
        tone: "critical",
        text: t("profile.status.reviewPasswordFields"),
      });
      return;
    }

    void (async () => {
      try {
        await changePassword(currentPassword, newPassword);
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
        setStatusMessage({
          tone: "default",
          text: t("profile.status.passwordChangeSuccess"),
        });
      } catch (error) {
        setStatusMessage({
          tone: "critical",
          text:
            error instanceof Error
              ? error.message
              : t("profile.status.passwordChangeError"),
        });
      }
    })();
  };

  return (
    <main className="auth-popup-shell items-center bg-canvas py-6 lg:py-8">
      <section className="auth-popup max-w-6xl p-4 sm:p-5 lg:p-6">
        <AuthPage
          aria-labelledby={fieldId("title")}
          action={
            <div className="auth-floating-language">
              <LanguageSwitcher />
            </div>
          }
          titleId={fieldId("title")}
          title={t("profile.title")}
          intro={t("profile.intro")}
          status={
            statusMessage
              ? {
                  content: statusMessage.text,
                  tone: statusMessage.tone,
                }
              : null
          }
        >
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_20rem] lg:items-stretch xl:grid-cols-[minmax(0,1fr)_21rem]">
            <AuthForm
              className="flex min-w-0 flex-col gap-4"
              onSubmit={handleProfileSubmit}
            >
              <AuthFormSection className="grid gap-3.5 md:grid-cols-2">
                <div className="flex flex-col gap-1 md:col-span-2">
                  <p className="auth-progress-text">Account details</p>
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
                  required
                  trailingIcon={<UserRound size={16} aria-hidden="true" />}
                />

                <TextInput
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
                  required
                />

                <TextInput
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
                  required
                  trailingIcon={<Mail size={16} aria-hidden="true" />}
                />

                <PhoneNumberField
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
                    dialCode: selectedPhoneCountry.dialCode,
                  })}
                  selectedCountry={selectedPhoneCountry}
                  options={phoneCountryOptions}
                  onCountrySelect={handlePhoneCountrySelect}
                />

                <FormField
                  htmlFor={fieldId("location")}
                  label={t("profile.fields.location.label")}
                  hint={t("profile.fields.location.hint")}
                  hintId={locationHintId}
                  error={profileErrors.locationQuery}
                  errorId={locationErrorId}
                  status={
                    <LocationLookup
                      statusId={locationStatusId}
                      loading={locationLookupLoading}
                      loadingText={t("profile.fields.location.loading")}
                      statusMessage={locationLookupMessage}
                      resolvedText={
                        resolvedLocation
                          ? t("profile.fields.location.resolved", {
                              location: resolvedLocation.displayName,
                            })
                          : ""
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
                      id={fieldId("location")}
                      type="text"
                      name="location"
                      autoComplete="off"
                      enterKeyHint="next"
                      value={profile.locationQuery}
                      onChange={(event) =>
                        handleLocationInputChange(event.target.value)
                      }
                      onKeyDown={handleLocationKeyDown}
                      trailingIcon={<MapPin aria-hidden="true" size={16} />}
                      aria-describedby={[
                        locationHintId,
                        locationStatusId,
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
                <ActionButton
                  type="submit"
                  className="gap-2"
                  disabled={!isDirty}
                >
                  <Save size={16} aria-hidden="true" />
                  {t("profile.actions.save")}
                </ActionButton>
                <ActionButton
                  type="button"
                  variant="secondary"
                  className="gap-2"
                  disabled={!isDirty}
                  onClick={resetProfileChanges}
                >
                  {t("profile.actions.discard")}
                </ActionButton>
              </FormActions>
            </AuthForm>

            <AuthForm
              className="flex min-w-0 flex-col gap-4 border-t border-border-subtle pt-5 lg:border-l lg:border-t-0 lg:pl-5 lg:pt-0"
              onSubmit={handlePasswordSubmit}
            >
              <AuthFormSection>
                <div className="flex flex-col gap-1">
                  <p className="auth-progress-text">
                    {t("profile.actions.changePassword")}
                  </p>
                </div>

                <PasswordField
                  id={fieldId("current-password")}
                  name="currentPassword"
                  label={t("profile.fields.currentPassword.label")}
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
                  errorId={fieldId("current-password-error")}
                  required
                  ariaLabelHide={t("profile.fields.currentPassword.hide", {
                    defaultValue: "Hide current password",
                  })}
                  ariaLabelShow={t("profile.fields.currentPassword.show", {
                    defaultValue: "Show current password",
                  })}
                />

                <PasswordField
                  id={fieldId("new-password")}
                  name="newPassword"
                  label={t("profile.fields.newPassword.label")}
                  hint={t("profile.fields.newPassword.hint")}
                  hintId={fieldId("new-password-hint")}
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
                  errorId={fieldId("new-password-error")}
                  required
                  ariaLabelHide={t("profile.fields.newPassword.hide", {
                    defaultValue: "Hide new password",
                  })}
                  ariaLabelShow={t("profile.fields.newPassword.show", {
                    defaultValue: "Show new password",
                  })}
                />

                <PasswordField
                  id={fieldId("confirm-password")}
                  name="confirmPassword"
                  label={t("profile.fields.confirmPassword.label")}
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
                  errorId={fieldId("confirm-password-error")}
                  required
                  ariaLabelHide="Hide confirmation password"
                  ariaLabelShow="Show confirmation password"
                />
              </AuthFormSection>

              <FormActions className="mt-auto border-t border-border-subtle pt-4">
                <ActionButton type="submit" className="gap-2">
                  <ShieldCheck size={16} aria-hidden="true" />
                  {t("profile.actions.updatePassword")}
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
