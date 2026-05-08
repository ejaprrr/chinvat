type NominatimAddress = {
  city?: string;
  county?: string;
  country?: string;
  country_code?: string;
  cycleway?: string;
  footway?: string;
  housenumber?: string;
  house_no?: string;
  house_number?: string;
  neighbourhood?: string;
  hamlet?: string;
  pedestrian?: string;
  postcode?: string;
  road?: string;
  suburb?: string;
  town?: string;
  village?: string;
};

type NominatimResult = {
  address?: NominatimAddress;
  display_name?: string;
  lat?: string;
  lon?: string;
};

export type LocationResult = {
  address?: string;
  city?: string;
  country?: string;
  countryCode?: string;
  displayName: string;
  isPrecise: boolean;
  lat?: string;
  lon?: string;
  postalCode?: string;
};

export class GeocodingError extends Error {
  code: "too_short" | "network_error" | "rate_limited" | "aborted";
  status?: number;

  constructor(
    message: string,
    code: "too_short" | "network_error" | "rate_limited" | "aborted",
    status?: number,
  ) {
    super(message);
    this.name = "GeocodingError";
    this.code = code;
    this.status = status;
  }
}

export type GeocodeOptions = {
  signal?: AbortSignal;
};

const NOMINATIM_SEARCH_URL =
  "https://nominatim.openstreetmap.org/search?format=jsonv2&addressdetails=1&limit=5";
const MIN_QUERY_LENGTH = 3;
const MIN_REQUEST_INTERVAL_MS = 1000;

const cache = new Map<string, LocationResult[]>();
const inFlight = new Map<string, Promise<LocationResult[]>>();

let requestQueue: Promise<void> = Promise.resolve();
let lastRequestStartedAt = 0;

function normalizeQuery(query: string) {
  return query.trim().replace(/\s+/g, " ");
}

function cacheKey(query: string) {
  return normalizeQuery(query).toLowerCase();
}

function wait(ms: number, signal?: AbortSignal) {
  if (ms <= 0) {
    return Promise.resolve();
  }

  return new Promise<void>((resolve, reject) => {
    const onAbort = () => {
      cleanup();
      reject(new GeocodingError("Geocoding request was aborted.", "aborted"));
    };

    const timeoutId = window.setTimeout(() => {
      cleanup();
      resolve();
    }, ms);

    const cleanup = () => {
      window.clearTimeout(timeoutId);
      signal?.removeEventListener("abort", onAbort);
    };

    if (signal) {
      if (signal.aborted) {
        cleanup();
        reject(new GeocodingError("Geocoding request was aborted.", "aborted"));
        return;
      }

      signal.addEventListener("abort", onAbort, { once: true });
    }
  });
}

function throwIfAborted(signal?: AbortSignal) {
  if (signal?.aborted) {
    throw new GeocodingError("Geocoding request was aborted.", "aborted");
  }
}

function queueTask<T>(task: () => Promise<T>) {
  const next = requestQueue.then(task, task);
  requestQueue = next.then(
    () => undefined,
    () => undefined,
  );
  return next;
}

function normalizePostcode(postcode?: string) {
  return postcode?.replace(/\s+/g, "").toUpperCase();
}

function buildDisplayName(
  address?: string,
  city?: string,
  country?: string,
) {
  return [address, city, country].filter(Boolean).join(", ");
}

function mapResult(item: NominatimResult): LocationResult {
  const address = item.address ?? {};
  const road =
    address.road ??
    address.pedestrian ??
    address.footway ??
    address.cycleway ??
    address.suburb ??
    address.neighbourhood ??
    address.village ??
    address.town;
  const house = address.house_number ?? address.house_no ?? address.housenumber;
  const addressLine = [road, house].filter(Boolean).join(" ") || item.display_name || "";
  const city = address.city ?? address.town ?? address.village ?? address.hamlet ?? address.county;
  const country = address.country;

  return {
    address: addressLine || undefined,
    city,
    country,
    countryCode: address.country_code?.toUpperCase(),
    displayName: buildDisplayName(addressLine, city, country) || item.display_name || "",
    isPrecise: Boolean(house || road),
    lat: item.lat,
    lon: item.lon,
    postalCode: normalizePostcode(address.postcode),
  };
}

async function executeGeocode(
  normalizedQuery: string,
  signal?: AbortSignal,
): Promise<LocationResult[]> {
  throwIfAborted(signal);

  const now = Date.now();
  const nextAllowedAt = lastRequestStartedAt + MIN_REQUEST_INTERVAL_MS;
  const delay = Math.max(0, nextAllowedAt - now);

  if (delay > 0) {
    await wait(delay, signal);
  }

  throwIfAborted(signal);
  lastRequestStartedAt = Date.now();

  const response = await fetch(
    `${NOMINATIM_SEARCH_URL}&q=${encodeURIComponent(normalizedQuery)}`,
    {
      signal,
      headers: {
        Accept: "application/json",
        "Accept-Language": navigator.language || "en",
      },
    },
  );

  if (response.status === 429) {
    throw new GeocodingError(
      "Location search was rate limited. Please wait a moment and try again.",
      "rate_limited",
      429,
    );
  }

  if (!response.ok) {
    throw new GeocodingError(
      `Location search failed with status ${response.status}.`,
      "network_error",
      response.status,
    );
  }

  const payload = (await response.json()) as NominatimResult[];
  const results = Array.isArray(payload) ? payload.map(mapResult).slice(0, 5) : [];

  cache.set(cacheKey(normalizedQuery), results);
  return results;
}

export async function geocode(
  query: string,
  options: GeocodeOptions = {},
): Promise<LocationResult[]> {
  const normalizedQuery = normalizeQuery(query);

  if (normalizedQuery.length < MIN_QUERY_LENGTH) {
    return [];
  }

  const key = cacheKey(normalizedQuery);
  const cached = cache.get(key);
  if (cached) {
    return cached;
  }

  const existing = inFlight.get(key);
  if (existing) {
    return existing;
  }

  const request = queueTask(() => executeGeocode(normalizedQuery, options.signal)).finally(
    () => {
      inFlight.delete(key);
    },
  );

  inFlight.set(key, request);
  return request;
}
