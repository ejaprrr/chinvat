<<<<<<< HEAD
import type { ReactNode } from 'react';
import { AlertCircle } from 'lucide-react';
import { cx } from '../../lib/cx';
=======
import type { ReactNode } from "react";
import { AlertCircle } from "lucide-react";
import { cx } from "../../lib/cx";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type FormFieldProps = {
  children: ReactNode;
  error?: string;
  errorId?: string;
  hint?: string;
  hintId?: string;
  htmlFor: string;
  label: string;
  labelAction?: ReactNode;
  labelSuffix?: ReactNode;
  required?: boolean;
  action?: ReactNode;
  status?: ReactNode;
  className?: string;
};

function FormField({
  action,
  children,
  error,
  errorId,
  hint,
  hintId,
  htmlFor,
  label,
  labelAction,
  labelSuffix,
  required = false,
  status,
  className,
}: FormFieldProps) {
  return (
<<<<<<< HEAD
    <div className={cx('field', className)}>
=======
    <div className={cx("field", className)}>
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      <div className="field__label-row">
        <label className="field__label" htmlFor={htmlFor}>
          {label}
          {required ? (
            <span className="required-mark" aria-hidden="true">
              *
            </span>
          ) : null}
          {labelSuffix}
        </label>
<<<<<<< HEAD
        {labelAction ? <div className="field__label-action">{labelAction}</div> : null}
=======
        {labelAction ? (
          <div className="field__label-action">{labelAction}</div>
        ) : null}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      </div>

      {children}

      {hint || error || action ? (
        <div className="field__supporting-row">
          <div className="field__supporting-copy">
            {hint ? (
<<<<<<< HEAD
              <p id={hintId} className={error ? 'sr-only' : 'field__hint'}>
=======
              <p id={hintId} className={error ? "sr-only" : "field__hint"}>
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                {hint}
              </p>
            ) : null}
            {error ? (
<<<<<<< HEAD
              <p id={errorId} className="field__error" role="alert" aria-atomic="true">
=======
              <p
                id={errorId}
                className="field__error"
                role="alert"
                aria-atomic="true"
              >
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
                <AlertCircle size={13} aria-hidden="true" />
                <span>{error}</span>
              </p>
            ) : null}
          </div>
<<<<<<< HEAD
          {action ? <div className="field__supporting-action">{action}</div> : null}
=======
          {action ? (
            <div className="field__supporting-action">{action}</div>
          ) : null}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        </div>
      ) : null}

      {status}
    </div>
  );
}

export default FormField;
