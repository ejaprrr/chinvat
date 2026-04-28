import {
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import {
  Eye,
  EyeOff,
  MapPin,
  ShieldCheck,
  TriangleAlert,
  Upload,
  UserRound,
} from "lucide-react";
import {
  getCountries,
  getCountryCallingCode,
  type CountryCode,
} from "libphonenumber-js/min";
import { useTranslation } from "react-i18next";
import { registerUser } from "../api/auth";
import { languageLabels, type Locale } from "../i18n";
import { cx } from "../lib/cx";
import { appRoutes } from "../router/paths";
import { ActionButton, ActionLink } from "./ui/Action";
import AuthPageHeader from "./ui/AuthPageHeader";
import FormField from "./ui/FormField";
import LanguageSwitcher from "./LanguageSwitcher";

type Step = "identity" | "contact" | "security" | "certificate" | "done";

type FormValues = {
  username: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  defaultLanguage: Locale;
  password: string;
  confirmPassword: string;
};

type FieldErrors = Partial<Record<keyof FormValues | "location", string>>;

type StatusMessage =
  | {
      text: string;
      tone: "default" | "critical";
    }
  | null;

type PhotonProperties = {
  name?: string;
  street?: string;
  housenumber?: string;
  postcode?: string;
  city?: string;
  district?: string;
  county?: string;
  state?: string;
  country?: string;
  countrycode?: string;
};

type PhotonFeature = {
  properties?: PhotonProperties;
};

type PhotonResponse = {
  features?: PhotonFeature[];
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

type PhoneCountryOption = {
  code: CountryCode;
  dialCode: string;
  flag: string;
  label: string;
};

const passwordMinLength = 8;
const locationQueryMinLength = 3;

const styles = {
  root: "flex flex-col gap-4.5",
  progressText:
    "text-[0.6875rem] font-semibold uppercase tracking-[0.12em] text-muted-soft",
  form: "flex flex-col gap-3.5",
  sectionStack: "flex flex-col gap-3.5",
  inputBase:
    "block min-h-12 w-full rounded-xl border bg-white px-4 py-3 text-sm text-ink shadow-sm outline-none transition disabled:cursor-not-allowed disabled:opacity-60",
  inputDefault:
    "border-border-subtle focus:border-brand-500 focus:ring-3 focus:ring-brand-500/15",
  inputError:
    "border-danger-200 focus:border-danger-700 focus:ring-3 focus:ring-danger-700/10",
  iconButton:
    "absolute inset-y-0 right-0 inline-flex min-h-12 min-w-12 items-center justify-center rounded-r-xl px-3 text-muted transition hover:bg-surface-hover hover:text-ink focus-visible:z-10 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  statusBase:
    "rounded-2xl border px-4 py-3 flex items-start gap-2 text-[0.8125rem] leading-5",
  statusDefault: "border-border-subtle bg-surface-subtle text-muted",
  statusCritical: "border-danger-200 bg-danger-50 text-danger-700",
  statusIcon: "mt-0.5 shrink-0",
  actions: "flex flex-col gap-2",
  helperText: "text-[0.8125rem] leading-5 text-muted",
  fileCard:
    "flex min-h-12 items-center gap-3 rounded-xl border border-dashed border-border-subtle bg-white px-4 py-3 text-sm text-ink shadow-sm",
  fileMeta: "min-w-0 flex-1",
  fileName: "truncate font-medium text-ink",
  fileEmpty: "text-muted",
  requiredMark: "text-brand-600",
  completion: "flex items-start gap-2 text-[0.8125rem] leading-5 text-muted",
  completionIcon: "mt-0.5 shrink-0 text-brand-600",
  locationStatus: "text-[0.8125rem] leading-5 text-muted",
  suggestions:
    "mt-2 overflow-hidden rounded-xl border border-border-subtle bg-white shadow-sm",
  suggestion:
    "block w-full border-b border-border-subtle px-4 py-3 text-left text-sm text-ink transition last:border-b-0 hover:bg-surface-subtle focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  suggestionActive: "bg-surface-subtle",
  suggestionMeta: "mt-0.5 block text-[0.8125rem] leading-5 text-muted",
  phoneRow: "flex items-stretch gap-2",
  phonePrefix: "relative w-[11.5rem] shrink-0",
  phonePrefixSuggestions:
    "absolute left-0 right-0 top-[calc(100%+0.5rem)] z-20 overflow-hidden rounded-xl border border-border-subtle bg-white shadow-sm",
  phonePrefixSuggestion:
    "block w-full border-b border-border-subtle px-3 py-3 text-left text-sm text-ink transition last:border-b-0 hover:bg-surface-subtle focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
  phoneNumberInput: "flex-1",
} as const;

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

function getDefaultPhoneCountry(language: string | undefined) {
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

function normalizePhoneNumber(phoneCountry: PhoneCountryOption, phoneNumber: string) {
  const digitsOnly = phoneNumber.replace(/[^\d]/g, "");

  if (!digitsOnly) {
    return undefined;
  }

  return `${phoneCountry.dialCode} ${digitsOnly}`;
}

function getInputClassName(hasError: boolean, hasTrailingButton = false) {
  return cx(
    styles.inputBase,
    hasTrailingButton ? "pr-12" : "",
    hasError ? styles.inputError : styles.inputDefault,
  );
}

function isValidEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function normalizeStreetAddress(properties?: PhotonProperties) {
  if (!properties) {
    return undefined;
  }

  const road = properties.street?.trim();
  const houseNumber = properties.housenumber?.trim();

  if (road && houseNumber) {
    return `${road} ${houseNumber}`;
  }

  return road || undefined;
}

function normalizeCity(properties?: PhotonProperties) {
  if (!properties) {
    return undefined;
  }

  return (
    properties.city?.trim() ||
    properties.district?.trim() ||
    properties.county?.trim() ||
    properties.state?.trim() ||
    undefined
  );
}

function hasPreciseStreetAddress(properties?: PhotonProperties) {
  return Boolean(properties?.street?.trim() && properties?.housenumber?.trim());
}

function normalizeLocation(feature: PhotonFeature): NormalizedLocation {
  const properties = feature.properties;
  return {
    address: normalizeStreetAddress(properties),
    postalCode: properties?.postcode?.trim() || undefined,
    city: normalizeCity(properties),
    country: properties?.country?.trim() || undefined,
    countryCode: properties?.countrycode?.trim()?.toUpperCase() || undefined,
    displayName: [
      normalizeStreetAddress(properties),
      properties?.postcode?.trim(),
      normalizeCity(properties),
      properties?.country?.trim(),
    ]
      .filter(Boolean)
      .join(", "),
    isPrecise: hasPreciseStreetAddress(properties),
  };
}

function scoreLocationMatch(location: NormalizedLocation, query: string) {
  const normalizedQuery = query.trim().toLowerCase();
  const normalizedName = location.displayName.toLowerCase();
  const normalizedAddress = location.address?.toLowerCase() || "";
  const queryHasNumber = /\d/.test(normalizedQuery);

  let score = 0;

  if (location.isPrecise) {
    score += 100;
  }

  if (normalizedName.startsWith(normalizedQuery)) {
    score += 30;
  }

  if (normalizedName.includes(normalizedQuery)) {
    score += 20;
  }

  if (normalizedAddress.includes(normalizedQuery)) {
    score += 25;
  }

  if (queryHasNumber && /\d/.test(normalizedAddress)) {
    score += 15;
  }

  return score;
}

async function searchLocations(
  query: string,
  signal?: AbortSignal,
): Promise<NormalizedLocation[]> {
  const endpoint = new URL("https://photon.komoot.io/api/");
  endpoint.searchParams.set("q", query);
  endpoint.searchParams.set("limit", "8");
  endpoint.searchParams.set("lang", "en");

  const response = await fetch(endpoint.toString(), {
    headers: {
      Accept: "application/json",
    },
    signal,
  });

  if (!response.ok) {
    throw new Error(`Location lookup failed with status ${response.status}.`);
  }

  const payload = (await response.json()) as PhotonResponse;
  return (payload.features ?? [])
    .map(normalizeLocation)
    .filter(
      (location) => Boolean(location.displayName) && isPreciseLocation(location),
    )
    .sort(
      (left, right) =>
        scoreLocationMatch(right, query) - scoreLocationMatch(left, query),
    );
}

async function resolveLocation(query: string) {
  const trimmedQuery = query.trim();

  if (!trimmedQuery) {
    return null;
  }

  try {
    const [firstMatch] = await searchLocations(trimmedQuery);

    if (firstMatch) {
      return firstMatch;
    }
  } catch {
    return null;
  }

  return null;
}

function isPreciseLocation(location: NormalizedLocation | null) {
  return Boolean(location?.isPrecise && location.address && /\d/.test(location.address));
}

function hasHouseNumberInQuery(query: string) {
  return /\d/.test(query);
}

function RegisterForm() {
  const { t, i18n } = useTranslation();
  const [currentStep, setCurrentStep] = useState<Step>("identity");
  const [values, setValues] = useState<FormValues>(() => ({
    ...initialValues,
    defaultLanguage:
      i18n.resolvedLanguage && i18n.resolvedLanguage in languageLabels
        ? (i18n.resolvedLanguage as Locale)
        : "en",
  }));
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [statusMessage, setStatusMessage] = useState<StatusMessage>(null);
  const [certificateFile, setCertificateFile] = useState<File | null>(null);
  const [locationQuery, setLocationQuery] = useState("");
  const [resolvedLocation, setResolvedLocation] = useState<NormalizedLocation | null>(null);
  const [locationSuggestions, setLocationSuggestions] = useState<NormalizedLocation[]>([]);
  const [locationLookupLoading, setLocationLookupLoading] = useState(false);
  const [locationLookupMessage, setLocationLookupMessage] = useState<string | null>(null);
  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [phoneCountryCode, setPhoneCountryCode] = useState(() =>
    getDefaultPhoneCountry(i18n.resolvedLanguage || i18n.language),
  );
  const [phoneCountryQuery, setPhoneCountryQuery] = useState("");
  const [phoneCountrySuggestions, setPhoneCountrySuggestions] = useState<
    PhoneCountryOption[]
  >([]);
  const [activePhoneCountrySuggestionIndex, setActivePhoneCountrySuggestionIndex] =
    useState(-1);

  const usernameInputRef = useRef<HTMLInputElement>(null);
  const phoneCountryInputRef = useRef<HTMLInputElement>(null);
  const emailInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);
  const stepHeadingRef = useRef<HTMLHeadingElement>(null);
  const previousStepRef = useRef<Step>(currentStep);

  const progressId = useId();
  const headerIntroId = useId();
  const usernameHintId = useId();
  const fullNameHintId = useId();
  const phoneHintId = useId();
  const phoneCountryHintId = useId();
  const phoneCountryListId = useId();
  const emailHintId = useId();
  const locationHintId = useId();
  const locationStatusId = useId();
  const locationListId = useId();
  const defaultLanguageHintId = useId();
  const passwordHintId = useId();
  const confirmPasswordHintId = useId();
  const certificateHintId = useId();

  const usernameErrorId = useId();
  const fullNameErrorId = useId();
  const emailErrorId = useId();
  const locationErrorId = useId();
  const passwordErrorId = useId();
  const confirmPasswordErrorId = useId();

  const languageOptions = useMemo(
    () =>
      Object.entries(languageLabels).map(([value, label]) => ({
        value: value as Locale,
        label,
      })),
    [],
  );
  const phoneCountryOptions = useMemo(
    () => buildPhoneCountryOptions(i18n.resolvedLanguage || i18n.language || "en"),
    [i18n.language, i18n.resolvedLanguage],
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
    setPhoneCountryQuery(
      `${selectedPhoneCountry.flag} ${selectedPhoneCountry.dialCode}`,
    );
  }, [selectedPhoneCountry.code, selectedPhoneCountry.dialCode, selectedPhoneCountry.flag]);

  useEffect(() => {
    const trimmedQuery = phoneCountryQuery.trim().toLowerCase();

    if (!trimmedQuery) {
      setPhoneCountrySuggestions([]);
      setActivePhoneCountrySuggestionIndex(-1);
      return;
    }

    const canonicalSelected = `${selectedPhoneCountry.flag} ${selectedPhoneCountry.dialCode}`.toLowerCase();
    if (trimmedQuery === canonicalSelected) {
      setPhoneCountrySuggestions([]);
      setActivePhoneCountrySuggestionIndex(-1);
      return;
    }

    const suggestions = phoneCountryOptions.filter((option) => {
      const haystack = [
        option.label.toLowerCase(),
        option.dialCode.toLowerCase(),
        option.code.toLowerCase(),
      ];

      return haystack.some((value) => value.includes(trimmedQuery));
    });

    setPhoneCountrySuggestions(suggestions.slice(0, 8));
    setActivePhoneCountrySuggestionIndex(suggestions.length > 0 ? 0 : -1);
  }, [phoneCountryOptions, phoneCountryQuery, selectedPhoneCountry]);

  useEffect(() => {
    if (currentStep !== "contact") {
      return;
    }

    const trimmedQuery = locationQuery.trim();

    if (trimmedQuery.length < locationQueryMinLength) {
      setLocationSuggestions([]);
      setLocationLookupLoading(false);
      setLocationLookupMessage(null);
      setActiveSuggestionIndex(-1);
      return;
    }

    if (resolvedLocation && trimmedQuery === resolvedLocation.displayName) {
      setLocationSuggestions([]);
      setLocationLookupLoading(false);
      setLocationLookupMessage(null);
      return;
    }

    const controller = new AbortController();
    const timeoutId = window.setTimeout(async () => {
      setLocationLookupLoading(true);
      setLocationLookupMessage(null);

      if (!hasHouseNumberInQuery(trimmedQuery)) {
        setLocationSuggestions([]);
        setActiveSuggestionIndex(-1);
        setLocationLookupLoading(false);
        setLocationLookupMessage(t("auth.register.fields.location.needsHouseNumber"));
        return;
      }

      try {
        const suggestions = await searchLocations(trimmedQuery, controller.signal);
        setLocationSuggestions(suggestions);
        setActiveSuggestionIndex(suggestions.length > 0 ? 0 : -1);
        setLocationLookupMessage(
          suggestions.length === 0
            ? t("auth.register.fields.location.noExactResults")
            : t("auth.register.fields.location.selectExact"),
        );
      } catch (error) {
        if (!controller.signal.aborted) {
          setLocationSuggestions([]);
          setLocationLookupMessage(t("auth.register.fields.location.lookupError"));
        }
      } finally {
        if (!controller.signal.aborted) {
          setLocationLookupLoading(false);
        }
      }
    }, 250);

    return () => {
      controller.abort();
      window.clearTimeout(timeoutId);
    };
  }, [currentStep, locationQuery, resolvedLocation, t]);

  const stepOrder: Step[] = [
    "identity",
    "contact",
    "security",
    "certificate",
    "done",
  ];
  const currentStepIndex = stepOrder.indexOf(currentStep);
  const activeStepNumber = currentStep === "done" ? 4 : currentStepIndex + 1;

  const clearStatus = () => {
    setStatusMessage(null);
  };

  const setFieldValue = <K extends keyof FormValues>(
    key: K,
    value: FormValues[K],
  ) => {
    setValues((current) => ({
      ...current,
      [key]: value,
    }));
    setFieldErrors((current) => ({
      ...current,
      [key]: undefined,
    }));
    clearStatus();
  };

  const validateIdentityStep = () => {
    const nextErrors: FieldErrors = {};

    if (!values.username.trim()) {
      nextErrors.username = t("auth.register.fields.username.required");
    }

    if (!values.fullName.trim()) {
      nextErrors.fullName = t("auth.register.fields.fullName.required");
    }

    return nextErrors;
  };

  const validateContactStep = () => {
    const nextErrors: FieldErrors = {};
    const trimmedLocationQuery = locationQuery.trim();

    if (!values.email.trim()) {
      nextErrors.email = t("auth.register.fields.email.required");
    } else if (!isValidEmail(values.email.trim())) {
      nextErrors.email = t("auth.register.fields.email.invalid");
    }

    if (trimmedLocationQuery && !hasHouseNumberInQuery(trimmedLocationQuery)) {
      nextErrors.location = t("auth.register.fields.location.requiredExact");
    } else if (trimmedLocationQuery && !isPreciseLocation(resolvedLocation)) {
      nextErrors.location = t("auth.register.fields.location.requiredExact");
    }

    return nextErrors;
  };

  const validateSecurityStep = () => {
    const nextErrors: FieldErrors = {};

    if (!values.password) {
      nextErrors.password = t("auth.register.fields.password.required");
    } else if (values.password.length < passwordMinLength) {
      nextErrors.password = t("auth.register.fields.password.length", {
        count: passwordMinLength,
      });
    }

    if (!values.confirmPassword) {
      nextErrors.confirmPassword = t(
        "auth.register.fields.confirmPassword.required",
      );
    } else if (values.confirmPassword !== values.password) {
      nextErrors.confirmPassword = t(
        "auth.register.fields.confirmPassword.mismatch",
      );
    }

    return nextErrors;
  };

  const goToStep = (step: Step) => {
    setFieldErrors({});
    clearStatus();
    setCurrentStep(step);
  };

  const getFieldDescribedBy = (hintId: string, error?: string, errorId?: string) =>
    [hintId, error ? errorId : ""].filter(Boolean).join(" ");

  const getLocationSuggestionId = (index: number) =>
    `${locationListId}-option-${index}`;

  const getPhoneCountrySuggestionId = (index: number) =>
    `${phoneCountryListId}-option-${index}`;

  const handleIdentitySubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateIdentityStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      usernameInputRef.current?.focus();
      return;
    }

    goToStep("contact");
  };

  const handleContactSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateContactStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      emailInputRef.current?.focus();
      return;
    }

    goToStep("security");
  };

  const handleSecuritySubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateSecurityStep();
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      passwordInputRef.current?.focus();
      return;
    }

    goToStep("certificate");
  };

  const handleCertificateChange = (event: ChangeEvent<HTMLInputElement>) => {
    setCertificateFile(event.target.files?.[0] ?? null);
    clearStatus();
  };

  const handleLocationInputChange = (value: string) => {
    setLocationQuery(value);
    setFieldErrors((current) => ({
      ...current,
      location: undefined,
    }));
    clearStatus();

    if (resolvedLocation && value.trim() !== resolvedLocation.displayName) {
      setResolvedLocation(null);
    }
  };

  const handlePhoneCountrySelect = (option: PhoneCountryOption) => {
    setPhoneCountryCode(option.code);
    setPhoneCountryQuery(`${option.flag} ${option.dialCode}`);
    setPhoneCountrySuggestions([]);
    setActivePhoneCountrySuggestionIndex(-1);
  };

  const handlePhoneCountryKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (phoneCountrySuggestions.length === 0) {
      return;
    }

    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActivePhoneCountrySuggestionIndex((current) =>
        current < phoneCountrySuggestions.length - 1 ? current + 1 : 0,
      );
      return;
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();
      setActivePhoneCountrySuggestionIndex((current) =>
        current > 0 ? current - 1 : phoneCountrySuggestions.length - 1,
      );
      return;
    }

    if (event.key === "Enter" && activePhoneCountrySuggestionIndex >= 0) {
      event.preventDefault();
      handlePhoneCountrySelect(
        phoneCountrySuggestions[activePhoneCountrySuggestionIndex],
      );
      return;
    }

    if (event.key === "Escape") {
      setPhoneCountrySuggestions([]);
      setActivePhoneCountrySuggestionIndex(-1);
    }
  };

  const handleLocationSuggestionSelect = (suggestion: NormalizedLocation) => {
    setLocationQuery(suggestion.displayName);
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
      handleLocationSuggestionSelect(locationSuggestions[activeSuggestionIndex]);
      return;
    }

    if (event.key === "Escape") {
      setLocationSuggestions([]);
      setActiveSuggestionIndex(-1);
    }
  };

  const handleRegistrationSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextErrors = {
      ...validateIdentityStep(),
      ...validateContactStep(),
      ...validateSecurityStep(),
    };

    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      if (nextErrors.username || nextErrors.fullName) {
        setCurrentStep("identity");
      } else if (nextErrors.email) {
        setCurrentStep("contact");
      } else {
        setCurrentStep("security");
      }
      return;
    }

    setIsSubmitting(true);

    const normalizedLocation =
      resolvedLocation ||
      (locationQuery.trim() ? await resolveLocation(locationQuery) : null);

    if (
      locationQuery.trim() &&
      (!hasHouseNumberInQuery(locationQuery) || !isPreciseLocation(normalizedLocation))
    ) {
      setIsSubmitting(false);
      setCurrentStep("contact");
      setFieldErrors((current) => ({
        ...current,
        location: t("auth.register.fields.location.requiredExact"),
      }));
      setLocationLookupMessage(t("auth.register.fields.location.requiredExact"));
      return;
    }

    if (normalizedLocation) {
      setResolvedLocation(normalizedLocation);
      setLocationQuery(normalizedLocation.displayName);
    }

    const response = await registerUser({
      username: values.username.trim(),
      fullName: values.fullName.trim(),
      phoneNumber: normalizePhoneNumber(selectedPhoneCountry, values.phoneNumber),
      email: values.email.trim(),
      userType: "individual",
      level: "normal",
      address: normalizedLocation?.address,
      postalCode: normalizedLocation?.postalCode,
      city: normalizedLocation?.city,
      country: normalizedLocation?.country,
      defaultLanguage: values.defaultLanguage,
      password: values.password,
      certificateFile,
    });

    setIsSubmitting(false);
    setStatusMessage({
      tone: response.ok ? "default" : "critical",
      text: response.message
        ? response.message
        : t(
            response.ok
              ? "auth.register.status.success"
              : "auth.register.status.error",
          ),
    });

    if (response.ok) {
      setCurrentStep("done");
    }
  };

  const headerTitle =
    currentStep === "identity"
      ? t("auth.register.steps.identity.title")
      : currentStep === "contact"
        ? t("auth.register.steps.contact.title")
        : currentStep === "security"
          ? t("auth.register.steps.security.title")
          : currentStep === "certificate"
            ? t("auth.register.steps.certificate.title")
            : t("auth.register.steps.done.title");

  const headerIntro =
    currentStep === "identity"
      ? t("auth.register.steps.identity.intro")
      : currentStep === "contact"
        ? t("auth.register.steps.contact.intro")
        : currentStep === "security"
          ? t("auth.register.steps.security.intro")
          : currentStep === "certificate"
            ? t("auth.register.steps.certificate.intro")
            : t("auth.register.steps.done.intro");

  useEffect(() => {
    if (previousStepRef.current === currentStep) {
      return;
    }

    previousStepRef.current = currentStep;
    stepHeadingRef.current?.focus();
  }, [currentStep]);

  return (
    <div className={styles.root} aria-labelledby="register-title">
      <p id={progressId} className={styles.progressText}>
        {t("auth.register.progress.current", {
          current: activeStepNumber,
          total: 4,
        })}
      </p>

      <AuthPageHeader
        action={<LanguageSwitcher />}
        id="register-title"
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
              styles.statusBase,
              statusMessage.tone === "critical"
                ? styles.statusCritical
                : styles.statusDefault,
            )}
            role={statusMessage.tone === "critical" ? "alert" : "status"}
            aria-live={statusMessage.tone === "critical" ? "assertive" : "polite"}
            aria-atomic="true"
          >
            {statusMessage.tone === "critical" ? (
              <TriangleAlert
                size={15}
                aria-hidden="true"
                className={styles.statusIcon}
              />
            ) : null}
            {statusMessage.text}
          </p>
        ) : null}
      </div>

      {currentStep === "identity" ? (
        <form className={styles.form} noValidate onSubmit={handleIdentitySubmit}>
          <div className={styles.sectionStack}>
            <FormField
              htmlFor="register-username"
              label={t("auth.register.fields.username.label")}
              hint={t("auth.register.fields.username.hint")}
              hintId={usernameHintId}
              error={fieldErrors.username}
              errorId={usernameErrorId}
              labelSuffix={<span className={styles.requiredMark}>*</span>}
            >
              <div className="relative">
                <input
                  ref={usernameInputRef}
                  id="register-username"
                  type="text"
                  name="username"
                  autoComplete="username"
                  value={values.username}
                  onChange={(event) => setFieldValue("username", event.target.value)}
                  className={getInputClassName(Boolean(fieldErrors.username), true)}
                  aria-describedby={getFieldDescribedBy(
                    usernameHintId,
                    fieldErrors.username,
                    usernameErrorId,
                  )}
                  aria-errormessage={fieldErrors.username ? usernameErrorId : undefined}
                  aria-invalid={fieldErrors.username ? "true" : "false"}
                  required
                />
                <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-muted">
                  <UserRound aria-hidden="true" size={16} />
                </span>
              </div>
            </FormField>

            <FormField
              htmlFor="register-full-name"
              label={t("auth.register.fields.fullName.label")}
              hint={t("auth.register.fields.fullName.hint")}
              hintId={fullNameHintId}
              error={fieldErrors.fullName}
              errorId={fullNameErrorId}
              labelSuffix={<span className={styles.requiredMark}>*</span>}
            >
              <input
                id="register-full-name"
                type="text"
                name="fullName"
                autoComplete="name"
                value={values.fullName}
                onChange={(event) => setFieldValue("fullName", event.target.value)}
                className={getInputClassName(Boolean(fieldErrors.fullName))}
                aria-describedby={getFieldDescribedBy(
                  fullNameHintId,
                  fieldErrors.fullName,
                  fullNameErrorId,
                )}
                aria-errormessage={fieldErrors.fullName ? fullNameErrorId : undefined}
                aria-invalid={fieldErrors.fullName ? "true" : "false"}
                required
              />
            </FormField>

            <FormField
              htmlFor="register-phone"
              label={t("auth.register.fields.phoneNumber.label")}
              hint={t("auth.register.fields.phoneNumber.hint")}
              hintId={phoneHintId}
            >
              <div className={styles.phoneRow}>
                <div className={styles.phonePrefix}>
                  <input
                    id="register-phone-country"
                    name="phoneCountry"
                    ref={phoneCountryInputRef}
                    type="text"
                    autoComplete="off"
                    spellCheck={false}
                    value={phoneCountryQuery}
                    onChange={(event) => setPhoneCountryQuery(event.target.value)}
                    onFocus={(event) => event.currentTarget.select()}
                    onClick={(event) => event.currentTarget.select()}
                    onKeyDown={handlePhoneCountryKeyDown}
                    className={getInputClassName(false)}
                    aria-label={t("auth.register.fields.phoneNumber.countryCodeLabel")}
                    aria-describedby={phoneCountryHintId}
                    aria-autocomplete="list"
                    aria-controls={
                      phoneCountrySuggestions.length > 0
                        ? phoneCountryListId
                        : undefined
                    }
                    aria-activedescendant={
                      activePhoneCountrySuggestionIndex >= 0
                        ? getPhoneCountrySuggestionId(
                            activePhoneCountrySuggestionIndex,
                          )
                        : undefined
                    }
                    aria-expanded={phoneCountrySuggestions.length > 0}
                    role="combobox"
                  />

                  {phoneCountrySuggestions.length > 0 ? (
                    <ul
                      id={phoneCountryListId}
                      role="listbox"
                      className={styles.phonePrefixSuggestions}
                    >
                      {phoneCountrySuggestions.map((option, index) => (
                        <li
                          id={getPhoneCountrySuggestionId(index)}
                          key={option.code}
                          role="option"
                          aria-selected={index === activePhoneCountrySuggestionIndex}
                        >
                          <button
                            type="button"
                            className={cx(
                              styles.phonePrefixSuggestion,
                              index === activePhoneCountrySuggestionIndex
                                ? styles.suggestionActive
                                : "",
                            )}
                            onClick={() => handlePhoneCountrySelect(option)}
                          >
                            {`${option.flag} ${option.dialCode} ${option.label}`}
                          </button>
                        </li>
                      ))}
                    </ul>
                  ) : null}
                </div>

                <input
                  id="register-phone"
                  type="tel"
                  name="phoneNumber"
                  autoComplete="tel-national"
                  inputMode="tel"
                  value={values.phoneNumber}
                  onChange={(event) => setFieldValue("phoneNumber", event.target.value)}
                  className={cx(
                    getInputClassName(false),
                    styles.phoneNumberInput,
                  )}
                  aria-describedby={`${phoneHintId} ${phoneCountryHintId}`}
                />
              </div>
            </FormField>
            <p id={phoneCountryHintId} className="sr-only">
              {t("auth.register.fields.phoneNumber.countryCodeHint", {
                dialCode: selectedPhoneCountry.dialCode,
              })}
            </p>
          </div>

          <div className={styles.actions}>
            <ActionButton type="submit">
              {t("auth.register.actions.next")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "contact" ? (
        <form className={styles.form} noValidate onSubmit={handleContactSubmit}>
          <div className={styles.sectionStack}>
            <FormField
              htmlFor="register-email"
              label={t("auth.register.fields.email.label")}
              hint={t("auth.register.fields.email.hint")}
              hintId={emailHintId}
              error={fieldErrors.email}
              errorId={emailErrorId}
              labelSuffix={<span className={styles.requiredMark}>*</span>}
            >
              <input
                ref={emailInputRef}
                id="register-email"
                type="email"
                name="email"
                autoComplete="email"
                inputMode="email"
                value={values.email}
                onChange={(event) => setFieldValue("email", event.target.value)}
                className={getInputClassName(Boolean(fieldErrors.email))}
                aria-describedby={getFieldDescribedBy(
                  emailHintId,
                  fieldErrors.email,
                  emailErrorId,
                )}
                aria-errormessage={fieldErrors.email ? emailErrorId : undefined}
                aria-invalid={fieldErrors.email ? "true" : "false"}
                required
              />
            </FormField>

            <FormField
              htmlFor="register-location"
              label={t("auth.register.fields.location.label")}
              hint={t("auth.register.fields.location.hint")}
              hintId={locationHintId}
              error={fieldErrors.location}
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
                      ? t("auth.register.fields.location.lookupLoading")
                      : locationLookupMessage ||
                        (resolvedLocation
                          ? t("auth.register.fields.location.lookupResolved", {
                              location: resolvedLocation.displayName,
                            })
                          : "")}
                  </p>

                  {locationSuggestions.length > 0 ? (
                    <ul
                      id={locationListId}
                      role="listbox"
                      className={styles.suggestions}
                    >
                      {locationSuggestions.map((suggestion, index) => (
                        <li
                          id={getLocationSuggestionId(index)}
                          key={`${suggestion.displayName}-${index}`}
                          role="option"
                          aria-selected={index === activeSuggestionIndex}
                        >
                          <button
                            type="button"
                            className={cx(
                              styles.suggestion,
                              index === activeSuggestionIndex
                                ? styles.suggestionActive
                                : "",
                            )}
                            onClick={() => handleLocationSuggestionSelect(suggestion)}
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
                        </li>
                      ))}
                    </ul>
                  ) : null}
                </div>
              }
            >
              <div className="relative">
                <input
                  id="register-location"
                  type="text"
                  name="location"
                  autoComplete="off"
                  enterKeyHint="next"
                  value={locationQuery}
                  onChange={(event) => handleLocationInputChange(event.target.value)}
                  onKeyDown={handleLocationKeyDown}
                  className={getInputClassName(false, true)}
                  aria-describedby={[
                    locationHintId,
                    locationStatusId,
                    fieldErrors.location ? locationErrorId : "",
                  ]
                    .filter(Boolean)
                    .join(" ")}
                  aria-errormessage={fieldErrors.location ? locationErrorId : undefined}
                  aria-invalid={fieldErrors.location ? "true" : "false"}
                  aria-autocomplete="list"
                  aria-controls={locationSuggestions.length > 0 ? locationListId : undefined}
                  aria-activedescendant={
                    activeSuggestionIndex >= 0
                      ? getLocationSuggestionId(activeSuggestionIndex)
                      : undefined
                  }
                  aria-expanded={locationSuggestions.length > 0}
                  role="combobox"
                />
                <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-muted">
                  <MapPin aria-hidden="true" size={16} />
                </span>
              </div>
            </FormField>

            <FormField
              htmlFor="register-default-language"
              label={t("auth.register.fields.defaultLanguage.label")}
              hint={t("auth.register.fields.defaultLanguage.hint")}
              hintId={defaultLanguageHintId}
            >
              <select
                id="register-default-language"
                name="defaultLanguage"
                value={values.defaultLanguage}
                onChange={(event) =>
                  setFieldValue("defaultLanguage", event.target.value as Locale)
                }
                className={getInputClassName(false)}
                aria-describedby={defaultLanguageHintId}
              >
                {languageOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </FormField>
          </div>

          <div className={styles.actions}>
            <ActionButton type="button" variant="secondary" onClick={() => goToStep("identity")}>
              {t("auth.register.actions.back")}
            </ActionButton>
            <ActionButton type="submit">
              {t("auth.register.actions.next")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "security" ? (
        <form className={styles.form} noValidate onSubmit={handleSecuritySubmit}>
          <FormField
            htmlFor="register-password"
            label={t("auth.register.fields.password.label")}
            hint={t("auth.register.fields.password.hint")}
            hintId={passwordHintId}
            error={fieldErrors.password}
            errorId={passwordErrorId}
            labelSuffix={<span className={styles.requiredMark}>*</span>}
          >
            <div className="relative">
              <input
                ref={passwordInputRef}
                id="register-password"
                type={showPassword ? "text" : "password"}
                name="password"
                autoComplete="new-password"
                value={values.password}
                onChange={(event) => setFieldValue("password", event.target.value)}
                className={getInputClassName(Boolean(fieldErrors.password), true)}
                aria-describedby={getFieldDescribedBy(
                  passwordHintId,
                  fieldErrors.password,
                  passwordErrorId,
                )}
                aria-errormessage={fieldErrors.password ? passwordErrorId : undefined}
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
                aria-controls="register-password"
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
            htmlFor="register-confirm-password"
            label={t("auth.register.fields.confirmPassword.label")}
            hint={t("auth.register.fields.confirmPassword.hint")}
            hintId={confirmPasswordHintId}
            error={fieldErrors.confirmPassword}
            errorId={confirmPasswordErrorId}
            labelSuffix={<span className={styles.requiredMark}>*</span>}
          >
            <div className="relative">
              <input
                id="register-confirm-password"
                type={showConfirmPassword ? "text" : "password"}
                name="confirmPassword"
                autoComplete="new-password"
                value={values.confirmPassword}
                onChange={(event) =>
                  setFieldValue("confirmPassword", event.target.value)
                }
                className={getInputClassName(
                  Boolean(fieldErrors.confirmPassword),
                  true,
                )}
                aria-describedby={getFieldDescribedBy(
                  confirmPasswordHintId,
                  fieldErrors.confirmPassword,
                  confirmPasswordErrorId,
                )}
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
                aria-controls="register-confirm-password"
              >
                {showConfirmPassword ? (
                  <EyeOff size={16} aria-hidden="true" />
                ) : (
                  <Eye size={16} aria-hidden="true" />
                )}
              </button>
            </div>
          </FormField>

          <p className={styles.helperText}>{t("auth.register.levelNotice")}</p>

          <div className={styles.actions}>
            <ActionButton type="button" variant="secondary" onClick={() => goToStep("contact")}>
              {t("auth.register.actions.back")}
            </ActionButton>
            <ActionButton type="submit">
              {t("auth.register.actions.next")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "certificate" ? (
        <form className={styles.form} noValidate onSubmit={handleRegistrationSubmit}>
          <FormField
            htmlFor="register-certificate"
            label={t("auth.register.fields.certificate.label")}
            hint={t("auth.register.fields.certificate.hint")}
            hintId={certificateHintId}
          >
            <div className={styles.fileCard}>
              <Upload size={16} aria-hidden="true" className="shrink-0 text-muted" />
              <div className={styles.fileMeta}>
                <p className={certificateFile ? styles.fileName : styles.fileEmpty}>
                  {certificateFile
                    ? certificateFile.name
                    : t("auth.register.fields.certificate.empty")}
                </p>
              </div>
              <label
                htmlFor="register-certificate"
                className="inline-flex min-h-11 items-center rounded-md px-2 text-sm font-medium text-brand-500 underline-offset-4 transition hover:text-brand-700 hover:underline"
              >
                {t("auth.register.fields.certificate.action")}
              </label>
              <input
                id="register-certificate"
                type="file"
                name="certificate"
                accept=".p12,.pfx,.pem,.crt,.cer"
                onChange={handleCertificateChange}
                className="sr-only"
                aria-describedby={certificateHintId}
              />
            </div>
          </FormField>

          <p className={styles.helperText}>{t("auth.register.footer")}</p>

          <div className={styles.actions}>
            <ActionButton type="button" variant="secondary" onClick={() => goToStep("security")}>
              {t("auth.register.actions.back")}
            </ActionButton>
            <ActionButton type="submit" aria-busy={isSubmitting} disabled={isSubmitting}>
              {isSubmitting
                ? t("auth.register.actions.submitting")
                : t("auth.register.actions.submit")}
            </ActionButton>
          </div>
        </form>
      ) : null}

      {currentStep === "done" ? (
        <>
          <p
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
            <span>{t("auth.register.status.success")}</span>
          </p>

          <div className={styles.actions}>
            <ActionLink to={appRoutes.login}>
              {t("auth.actions.backToSignIn")}
            </ActionLink>
          </div>
        </>
      ) : null}
    </div>
  );
}

export default RegisterForm;
