import React from "react";

interface FieldProps {
  label: string;
  htmlFor: string;
  children: React.ReactNode;
  error?: string;
  hint?: string;
  required?: boolean;
  className?: string;
}

const Field: React.FC<FieldProps> = ({
  label,
  htmlFor,
  children,
  error,
  hint,
  required,
  className = "",
}) => (
  <div className={`mb-4 ${className}`}>
    <label htmlFor={htmlFor} className="block text-sm font-medium mb-1">
      {label}
      {required && <span className="text-brand-600 ml-1">*</span>}
    </label>
    {children}
    {hint && !error && <div className="text-xs text-muted mt-1">{hint}</div>}
    {error && <div className="text-xs text-danger-700 mt-1">{error}</div>}
  </div>
);

export default Field;
