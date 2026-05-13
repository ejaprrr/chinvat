<<<<<<< HEAD
import type { HTMLAttributes } from 'react';
import { cx } from '../../lib/cx';

type ActionGroupProps = HTMLAttributes<HTMLDivElement> & {
  align?: 'start' | 'end';
  direction?: 'row' | 'column';
};

function ActionGroup({
  align = 'start',
  className,
  direction = 'column',
=======
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
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  ...props
}: ActionGroupProps) {
  return (
    <div
      {...props}
      className={cx(
<<<<<<< HEAD
        'action-group',
        `action-group--${direction}`,
        align === 'end' && 'action-group--end',
=======
        "action-group",
        `action-group--${direction}`,
        align === "end" && "action-group--end",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        className,
      )}
    />
  );
}

export default ActionGroup;
