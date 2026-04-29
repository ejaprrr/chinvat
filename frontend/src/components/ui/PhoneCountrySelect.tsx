import React from "react";

interface PhoneCountrySelectProps {
  value: string;
  onChange: (value: string) => void;
  options: Array<{
    code: string;
    dialCode: string;
    flag: string;
    label: string;
  }>;
  suggestions: Array<{
    code: string;
    dialCode: string;
    flag: string;
    label: string;
  }>;
  onSelect: (option: any) => void;
  onKeyDown: (event: React.KeyboardEvent<HTMLInputElement>) => void;
  inputRef?: React.RefObject<HTMLInputElement>;
  hintId?: string;
  listId?: string;
  activeSuggestionIndex?: number;
  getSuggestionId?: (index: number) => string;
  className?: string;
  inputClassName?: string;
  suggestionClassName?: string;
  suggestionActiveClassName?: string;
}

const PhoneCountrySelect: React.FC<PhoneCountrySelectProps> = ({
  value,
  onChange,
  options,
  suggestions,
  onSelect,
  onKeyDown,
  inputRef,
  hintId,
  listId,
  activeSuggestionIndex = -1,
  getSuggestionId = (i) => `${listId}-option-${i}`,
  className = "",
  inputClassName = "",
  suggestionClassName = "",
  suggestionActiveClassName = "",
}) => {
  return (
    <div className={className}>
      <input
        ref={inputRef}
        type="text"
        autoComplete="off"
        spellCheck={false}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onFocus={(e) => e.currentTarget.select()}
        onClick={(e) => e.currentTarget.select()}
        onKeyDown={onKeyDown}
        className={inputClassName}
        aria-describedby={hintId}
        aria-autocomplete="list"
        aria-controls={suggestions.length > 0 ? listId : undefined}
        aria-activedescendant={
          activeSuggestionIndex >= 0
            ? getSuggestionId(activeSuggestionIndex)
            : undefined
        }
        aria-expanded={suggestions.length > 0}
        role="combobox"
      />
      {suggestions.length > 0 && (
        <ul
          id={listId}
          role="listbox"
          className="absolute left-0 right-0 top-[calc(100%+0.5rem)] z-20 overflow-hidden rounded-xl border border-border-subtle bg-white shadow-sm"
        >
          {suggestions.map((option, index) => (
            <li
              id={getSuggestionId(index)}
              key={option.code}
              role="option"
              aria-selected={index === activeSuggestionIndex}
            >
              <button
                type="button"
                className={
                  index === activeSuggestionIndex
                    ? suggestionActiveClassName
                    : suggestionClassName
                }
                onClick={() => onSelect(option)}
              >
                {`${option.flag} ${option.dialCode} ${option.label}`}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default PhoneCountrySelect;
