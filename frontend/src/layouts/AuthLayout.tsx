import { Outlet } from "react-router";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "../components/LanguageSwitcher";

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
        className="grid min-h-screen min-h-dvh place-items-center px-4 py-4 sm:px-6"
      >
        <section
          className="w-full max-w-[29rem] rounded-[0.875rem] border border-border-subtle bg-panel p-6 shadow-panel motion-safe:animate-panel-in sm:p-8"
        >
          <header className="mb-5 flex items-center justify-between gap-4">
            <p className="font-display text-[0.8125rem] font-semibold uppercase tracking-[0.1em] text-ink">
              {t("brand.projectName")}
            </p>
            <LanguageSwitcher />
          </header>
          <Outlet />
        </section>
      </main>
    </>
  );
}

export default AuthLayout;
