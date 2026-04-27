import { Outlet } from "react-router";
import { useTranslation } from "react-i18next";

function AuthLayout() {
  const { t } = useTranslation();

  return (
    <>
      <a
        className="sr-only absolute left-4 top-4 z-50 rounded-md bg-ink px-3.5 py-2 text-sm font-medium text-white focus:not-sr-only focus:inline-flex focus:min-h-11 focus:items-center focus:outline-none focus:ring-4 focus:ring-brand-500/25"
        href="#main-content"
      >
        {t("accessibility.skipToContent")}
      </a>

      <main
        id="main-content"
        tabIndex={-1}
        className="flex min-h-screen min-h-dvh items-center justify-center px-4 py-3 sm:px-6"
      >
        <section
          className="mx-auto w-full max-w-[32rem] rounded-[1.25rem] border border-border-subtle bg-panel p-5 shadow-panel motion-safe:animate-panel-in sm:p-6"
        >
          <Outlet />
        </section>
      </main>
    </>
  );
}

export default AuthLayout;
