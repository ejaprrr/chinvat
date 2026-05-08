import type { HTMLAttributes } from "react";
import { cx } from "../../lib/cx";

type ActionGroupProps = HTMLAttributes<HTMLDivElement> & {
  align?: "start" | "end";
  direction?: "row" | "column";
};

function ActionGroup({
  align = "start",
  className,
  direction = "column",
  ...props
}: ActionGroupProps) {
  return (
    <div
      {...props}
      className={cx(
        "action-group",
        `action-group--${direction}`,
        align === "end" && "action-group--end",
        className,
      )}
    />
  );
}

export default ActionGroup;
