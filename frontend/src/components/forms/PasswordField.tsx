import {
  forwardRef,
  type Dispatch,
  type InputHTMLAttributes,
  type ReactNode,
  type SetStateAction,
} from 'react';
import { Eye, EyeOff } from 'lucide-react';
import FormField from './FormField';
import { cx } from '../../lib/cx';

type PasswordFieldProps = Omit<
  InputHTMLAttributes<HTMLInputElement>,
  'onChange' | 'type' | 'value'
> & {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  show: boolean;
  setShow: Dispatch<SetStateAction<boolean>>;
  error?: string;
  errorId?: string;
  hint?: string;
  hintId?: string;
  labelAction?: ReactNode;
  status?: ReactNode;
  ariaLabelShow?: string;
  ariaLabelHide?: string;
};

const PasswordField = forwardRef<HTMLInputElement, PasswordFieldProps>(
  (
    {
      id,
      label,
      value,
      onChange,
      show,
      setShow,
      error,
      errorId,
      hint,
      hintId,
      labelAction,
      required = false,
      status,
      className,
      ariaLabelShow = 'Show password',
      ariaLabelHide = 'Hide password',
      'aria-describedby': ariaDescribedBy,
      ...inputProps
    },
    ref,
  ) => {
    const describedBy = ariaDescribedBy || [hintId, error ? errorId : ''].filter(Boolean).join(' ');

    return (
      <FormField
        htmlFor={id}
        label={label}
        hint={hint}
        hintId={hintId}
        error={error}
        errorId={errorId}
        labelAction={labelAction}
        required={required}
        status={status}
      >
        <div className="relative">
          <input
            {...inputProps}
            ref={ref}
            id={id}
            type={show ? 'text' : 'password'}
            value={value}
            onChange={(event) => onChange(event.target.value)}
            className={cx(
              'field-control field-control--trailing',
              error && 'field-control--error',
              className,
            )}
            aria-describedby={describedBy || undefined}
            aria-errormessage={error ? errorId : undefined}
            aria-invalid={error ? 'true' : 'false'}
            required={required}
          />
          <button
            type="button"
            className="icon-control"
            onClick={() => setShow((current) => !current)}
            aria-pressed={show}
            aria-label={show ? ariaLabelHide : ariaLabelShow}
            aria-controls={id}
          >
            {show ? <EyeOff size={16} aria-hidden="true" /> : <Eye size={16} aria-hidden="true" />}
          </button>
        </div>
      </FormField>
    );
  },
);

PasswordField.displayName = 'PasswordField';

export default PasswordField;
