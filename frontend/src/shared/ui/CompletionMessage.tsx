import type { ReactNode } from 'react';
import { ShieldCheck } from 'lucide-react';

type CompletionMessageProps = {
  children: ReactNode;
  id?: string;
};

function CompletionMessage({ children, id }: CompletionMessageProps) {
  return (
    <p id={id} className="completion-message" role="status" aria-live="polite" aria-atomic="true">
      <ShieldCheck aria-hidden="true" size={16} className="completion-message__icon" />{' '}
      <span>{children}</span>
    </p>
  );
}

export default CompletionMessage;
