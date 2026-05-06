import type { ReactNode } from "react";
import FormStatus from "./FormStatus";

type StatusMessageProps = {
  children: ReactNode;
  tone?: "default" | "critical" | "warning";
};

function StatusMessage({ children, tone = "default" }: StatusMessageProps) {
  return <FormStatus tone={tone}>{children}</FormStatus>;
}

export type { StatusMessageProps };
export default StatusMessage;
