import { forwardRef, type InputHTMLAttributes, type ReactNode } from 'react';
import { cx } from '../../lib/cx';
import FormField from './FormField';

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
      <div className={hasTrailingIcon ? 'relative' : undefined}>
        <input
          {...props}
          ref={ref}
          required={required}
          className={cx(
            'field-control',
            error && 'field-control--error',
            hasTrailingIcon && 'field-control--trailing',
            className,
          )}
        />
        {hasTrailingIcon ? <span className="inline-trailing-icon">{trailingIcon}</span> : null}
      </div>
    );

    if (!label) {
      return control;
    }

    return (
      <FormField
        htmlFor={htmlFor || props.id || ''}
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

TextInput.displayName = 'TextInput';

export default TextInput;
