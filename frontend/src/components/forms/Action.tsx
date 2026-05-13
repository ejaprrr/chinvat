<<<<<<< HEAD
import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router';
import { cx } from '../../lib/cx';

type ActionVariant = 'primary' | 'secondary' | 'text';
=======
import type {
  ButtonHTMLAttributes,
  ReactNode,
} from "react";
import { Link, type LinkProps } from "react-router";
import { cx } from "../../lib/cx";

type ActionVariant = "primary" | "secondary" | "text";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type CommonActionProps = {
  children: ReactNode;
  className?: string;
  variant?: ActionVariant;
};

function getActionClassName(variant: ActionVariant, className?: string) {
  const baseClassName =
<<<<<<< HEAD
    'inline-flex items-center justify-center text-sm font-semibold transition focus-visible:outline-none';

  if (variant === 'secondary') {
    return cx(
      baseClassName,
      'min-h-12 w-full rounded-xl border border-border-subtle bg-white px-4 py-3 text-ink hover:border-border-strong hover:bg-surface-subtle focus-visible:ring-4 focus-visible:ring-brand-500/15 disabled:cursor-wait disabled:opacity-70',
=======
    "inline-flex items-center justify-center text-sm font-semibold transition focus-visible:outline-none";

  if (variant === "secondary") {
    return cx(
      baseClassName,
      "min-h-12 w-full rounded-xl border border-border-subtle bg-white px-4 py-3 text-ink hover:border-border-strong hover:bg-surface-subtle focus-visible:ring-4 focus-visible:ring-brand-500/15 disabled:cursor-wait disabled:opacity-70",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      className,
    );
  }

<<<<<<< HEAD
  if (variant === 'text') {
    return cx(
      baseClassName,
      'min-h-11 rounded-md px-2 font-medium text-brand-500 underline-offset-4 hover:text-brand-700 hover:underline focus-visible:ring-4 focus-visible:ring-brand-500/15',
=======
  if (variant === "text") {
    return cx(
      baseClassName,
      "min-h-11 rounded-md px-2 font-medium text-brand-500 underline-offset-4 hover:text-brand-700 hover:underline focus-visible:ring-4 focus-visible:ring-brand-500/15",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      className,
    );
  }

  return cx(
    baseClassName,
<<<<<<< HEAD
    'min-h-12 w-full rounded-xl bg-brand-500 px-4 py-3 text-white hover:bg-brand-600 focus-visible:ring-4 focus-visible:ring-brand-500/20 disabled:cursor-wait disabled:opacity-70',
=======
    "min-h-12 w-full rounded-xl bg-brand-500 px-4 py-3 text-white hover:bg-brand-600 focus-visible:ring-4 focus-visible:ring-brand-500/20 disabled:cursor-wait disabled:opacity-70",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    className,
  );
}

<<<<<<< HEAD
type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & CommonActionProps;
=======
type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  CommonActionProps;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

function ActionButton({
  children,
  className,
<<<<<<< HEAD
  variant = 'primary',
  type = 'button',
  ...props
}: ActionButtonProps) {
  return (
    <button {...props} type={type} className={getActionClassName(variant, className)}>
=======
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
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      {children}
    </button>
  );
}

type ActionLinkProps = LinkProps & CommonActionProps;

<<<<<<< HEAD
function ActionLink({ children, className, variant = 'primary', ...props }: ActionLinkProps) {
  return (
    <Link {...props} className={getActionClassName(variant, className)}>
=======
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
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      {children}
    </Link>
  );
}

export { ActionButton, ActionLink };
