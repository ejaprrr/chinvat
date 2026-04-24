import type { HTMLAttributes } from "react";
import { cx } from "../../lib/cx";

type InfoPanelProps = HTMLAttributes<HTMLParagraphElement> & {
  align?: "left" | "center";
  tone?: "default" | "warning";
};

const styles = {
  base: "rounded-lg border px-4 py-3 text-[0.8125rem] leading-5",
  default: "border-border-subtle bg-surface-subtle text-muted",
  warning: "border-warning-border bg-warning-surface text-warning-ink",
  center: "text-center",
} as const;

function InfoPanel({
  align = "left",
  className,
  tone = "default",
  ...props
}: InfoPanelProps) {
  return (
    <p
      {...props}
      className={cx(
        styles.base,
        styles[tone],
        align === "center" && styles.center,
        className,
      )}
    />
  );
}

export default InfoPanel;
