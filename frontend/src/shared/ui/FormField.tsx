import type { ReactNode } from 'react';
import { AlertCircle } from 'lucide-react';
import { cx } from '@/shared/lib/cx';
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
    <div className={cx('field', className)}>
      {' '}
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
        {labelAction ? <div className="field__label-action">{labelAction}</div> : null}{' '}
      </div>
      {children}
      {hint || error || action ? (
        <div className="field__supporting-row">
          <div className="field__supporting-copy">
            {hint ? (
              <p id={hintId} className={error ? 'sr-only' : 'field__hint'}>
                {' '}
                {hint}
              </p>
            ) : null}
            {error ? (
              <p id={errorId} className="field__error" role="alert" aria-atomic="true">
                {' '}
                <AlertCircle size={13} aria-hidden="true" />
                <span>{error}</span>
              </p>
            ) : null}
          </div>
          {action ? <div className="field__supporting-action">{action}</div> : null}{' '}
        </div>
      ) : null}
      {status}
    </div>
  );
}

export default FormField;
