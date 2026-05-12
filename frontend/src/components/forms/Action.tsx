import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router';
import { cx } from '../../lib/cx';

type ActionVariant = 'primary' | 'secondary' | 'text';

type CommonActionProps = {
  children: ReactNode;
  className?: string;
  variant?: ActionVariant;
};

function getActionClassName(variant: ActionVariant, className?: string) {
  const baseClassName =
    'inline-flex items-center justify-center text-sm font-semibold transition focus-visible:outline-none';

  if (variant === 'secondary') {
    return cx(
      baseClassName,
      'min-h-12 w-full rounded-xl border border-border-subtle bg-white px-4 py-3 text-ink hover:border-border-strong hover:bg-surface-subtle focus-visible:ring-4 focus-visible:ring-brand-500/15 disabled:cursor-wait disabled:opacity-70',
      className,
    );
  }

  if (variant === 'text') {
    return cx(
      baseClassName,
      'min-h-11 rounded-md px-2 font-medium text-brand-500 underline-offset-4 hover:text-brand-700 hover:underline focus-visible:ring-4 focus-visible:ring-brand-500/15',
      className,
    );
  }

  return cx(
    baseClassName,
    'min-h-12 w-full rounded-xl bg-brand-500 px-4 py-3 text-white hover:bg-brand-600 focus-visible:ring-4 focus-visible:ring-brand-500/20 disabled:cursor-wait disabled:opacity-70',
    className,
  );
}

type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & CommonActionProps;

function ActionButton({
  children,
  className,
  variant = 'primary',
  type = 'button',
  ...props
}: ActionButtonProps) {
  return (
    <button {...props} type={type} className={getActionClassName(variant, className)}>
      {children}
    </button>
  );
}

type ActionLinkProps = LinkProps & CommonActionProps;

function ActionLink({ children, className, variant = 'primary', ...props }: ActionLinkProps) {
  return (
    <Link {...props} className={getActionClassName(variant, className)}>
      {children}
    </Link>
  );
}

export { ActionButton, ActionLink };
