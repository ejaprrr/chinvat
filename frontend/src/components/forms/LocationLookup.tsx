import { cx } from '../../lib/cx';
import type { LocationResult } from '../../lib/geocoding';

type LocationSuggestion = LocationResult;

type LocationLookupProps = {
  activeSuggestionIndex: number;
  getSuggestionId: (index: number) => string;
  listId: string;
  loading?: boolean;
  loadingText: string;
  onSuggestionSelect: (suggestion: LocationSuggestion) => void;
  resolvedText?: string;
  statusId: string;
  statusMessage?: string | null;
  suggestions: LocationSuggestion[];
};

function LocationLookup({
  activeSuggestionIndex,
  getSuggestionId,
  listId,
  loading = false,
  loadingText,
  onSuggestionSelect,
  resolvedText,
  statusId,
  statusMessage,
  suggestions,
}: LocationLookupProps) {
  const visibleStatus = loading ? loadingText : statusMessage || resolvedText || '';

  return (
    <div className="space-y-2">
      <p
        id={statusId}
        className="text-[0.8125rem] leading-5 text-muted"
        role="status"
        aria-live="polite"
        aria-atomic="true"
      >
        {visibleStatus}
      </p>

      {suggestions.length > 0 ? (
        <div
          id={listId}
          role="listbox"
          className="overflow-hidden rounded-xl border border-border-subtle bg-white shadow-sm"
        >
          {suggestions.map((suggestion, index) => (
            <div
              id={getSuggestionId(index)}
              key={`${suggestion.displayName}-${index}`}
              role="option"
              aria-selected={index === activeSuggestionIndex}
            >
              <button
                type="button"
                className={cx(
                  'block w-full border-b border-border-subtle px-4 py-3 text-left text-sm text-ink transition last:border-b-0 hover:bg-surface-subtle focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15',
                  index === activeSuggestionIndex && 'bg-surface-subtle',
                )}
                onClick={() => onSuggestionSelect(suggestion)}
              >
                <span>{suggestion.displayName}</span>
                <span className="mt-0.5 block text-[0.8125rem] leading-5 text-muted">
                  {[suggestion.address, suggestion.postalCode, suggestion.city, suggestion.country]
                    .filter(Boolean)
                    .join(' · ')}
                </span>
              </button>
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}

export type { LocationSuggestion, LocationLookupProps };
export default LocationLookup;
