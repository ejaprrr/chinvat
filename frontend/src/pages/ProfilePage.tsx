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
  LogOut,
  Mail,
  MapPin,
  Phone,
  RotateCcw,
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
import { appRoutes } from "../router/paths";
import { ActionButton, ActionLink } from "../components/ui/Action";
import AuthPage from "../components/ui/AuthPage";
import FormField from "../components/ui/FormField";
import LanguageSwitcher from "../components/LanguageSwitcher";
import PasswordField from "../components/ui/PasswordField";
import PhoneNumberField from "../components/ui/PhoneNumberField";
import type { PhoneCountryOption } from "../components/ui/PhoneCountrySelect";
import TextInput from "../components/ui/TextInput";

type UserProfile = {
  username: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  location: string;
  defaultLanguage: Locale;
};

type ProfileErrors = Partial<
  Record<"username" | "fullName" | "email" | "location", string>
>;

type PasswordErrors = Partial<
  Record<"currentPassword" | "newPassword" | "confirmPassword", string>
>;

const initialProfile: UserProfile = {
  username: "maria.garcia",
  fullName: "Maria Garcia",
  phoneNumber: "+34 600 123 456",
  email: "maria.garcia@example.org",
  location: "Barcelona, Spain",
  defaultLanguage: "en",
};

type NormalizedLocation = {
  address?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  countryCode?: string;
  displayName: string;
  isPrecise: boolean;
};

