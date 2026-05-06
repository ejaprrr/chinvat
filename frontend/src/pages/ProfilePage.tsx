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
import { languageLabels, type Locale } from "../i18n";
import { ActionButton } from "../components/actions/Action";
import AuthPage from "../components/auth/AuthPage";
import {
  AuthForm,
  AuthFormSection,
  FormActions,
} from "../components/auth/AuthForm";
import FormField from "../components/fields/FormField";
import LocationLookup, {
  type LocationSuggestion,
} from "../components/fields/LocationLookup";
import PasswordField from "../components/fields/PasswordField";
import PhoneNumberField from "../components/fields/PhoneNumberField";
import type { PhoneCountryOption } from "../components/fields/PhoneCountrySelect";
import TextInput from "../components/fields/TextInput";
import LanguageSwitcher from "../components/i18n/LanguageSwitcher";

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

type NormalizedLocation = {
  address?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  countryCode?: string;
  displayName: string;
  isPrecise: boolean;
};

const locationCatalog: NormalizedLocation[] = [
  {
    displayName: "Barcelona, Spain",
    address: "Placa de Catalunya",
    postalCode: "08002",
    city: "Barcelona",
    country: "Spain",
    countryCode: "ES",
    isPrecise: true,
  },
  {
    displayName: "Madrid, Spain",
    address: "Gran Via 32",
    postalCode: "28013",
    city: "Madrid",
    country: "Spain",
    countryCode: "ES",
    isPrecise: true,
  },
  {
    displayName: "Valencia, Spain",
    address: "Carrer de Colon 18",
    postalCode: "46004",
    city: "Valencia",
    country: "Spain",
    countryCode: "ES",
    isPrecise: true,
  },
  {
    displayName: "Lisbon, Portugal",
    address: "Rua Augusta 122",
    postalCode: "1100-053",
    city: "Lisbon",
    country: "Portugal",
    countryCode: "PT",
    isPrecise: true,
  },
];

const initialProfile: ProfileForm = {
  username: "maria.garcia",
  fullName: "Maria Garcia",
  phoneNumber: "+34 600 123 456",
  email: "maria.garcia@example.org",
  defaultLanguage: "en",
  locationQuery: "Barcelona, Spain",
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
    useState<NormalizedLocation | null>(locationCatalog[0] ?? null);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

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

  useEffect(() => {
    document.title = "Profile | chinvat";
  }, []);

  const locationSuggestions = useMemo(() => {
    const query = profile.locationQuery.trim();

    if (!query) {
      return [];
    }

    if (resolvedLocation && resolvedLocation.displayName === query) {
      return [];
    }

    const normalizedQuery = query.toLowerCase();
    return locationCatalog
      .filter((item) => {
        return (
          item.displayName.toLowerCase().includes(normalizedQuery) ||
          item.city?.toLowerCase().includes(normalizedQuery) ||
          item.country?.toLowerCase().includes(normalizedQuery)
        );
      })
      .slice(0, 5);
  }, [profile.locationQuery, resolvedLocation]);

  const locationLookupMessage = useMemo(() => {
    const query = profile.locationQuery.trim();

    if (!query) {
      return null;
    }

    if (resolvedLocation && resolvedLocation.displayName === query) {
      return null;
    }

    return locationSuggestions.length ? null : "No locations found.";
  }, [profile.locationQuery, resolvedLocation, locationSuggestions]);

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

    if (!profile.username.trim()) errors.username = "Enter a username.";
    if (!profile.fullName.trim()) errors.fullName = "Enter a full name.";

    if (!profile.email.trim()) {
      errors.email = "Enter an email address.";
    } else if (!isValidEmail(profile.email)) {
      errors.email = "Enter a valid email address.";
    }

    if (!profile.locationQuery.trim()) {
      errors.locationQuery = "Search for your location.";
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
        text: "Review the highlighted profile fields.",
      });
      return;
    }

    setSavedProfile(profile);
    setStatusMessage({
      tone: "default",
      text: "Profile saved in the UI preview. Connect this action to your backend when ready.",
    });
  };

  const resetProfileChanges = () => {
    setProfile(savedProfile);
    setProfileErrors({});
    setStatusMessage({
      tone: "warning",
      text: "Unsaved profile changes were discarded.",
    });
  };

  const handlePasswordSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: PasswordErrors = {};

    if (!currentPassword)
      nextErrors.currentPassword = "Enter the current password.";
    if (!newPassword) {
      nextErrors.newPassword = "Enter a new password.";
    } else if (newPassword.length < 8) {
      nextErrors.newPassword = "Use at least 8 characters.";
    }
    if (newPassword !== confirmPassword) {
      nextErrors.confirmPassword = "The passwords do not match.";
    }

    setPasswordErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setStatusMessage({
        tone: "critical",
        text: "Review the highlighted password fields.",
      });
      return;
    }

    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setStatusMessage({
      tone: "default",
      text: "Password updated in the UI preview. Connect this action to your backend when ready.",
    });
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
          title="Profile"
          intro="Update your account details and password."
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
                  label="Username"
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
                  label="Full name"
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
                  label="Email"
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
                  label="Phone number"
                  hint="Use the number you can access right now."
                  hintId={fieldId("phone-hint")}
                  value={profile.phoneNumber}
                  onNumberChange={(event) =>
                    updateProfileField("phoneNumber", event.target.value)
                  }
                  countryControlId={phoneCountryControlId}
                  countryHintId={phoneCountryHintId}
                  countryHint={`Selected dial code ${selectedPhoneCountry.dialCode}.`}
                  selectedCountry={selectedPhoneCountry}
                  options={phoneCountryOptions}
                  onCountrySelect={handlePhoneCountrySelect}
                />

                <FormField
                  htmlFor={fieldId("location")}
                  label="Location"
                  hint="Search and select a location to fill address details."
                  hintId={locationHintId}
                  error={profileErrors.locationQuery}
                  errorId={locationErrorId}
                  status={
                    <LocationLookup
                      statusId={locationStatusId}
                      loadingText="Searching locations..."
                      statusMessage={locationLookupMessage}
                      resolvedText={
                        resolvedLocation
                          ? `Location set to ${resolvedLocation.displayName}.`
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
                  label="Default language"
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
                  Save changes
                </ActionButton>
                <ActionButton
                  type="button"
                  variant="secondary"
                  className="gap-2"
                  disabled={!isDirty}
                  onClick={resetProfileChanges}
                >
                  Discard changes
                </ActionButton>
              </FormActions>
            </AuthForm>

            <AuthForm
              className="flex min-w-0 flex-col gap-4 border-t border-border-subtle pt-5 lg:border-l lg:border-t-0 lg:pl-5 lg:pt-0"
              onSubmit={handlePasswordSubmit}
            >
              <AuthFormSection>
                <div className="flex flex-col gap-1">
                  <p className="auth-progress-text">Change password</p>
                </div>

                <PasswordField
                  id={fieldId("current-password")}
                  name="currentPassword"
                  label="Current password"
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
                  ariaLabelHide="Hide current password"
                  ariaLabelShow="Show current password"
                />

                <PasswordField
                  id={fieldId("new-password")}
                  name="newPassword"
                  label="New password"
                  hint="Use at least 8 characters."
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
                  ariaLabelHide="Hide new password"
                  ariaLabelShow="Show new password"
                />

                <PasswordField
                  id={fieldId("confirm-password")}
                  name="confirmPassword"
                  label="Confirm new password"
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
                  Update password
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
