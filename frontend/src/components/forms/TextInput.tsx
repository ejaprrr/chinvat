<<<<<<< HEAD
import { forwardRef, type InputHTMLAttributes, type ReactNode } from 'react';
import { cx } from '../../lib/cx';
import FormField from './FormField';
=======
import { forwardRef, type InputHTMLAttributes, type ReactNode } from "react";
import { cx } from "../../lib/cx";
import FormField from "./FormField";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type TextInputProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: boolean;
  errorId?: string;
  fieldError?: string;
  hint?: string;
  hintId?: string;
  htmlFor?: string;
  label?: string;
  labelAction?: ReactNode;
  required?: boolean;
  status?: ReactNode;
  trailingIcon?: ReactNode;
};

const TextInput = forwardRef<HTMLInputElement, TextInputProps>(
  (
    {
      className,
      error = false,
      errorId,
      fieldError,
      hint,
      hintId,
      htmlFor,
      label,
      labelAction,
      required = false,
      status,
      trailingIcon,
      ...props
    },
    ref,
  ) => {
    const hasTrailingIcon = Boolean(trailingIcon);
    const control = (
<<<<<<< HEAD
      <div className={hasTrailingIcon ? 'relative' : undefined}>
=======
      <div className={hasTrailingIcon ? "relative" : undefined}>
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        <input
          {...props}
          ref={ref}
          required={required}
          className={cx(
<<<<<<< HEAD
            'field-control',
            error && 'field-control--error',
            hasTrailingIcon && 'field-control--trailing',
            className,
          )}
        />
        {hasTrailingIcon ? <span className="inline-trailing-icon">{trailingIcon}</span> : null}
=======
            "field-control",
            error && "field-control--error",
            hasTrailingIcon && "field-control--trailing",
            className,
          )}
        />
        {hasTrailingIcon ? (
          <span className="inline-trailing-icon">{trailingIcon}</span>
        ) : null}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      </div>
    );

    if (!label) {
      return control;
    }

    return (
      <FormField
<<<<<<< HEAD
        htmlFor={htmlFor || props.id || ''}
=======
        htmlFor={htmlFor || props.id || ""}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        label={label}
        labelAction={labelAction}
        hint={hint}
        hintId={hintId}
        error={fieldError}
        errorId={errorId}
        required={required}
        status={status}
      >
        {control}
      </FormField>
    );
  },
);

<<<<<<< HEAD
TextInput.displayName = 'TextInput';
=======
TextInput.displayName = "TextInput";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

export default TextInput;
