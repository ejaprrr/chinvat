import type { Ref } from "react";

type AuthPageHeaderProps = {
  id: string;
  intro: string;
  introId?: string;
  title: string;
  titleDescribedBy?: string;
  titleRef?: Ref<HTMLHeadingElement>;
  titleTabIndex?: number;
};

const styles = {
  root: "flex flex-col gap-1.5",
  title: "text-[1.375rem] leading-tight font-semibold tracking-[-0.03em] text-ink text-balance",
  intro: "max-w-[42ch] text-[0.9375rem] leading-6 text-muted text-pretty",
} as const;

function AuthPageHeader({
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
      <h1
        id={id}
        ref={titleRef}
        tabIndex={titleTabIndex}
        aria-describedby={titleDescribedBy}
        className={styles.title}
      >
        {title}
      </h1>
      <p id={introId} className={styles.intro}>
        {intro}
      </p>
    </header>
  );
}

export default AuthPageHeader;
