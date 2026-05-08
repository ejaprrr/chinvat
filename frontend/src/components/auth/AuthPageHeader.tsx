import type { ReactNode, Ref } from "react";

type AuthPageHeaderProps = {
  action?: ReactNode;
  id: string;
  intro: string;
  introId?: string;
  title: string;
  titleDescribedBy?: string;
  titleRef?: Ref<HTMLHeadingElement>;
  titleTabIndex?: number;
};

function AuthPageHeader({
  action,
  id,
  intro,
  introId,
  title,
  titleDescribedBy,
  titleRef,
  titleTabIndex,
}: AuthPageHeaderProps) {
  return (
    <header className="auth-page-header">
      <div className="auth-page-header__row">
        <div className="auth-page-header__content">
          <h1
            id={id}
            ref={titleRef}
            tabIndex={titleTabIndex}
            aria-describedby={titleDescribedBy}
            className="auth-page-header__title"
          >
            {title}
          </h1>
        </div>
        {action ? (
          <div className="auth-page-header__action">{action}</div>
        ) : null}
      </div>
      <p id={introId} className="auth-page-header__intro">
        {intro}
      </p>
    </header>
  );
}

export default AuthPageHeader;
