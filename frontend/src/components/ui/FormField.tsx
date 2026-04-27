import type { ReactNode } from "react";
import { AlertCircle } from "lucide-react";

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
  action?: ReactNode;
  status?: ReactNode;
};

const styles = {
  root: "space-y-2.5",
  labelRow: "flex flex-col gap-1.5 min-[480px]:flex-row min-[480px]:items-center min-[480px]:justify-between min-[480px]:gap-4",
  label: "flex items-center gap-1 text-sm font-medium text-ink",
  labelActionRow: "shrink-0 self-start min-[480px]:self-auto",
  supportingRow:
    "flex flex-col gap-1.5 min-[480px]:flex-row min-[480px]:items-start min-[480px]:justify-between min-[480px]:gap-4",
  supportingCopy: "min-w-0 flex-1",
  hint: "text-[0.8125rem] leading-5 text-muted",
  supportingActionRow: "flex shrink-0 min-[480px]:justify-end",
  error: "flex items-center gap-2 text-[0.8125rem] leading-5 text-danger-700",
} as const;

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
  status,
}: FormFieldProps) {
  return (
    <div className={styles.root}>
      <div className={styles.labelRow}>
        <label className={styles.label} htmlFor={htmlFor}>
          {label}
          {labelSuffix}
        </label>
        {labelAction ? (
          <div className={styles.labelActionRow}>{labelAction}</div>
        ) : null}
      </div>

      {children}

      {hint || error || action ? (
        <div className={styles.supportingRow}>
          <div className={styles.supportingCopy}>
            {hint ? (
              <p
                id={hintId}
                className={error ? "sr-only" : styles.hint}
              >
                {hint}
              </p>
            ) : null}
            {error ? (
              <p
                id={errorId}
                className={styles.error}
                role="alert"
                aria-atomic="true"
              >
                <AlertCircle size={13} aria-hidden="true" />
                <span>{error}</span>
              </p>
            ) : null}
          </div>
          {action ? (
            <div className={styles.supportingActionRow}>{action}</div>
          ) : null}
        </div>
      ) : null}

      {status}
    </div>
  );
}

export default FormField;
