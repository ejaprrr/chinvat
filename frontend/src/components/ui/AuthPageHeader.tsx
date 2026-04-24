type AuthPageHeaderProps = {
  id: string;
  intro: string;
  title: string;
};

const styles = {
  root: "flex flex-col gap-1",
  title: "text-[1.1875rem] font-semibold tracking-[-0.02em] text-ink",
  intro: "text-sm text-muted",
} as const;

function AuthPageHeader({ id, intro, title }: AuthPageHeaderProps) {
  return (
    <header className={styles.root}>
      <h1 id={id} className={styles.title}>
        {title}
      </h1>
      <p className={styles.intro}>{intro}</p>
    </header>
  );
}

export default AuthPageHeader;