const styles = {
  locationStatus: "text-[0.8125rem] leading-5 text-muted",
  suggestions:
    "mt-2 overflow-hidden rounded-xl border border-border-subtle bg-white shadow-sm",
  suggestion:
    "block w-full border-b border-border-subtle px-4 py-3 text-left text-sm text-ink transition last:border-b-0 hover:bg-surface-subtle focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  suggestionActive: "bg-surface-subtle",
  suggestionMeta: "mt-0.5 block text-[0.8125rem] leading-5 text-muted",
} as const;

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
  const [profile, setProfile] = useState<UserProfile>(initialProfile);
  const [savedProfile, setSavedProfile] = useState<UserProfile>(initialProfile);
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
    useState<NormalizedLocation | null>(null);
  const [locationSuggestions, setLocationSuggestions] = useState<
    NormalizedLocation[]
  >([]);
  const [locationLookupMessage, setLocationLookupMessage] = useState<
    string | null
  >(null);
  const [locationLookupLoading, setLocationLookupLoading] = useState(false);
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
    document.title = "Profile management | chinvat";
  }, []);

  const fieldId = (name: string) => `${uid}-${name}`;

  const titleId = fieldId("profile-title");
  const headerIntroId = fieldId("profile-intro");

  const locationHintId = fieldId("location-hint");
  const locationErrorId = fieldId("location-error");
  const locationStatusId = fieldId("location-status");
  const locationListId = fieldId("location-list");
  const phoneCountryControlId = fieldId("phone-country");
  const phoneCountryHintId = fieldId("phone-country-hint");
  const getLocationSuggestionId = (index: number) =>
    `${locationListId}-option-${index}`;

  const updateProfile = <Field extends keyof UserProfile>(
    field: Field,
    value: UserProfile[Field],
  ) => {
    setProfile((current) => ({ ...current, [field]: value }));
    setProfileErrors((current) => ({ ...current, [field]: undefined }));
    setStatusMessage(null);
  };

  const validateProfile = () => {
    const nextErrors: ProfileErrors = {};

    if (!profile.username.trim()) nextErrors.username = "Enter a username.";
    if (!profile.fullName.trim()) nextErrors.fullName = "Enter a full name.";

    if (!profile.email.trim()) {
      nextErrors.email = "Enter an email address.";
    } else if (!isValidEmail(profile.email)) {
      nextErrors.email = "Enter a valid email address.";
    }

    return nextErrors;
  };

  const handleLocationInputChange = (value: string) => {
    updateProfile("location", value);

    if (resolvedLocation && value.trim() !== resolvedLocation.displayName) {
      setResolvedLocation(null);
    }
  };

  const handleLocationSuggestionSelect = (suggestion: NormalizedLocation) => {
    updateProfile("location", suggestion.displayName);
    setResolvedLocation(suggestion);
    setLocationSuggestions([]);
    setLocationLookupMessage(null);
    setLocationLookupLoading(false);
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
      setLocationSuggestions([]);
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
    <main className="min-h-screen min-h-dvh bg-canvas">
      <section className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6">
        <div className="rounded-[1.25rem] border border-border-subtle bg-panel p-5 shadow-panel motion-safe:animate-panel-in sm:p-6">
          <AuthPage
            aria-labelledby={titleId}
            titleId={titleId}
            title="Profile management"
            introId={headerIntroId}
            intro="Manage your chinvat profile details."
            action={
              <div className="flex items-center gap-2">
                <LanguageSwitcher />
                <ActionLink
                  to={appRoutes.login}
                  variant="secondary"
                  className="w-auto gap-2"
                >
                  <LogOut size={16} aria-hidden="true" />
                  Sign out
                </ActionLink>
              </div>
            }
            status={
              statusMessage
                ? {
                    content: statusMessage.text,
                    tone:
                      statusMessage.tone === "warning"
                        ? "warning"
                        : statusMessage.tone === "critical"
                          ? "critical"
                          : "default",
                  }
                : null
            }
          >
            <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
              <form
                className="auth-form rounded-lg border border-border-subtle bg-panel p-5 shadow-sm"
                onSubmit={handleProfileSubmit}
                noValidate
              >
                <div className="flex flex-col gap-3 border-b border-border-subtle pb-5 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <h2 className="text-base font-semibold text-ink">
                      Profile details
                    </h2>
                    <p className="mt-1 text-sm leading-6 text-muted">
                      Update the information shown on your profile.
                    </p>
                  </div>
                  <div className="flex flex-col gap-2 sm:flex-row">
                    <ActionButton
                      type="button"
                      variant="secondary"
                      className="gap-2 sm:w-auto"
                      disabled={!isDirty}
                      onClick={resetProfileChanges}
                    >
                      <RotateCcw size={16} aria-hidden="true" />
                      Discard
                    </ActionButton>
                    <ActionButton
                      type="submit"
                      className="gap-2 sm:w-auto"
                      disabled={!isDirty}
                    >
                      <Save size={16} aria-hidden="true" />
                      Save profile
                    </ActionButton>
                  </div>
                </div>

                <div className="mt-5 auth-section-stack">
                  <TextInput
                    id={fieldId("username")}
                    name="username"
                    label="Username"
                    value={profile.username}
                    onChange={(event) =>
                      updateProfile("username", event.target.value)
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
                      updateProfile("fullName", event.target.value)
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
                      updateProfile("email", event.target.value)
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
                      updateProfile("phoneNumber", event.target.value)
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
                    hint="Search for your city or address."
                    hintId={locationHintId}
                    error={profileErrors.location}
                    errorId={locationErrorId}
                    status={
                      <div className="space-y-2">
                        <p
                          id={locationStatusId}
                          className={styles.locationStatus}
                          role="status"
                          aria-live="polite"
                          aria-atomic="true"
                        >
                          {locationLookupLoading
                            ? "Searching locations..."
                            : locationLookupMessage ||
                              (resolvedLocation
                                ? `Location set to ${resolvedLocation.displayName}.`
                                : "Type to see suggestions.")}
                        </p>

                        {locationSuggestions.length > 0 ? (
                          <div
                            id={locationListId}
                            role="listbox"
                            className={styles.suggestions}
                          >
                            {locationSuggestions.map((suggestion, index) => (
                              <div
                                id={getLocationSuggestionId(index)}
                                key={`${suggestion.displayName}-${index}`}
                                role="option"
                                aria-selected={index === activeSuggestionIndex}
                              >
                                <button
                                  type="button"
                                  className={
                                    index === activeSuggestionIndex
                                      ? `${styles.suggestion} ${styles.suggestionActive}`
                                      : styles.suggestion
                                  }
                                  onClick={() =>
                                    handleLocationSuggestionSelect(suggestion)
                                  }
                                >
                                  <span>{suggestion.displayName}</span>
                                  <span className={styles.suggestionMeta}>
                                    {[
                                      suggestion.address,
                                      suggestion.postalCode,
                                      suggestion.city,
                                      suggestion.country,
                                    ]
                                      .filter(Boolean)
                                      .join(" · ")}
                                  </span>
                                </button>
                              </div>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    }
                  >
                    <div className="relative">
                      <TextInput
                        id={fieldId("location")}
                        type="text"
                        name="location"
                        autoComplete="off"
                        enterKeyHint="next"
                        value={profile.location}
                        onChange={(event) =>
                          handleLocationInputChange(event.target.value)
                        }
                        onKeyDown={handleLocationKeyDown}
                        trailingIcon={<MapPin aria-hidden="true" size={16} />}
                        aria-describedby={[
                          locationHintId,
                          locationStatusId,
                          profileErrors.location ? locationErrorId : "",
                        ]
                          .filter(Boolean)
                          .join(" ")}
                        aria-errormessage={
                          profileErrors.location ? locationErrorId : undefined
                        }
                        aria-invalid={profileErrors.location ? "true" : "false"}
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
                        className="field-control field-control--trailing"
                        value={profile.defaultLanguage}
                        onChange={(event) =>
                          updateProfile(
                            "defaultLanguage",
                            event.target.value as Locale,
                          )
                        }
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
                </div>
              </form>

              <div className="space-y-6">
                <section className="rounded-lg border border-border-subtle bg-panel p-5 shadow-sm">
                  <h2 className="text-base font-semibold text-ink">
                    Profile summary
                  </h2>
                  <p className="mt-1 text-sm text-muted">
                    {savedProfile.fullName} · @{savedProfile.username}
                  </p>
                  <div className="mt-4 space-y-3 text-sm">
                    <div className="rounded-lg border border-border-subtle bg-surface-subtle p-3">
                      <p className="text-[0.75rem] font-medium text-muted">
                        Email
                      </p>
                      <p className="mt-1 text-sm font-semibold text-ink">
                        {savedProfile.email}
                      </p>
                    </div>
                    <div className="rounded-lg border border-border-subtle bg-surface-subtle p-3">
                      <p className="text-[0.75rem] font-medium text-muted">
                        Phone
                      </p>
                      <p className="mt-1 text-sm font-semibold text-ink">
                        {savedProfile.phoneNumber || "Not set"}
                      </p>
                    </div>
                    <div className="rounded-lg border border-border-subtle bg-surface-subtle p-3">
                      <p className="text-[0.75rem] font-medium text-muted">
                        Location
                      </p>
                      <p className="mt-1 text-sm font-semibold text-ink">
                        {savedProfile.location || "Not set"}
                      </p>
                    </div>
                    <div className="rounded-lg border border-border-subtle bg-surface-subtle p-3">
                      <p className="text-[0.75rem] font-medium text-muted">
                        Language
                      </p>
                      <p className="mt-1 text-sm font-semibold text-ink">
                        {languageLabels[savedProfile.defaultLanguage]}
                      </p>
                    </div>
                  </div>
                </section>

                <form
                  className="auth-form rounded-lg border border-border-subtle bg-panel p-5 shadow-sm"
                  onSubmit={handlePasswordSubmit}
                  noValidate
                >
                  <div className="border-b border-border-subtle pb-5">
                    <h2 className="text-base font-semibold text-ink">
                      Password
                    </h2>
                    <p className="mt-1 text-sm leading-6 text-muted">
                      Update your sign-in password.
                    </p>
                  </div>

                  <div className="mt-5 auth-section-stack">
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

                    <div className="flex flex-col gap-2 border-t border-border-subtle pt-5 sm:flex-row sm:justify-end">
                      <ActionButton type="submit" className="gap-2 sm:w-auto">
                        <ShieldCheck size={16} aria-hidden="true" />
                        Update password
                      </ActionButton>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </AuthPage>
        </div>
      </section>
    </main>
  );
}

export default ProfilePage;
