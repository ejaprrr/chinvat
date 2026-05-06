import type { ReactNode, Ref } from "react";
import AuthPageHeader from "./AuthPageHeader";
import StatusMessage from "../feedback/StatusMessage";

type AuthStatus = {
  content: ReactNode;
  tone?: "default" | "critical" | "warning";
} | null;

type AuthPageProps = {
  action?: ReactNode;
  "aria-labelledby": string;
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
  "aria-labelledby": ariaLabelledBy,
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

      <div
        className={status ? "block" : "hidden"}
        aria-hidden={status ? undefined : "true"}
      >
        {status ? (
          <StatusMessage tone={status.tone}>{status.content}</StatusMessage>
        ) : null}
      </div>

      {children}

      {footer}
    </div>
  );
}

export type { AuthStatus };
export default AuthPage;
