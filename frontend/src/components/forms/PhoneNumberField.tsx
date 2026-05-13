<<<<<<< HEAD
import type { ChangeEventHandler, Ref } from 'react';
import { cx } from '../../lib/cx';
import FormField from './FormField';
import PhoneCountrySelect, { type PhoneCountryOption } from './PhoneCountrySelect';
=======
import type { ChangeEventHandler, Ref } from "react";
import { cx } from "../../lib/cx";
import FormField from "./FormField";
import PhoneCountrySelect, {
  type PhoneCountryOption,
} from "./PhoneCountrySelect";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type PhoneNumberFieldProps = {
  countryControlId: string;
  countryHint: string;
  countryHintId: string;
  hint: string;
  hintId: string;
  id: string;
  inputRef?: Ref<HTMLInputElement>;
  label: string;
  name: string;
  onCountrySelect: (option: PhoneCountryOption) => void;
  onNumberChange: ChangeEventHandler<HTMLInputElement>;
  options: PhoneCountryOption[];
  selectedCountry: PhoneCountryOption;
  value: string;
};

function PhoneNumberField({
  countryControlId,
  countryHint,
  countryHintId,
  hint,
  hintId,
  id,
  inputRef,
  label,
  name,
  onCountrySelect,
  onNumberChange,
  options,
  selectedCountry,
  value,
}: PhoneNumberFieldProps) {
  return (
    <FormField htmlFor={id} label={label} hint={hint} hintId={hintId}>
      <div className="phone-number-field">
        <PhoneCountrySelect
          id={countryControlId}
          labelledBy={countryHintId}
          options={options}
          selectedOption={selectedCountry}
          onSelect={onCountrySelect}
        />
        <input
          ref={inputRef}
          id={id}
          type="tel"
          name={name}
          autoComplete="tel-national"
          inputMode="tel"
          value={value}
          onChange={onNumberChange}
<<<<<<< HEAD
          className={cx('field-control', 'phone-number-field__input')}
=======
          className={cx("field-control", "phone-number-field__input")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          aria-describedby={`${hintId} ${countryHintId}`}
        />
      </div>
      <p id={countryHintId} className="sr-only">
        {countryHint}
      </p>
    </FormField>
  );
}

export default PhoneNumberField;
