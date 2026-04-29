import React from "react";

interface PasswordFieldProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  show: boolean;
  setShow: (show: boolean) => void;
  error?: string;
  errorId?: string;
  hint?: string;
  hintId?: string;
  required?: boolean;
  className?: string;
  inputClassName?: string;
  buttonClassName?: string;
  ariaLabelShow?: string;
  ariaLabelHide?: string;
}

import { Eye, EyeOff } from "lucide-react";

const PasswordField: React.FC<PasswordFieldProps> = ({
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
  required = false,
  className = "",
  inputClassName = "",
  buttonClassName = "",
  ariaLabelShow = "Show password",
  ariaLabelHide = "Hide password",
}) => {
  return (
    <div className={className}>
      <label htmlFor={id} className="block text-sm font-medium mb-1">
        {label}
        {required && <span className="text-brand-600">*</span>}
      </label>
      {hint && (
        <p id={hintId} className="text-xs text-muted mb-2">
          {hint}
        </p>
      )}
      <div className="relative">
        <input
          id={id}
          type={show ? "text" : "password"}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={inputClassName}
          aria-describedby={hintId}
          aria-errormessage={error ? errorId : undefined}
          aria-invalid={!!error}
          required={required}
        />
        <button
          type="button"
          className={buttonClassName}
          onClick={() => setShow(!show)}
          aria-pressed={show}
          aria-label={show ? ariaLabelHide : ariaLabelShow}
          aria-controls={id}
        >
          {show ? (
            <EyeOff size={16} aria-hidden="true" />
          ) : (
            <Eye size={16} aria-hidden="true" />
          )}
        </button>
      </div>
      {error && (
        <p id={errorId} className="text-xs text-danger-700 mt-1">
          {error}
        </p>
      )}
    </div>
  );
};

export default PasswordField;
