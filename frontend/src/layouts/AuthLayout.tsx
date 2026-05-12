import { Outlet } from "react-router";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAuth } from "../contexts/auth";

function AuthLayout() {
  const { t } = useTranslation();
  const isPopupWindow = typeof window !== "undefined" && Boolean(window.opener);
  const { error } = useAuth();

  useEffect(() => {
    // surface error value for debugging
    // eslint-disable-next-line no-console
    console.error("AuthLayout observed auth.error:", error);
  }, [error]);

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
        className={`auth-popup-shell${isPopupWindow ? " auth-popup-shell--window" : ""}`}
      >
        <section
          className={`auth-popup${isPopupWindow ? " auth-popup--window" : ""}`}
        >
          <Outlet />
        </section>
      </main>
    </>
  );
}

export default AuthLayout;
