import type { ReactNode } from 'react';
import { ShieldCheck } from 'lucide-react';

type AuthCompletionProps = {
  children: ReactNode;
  id?: string;
};

function AuthCompletion({ children, id }: AuthCompletionProps) {
  return (
    <p id={id} className="completion-message" role="status" aria-live="polite" aria-atomic="true">
      <ShieldCheck aria-hidden="true" size={16} className="completion-message__icon" />
      <span>{children}</span>
    </p>
  );
}

type AuthDividerProps = {
  children: ReactNode;
};

function AuthDivider({ children }: AuthDividerProps) {
  return (
    <p className="auth-divider" aria-hidden="true">
      <span className="auth-divider__line" />
      <span>{children}</span>
      <span className="auth-divider__line" />
    </p>
  );
}

export { AuthCompletion, AuthDivider };
