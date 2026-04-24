import type {
  AnchorHTMLAttributes,
  ButtonHTMLAttributes,
  ReactNode,
} from "react";
import { Link, type LinkProps } from "react-router";
import { cx } from "../../lib/cx";

const actionStyles = {
  primary:
    "inline-flex min-h-11 items-center justify-center rounded-lg bg-brand-500 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/20 disabled:cursor-wait disabled:opacity-70",
  secondary:
    "inline-flex min-h-11 items-center justify-center rounded-lg border border-border-subtle bg-white px-4 py-3 text-sm font-medium text-ink transition hover:border-border-strong hover:bg-surface-subtle focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15 disabled:cursor-wait disabled:opacity-70",
  text:
    "inline-flex min-h-11 items-center rounded-md px-2 text-sm font-medium text-brand-600 underline-offset-4 transition hover:text-brand-700 hover:underline focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-500/15",
} as const;

type ActionVariant = keyof typeof actionStyles;

type CommonActionProps = {
  children: ReactNode;
  className?: string;
  variant?: ActionVariant;
};

function getActionClassName(variant: ActionVariant, className?: string) {
  return cx(actionStyles[variant], className);
}

type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  CommonActionProps;

function ActionButton({
  children,
  className,
  variant = "primary",
  type = "button",
  ...props
}: ActionButtonProps) {
  return (
    <button
      {...props}
      type={type}
      className={getActionClassName(variant, className)}
    >
      {children}
    </button>
  );
}

type ActionLinkProps = LinkProps & CommonActionProps;

function ActionLink({
  children,
  className,
  variant = "primary",
  ...props
}: ActionLinkProps) {
  return (
    <Link
      {...props}
      className={getActionClassName(variant, className)}
    >
      {children}
    </Link>
  );
}

type ActionAnchorProps = AnchorHTMLAttributes<HTMLAnchorElement> &
  CommonActionProps;

function ActionAnchor({
  children,
  className,
  variant = "primary",
  ...props
}: ActionAnchorProps) {
  return (
    <a
      {...props}
      className={getActionClassName(variant, className)}
    >
      {children}
    </a>
  );
}

export { ActionAnchor, ActionButton, ActionLink };
