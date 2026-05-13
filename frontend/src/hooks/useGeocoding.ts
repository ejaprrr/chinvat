<<<<<<< HEAD
import { useEffect, useState } from 'react';
import { geocode, GeocodingError, type LocationResult } from '../lib/geocoding';
=======
import { useEffect, useState } from "react";
import { geocode, GeocodingError, type LocationResult } from "../lib/geocoding";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type UseGeocodingState = {
  data: LocationResult[];
  error: GeocodingError | null;
  loading: boolean;
};

const DEFAULT_DEBOUNCE_MS = 750;

function isAbortError(error: unknown) {
  return (
<<<<<<< HEAD
    (error instanceof GeocodingError && error.code === 'aborted') ||
    (error instanceof DOMException && error.name === 'AbortError') ||
    (error instanceof Error && error.name === 'AbortError')
  );
}

export function useGeocoding(query: string, debounceMs = DEFAULT_DEBOUNCE_MS): UseGeocodingState {
=======
    (error instanceof GeocodingError && error.code === "aborted") ||
    (error instanceof DOMException && error.name === "AbortError") ||
    (error instanceof Error && error.name === "AbortError")
  );
}

export function useGeocoding(
  query: string,
  debounceMs = DEFAULT_DEBOUNCE_MS,
): UseGeocodingState {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const [data, setData] = useState<LocationResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<GeocodingError | null>(null);

  useEffect(() => {
<<<<<<< HEAD
    const normalizedQuery = query.trim().replace(/\s+/g, ' ');
=======
    const normalizedQuery = query.trim().replace(/\s+/g, " ");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

    if (normalizedQuery.length < 3) {
      Promise.resolve().then(() => {
        setData([]);
        setLoading(false);
        setError(null);
      });
      return;
    }

    const controller = new AbortController();
    const timerId = window.setTimeout(() => {
      setLoading(true);
      setError(null);

      void geocode(normalizedQuery, { signal: controller.signal })
        .then((results) => {
          setData(results);
        })
        .catch((nextError) => {
          if (isAbortError(nextError)) {
            return;
          }

          setData([]);
          setError(
            nextError instanceof GeocodingError
              ? nextError
<<<<<<< HEAD
              : new GeocodingError('Unable to search locations right now.', 'network_error'),
=======
              : new GeocodingError(
                  "Unable to search locations right now.",
                  "network_error",
                ),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          );
        })
        .finally(() => {
          setLoading(false);
        });
    }, debounceMs);

    return () => {
      window.clearTimeout(timerId);
      controller.abort();
      setLoading(false);
    };
  }, [debounceMs, query]);

  return { data, error, loading };
}
