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

const styles = {
  root:
    "grid gap-y-1.5 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-start sm:gap-x-4",
  content: "min-w-0",
  actionWrap: "shrink-0 self-start sm:row-span-2",
  title: "text-[1.5rem] leading-tight font-semibold tracking-[-0.04em] text-ink text-balance sm:text-[1.75rem]",
  intro: "max-w-[34ch] text-[0.9375rem] leading-6 text-muted text-pretty",
} as const;

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
    <header className={styles.root}>
      <div className={styles.content}>
        <h1
          id={id}
          ref={titleRef}
          tabIndex={titleTabIndex}
          aria-describedby={titleDescribedBy}
          className={styles.title}
        >
          {title}
        </h1>
      </div>
      {action ? <div className={styles.actionWrap}>{action}</div> : null}
      <p id={introId} className={styles.intro}>
        {intro}
      </p>
    </header>
  );
}

export default AuthPageHeader;
