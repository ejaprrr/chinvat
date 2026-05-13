<<<<<<< HEAD
import type { ReactNode } from 'react';
import { TriangleAlert } from 'lucide-react';
import { cx } from '../../lib/cx';

type FormStatusTone = 'default' | 'critical' | 'warning';
=======
import type { ReactNode } from "react";
import { TriangleAlert } from "lucide-react";
import { cx } from "../../lib/cx";

type FormStatusTone = "default" | "critical" | "warning";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type FormStatusProps = {
  children: ReactNode;
  className?: string;
  tone?: FormStatusTone;
};

<<<<<<< HEAD
function FormStatus({ children, className, tone = 'default' }: FormStatusProps) {
  const isAssertive = tone === 'critical' || tone === 'warning';

  return (
    <div
      className={cx('status-message', `status-message--${tone}`, className)}
      role={isAssertive ? 'alert' : 'status'}
      aria-live={isAssertive ? 'assertive' : 'polite'}
      aria-atomic="true"
    >
      {tone === 'critical' ? (
        <TriangleAlert size={15} aria-hidden="true" className="status-message__icon" />
=======
function FormStatus({
  children,
  className,
  tone = "default",
}: FormStatusProps) {
  const isAssertive = tone === "critical" || tone === "warning";

  return (
    <div
      className={cx("status-message", `status-message--${tone}`, className)}
      role={isAssertive ? "alert" : "status"}
      aria-live={isAssertive ? "assertive" : "polite"}
      aria-atomic="true"
    >
      {tone === "critical" ? (
        <TriangleAlert
          size={15}
          aria-hidden="true"
          className="status-message__icon"
        />
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      ) : null}
      {children}
    </div>
  );
}

export default FormStatus;
