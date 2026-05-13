<<<<<<< HEAD
import type { ReactNode, Ref } from 'react';
import AuthPageHeader from './AuthPageHeader';
import FormStatus from '../forms/FormStatus';

type AuthStatus = {
  content: ReactNode;
  tone?: 'default' | 'critical' | 'warning';
=======
import type { ReactNode, Ref } from "react";
import AuthPageHeader from "./AuthPageHeader";
import FormStatus from "../forms/FormStatus";

type AuthStatus = {
  content: ReactNode;
  tone?: "default" | "critical" | "warning";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
} | null;

type AuthPageProps = {
  action?: ReactNode;
<<<<<<< HEAD
  'aria-labelledby': string;
=======
  "aria-labelledby": string;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  children: ReactNode;
  footer?: ReactNode;
  intro: string;
  introId?: string;
  progress?: ReactNode;
  status?: AuthStatus;
  title: string;
  titleDescribedBy?: string;
  titleId: string;
  titleRef?: Ref<HTMLHeadingElement>;
  titleTabIndex?: number;
};

function AuthPage({
  action,
<<<<<<< HEAD
  'aria-labelledby': ariaLabelledBy,
=======
  "aria-labelledby": ariaLabelledBy,
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  children,
  footer,
  intro,
  introId,
  progress,
  status,
  title,
  titleDescribedBy,
  titleId,
  titleRef,
  titleTabIndex,
}: AuthPageProps) {
  return (
    <div className="auth-flow" aria-labelledby={ariaLabelledBy}>
      {progress}

      <AuthPageHeader
        action={action}
        id={titleId}
        intro={intro}
        introId={introId}
        title={title}
        titleDescribedBy={titleDescribedBy}
        titleRef={titleRef}
        titleTabIndex={titleTabIndex}
      />

<<<<<<< HEAD
      <div className={status ? 'block' : 'hidden'} aria-hidden={status ? undefined : 'true'}>
        {status ? <FormStatus tone={status.tone}>{status.content}</FormStatus> : null}
=======
      <div
        className={status ? "block" : "hidden"}
        aria-hidden={status ? undefined : "true"}
      >
        {status ? (
          <FormStatus tone={status.tone}>{status.content}</FormStatus>
        ) : null}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      </div>

      {children}

      {footer}
    </div>
  );
}

export type { AuthStatus };
export default AuthPage;
