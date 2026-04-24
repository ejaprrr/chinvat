import type { HTMLAttributes } from "react";
import { cx } from "../../lib/cx";

type InfoPanelProps = HTMLAttributes<HTMLParagraphElement> & {
  align?: "left" | "center";
};

const styles = {
  base:
    "rounded-lg border border-border-subtle bg-surface-subtle px-4 py-3 text-[0.8125rem] leading-5 text-muted",
  center: "text-center",
} as const;

function InfoPanel({
  align = "left",
  className,
  ...props
}: InfoPanelProps) {
  return (
    <p
      {...props}
      className={cx(
        styles.base,
        align === "center" && styles.center,
        className,
      )}
    />
  );
}

export default InfoPanel;
