import type { ReactNode } from "react";
import { TriangleAlert } from "lucide-react";
import { cx } from "../../lib/cx";

type FormStatusTone = "default" | "critical" | "warning";

type FormStatusProps = {
  children: ReactNode;
  className?: string;
  tone?: FormStatusTone;
};

function FormStatus({
  children,
  className,
  tone = "default",
}: FormStatusProps) {
  const isAssertive = tone === "critical" || tone === "warning";

  return (
    <p
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
      ) : null}
      {children}
    </p>
  );
}

export default FormStatus;
