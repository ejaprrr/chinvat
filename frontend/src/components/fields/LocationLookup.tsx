import { cx } from "../../lib/cx";

type LocationSuggestion = {
  address?: string;
  city?: string;
  country?: string;
  displayName: string;
  postalCode?: string;
  isPrecise?: boolean;
};

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
  const visibleStatus = loading
    ? loadingText
    : statusMessage || resolvedText || "";

  return (
    <div className="location-lookup">
      <p
        id={statusId}
        className="location-lookup__status"
        role="status"
        aria-live="polite"
        aria-atomic="true"
      >
        {visibleStatus}
      </p>

      {suggestions.length > 0 ? (
        <div id={listId} role="listbox" className="location-lookup__list">
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
                  "location-lookup__item",
                  index === activeSuggestionIndex &&
                    "location-lookup__item--active",
                )}
                onClick={() => onSuggestionSelect(suggestion)}
              >
                <span>{suggestion.displayName}</span>
                <span className="location-lookup__meta">
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
  );
}

export type { LocationSuggestion, LocationLookupProps };
export default LocationLookup;
