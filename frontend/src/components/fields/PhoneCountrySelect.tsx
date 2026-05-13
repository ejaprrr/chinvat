import type { ChangeEvent } from 'react';

type PhoneCountryOption = {
  code: string;
  dialCode: string;
  flag: string;
  label: string;
};

type PhoneCountrySelectProps = {
  id?: string;
  labelledBy?: string;
  onSelect: (option: PhoneCountryOption) => void;
  options: PhoneCountryOption[];
  selectedOption: PhoneCountryOption;
};

function PhoneCountrySelect({
  id,
  labelledBy,
  onSelect,
  options,
  selectedOption,
}: PhoneCountrySelectProps) {
  const handleChange = (event: ChangeEvent<HTMLSelectElement>) => {
    const nextOption = options.find((option) => option.code === event.target.value);

    if (nextOption) {
      onSelect(nextOption);
    }
  };

  return (
    <select
      id={id}
      className="phone-country"
      value={selectedOption.code}
      onChange={handleChange}
      aria-labelledby={labelledBy}
    >
      {options.map((option) => (
        <option key={option.code} value={option.code} title={option.label}>
          {option.flag} {option.dialCode}
        </option>
      ))}
    </select>
  );
}

export type { PhoneCountryOption };
export default PhoneCountrySelect;
