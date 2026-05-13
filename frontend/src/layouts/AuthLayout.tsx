<<<<<<< HEAD
import { Outlet } from 'react-router';
import { useTranslation } from 'react-i18next';

function AuthLayout() {
  const { t } = useTranslation();
  const isPopupWindow = typeof window !== 'undefined' && Boolean(window.opener);
=======
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
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

  return (
    <>
      <a
        className="sr-only absolute left-4 top-4 z-50 rounded-md bg-ink px-3.5 py-2 text-sm font-medium text-white focus:not-sr-only focus:inline-flex focus:min-h-11 focus:items-center focus:outline-none focus:ring-4 focus:ring-brand-500/25"
        href="#main-content"
      >
<<<<<<< HEAD
        {t('accessibility.skipToContent')}
=======
        {t("accessibility.skipToContent")}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      </a>

      <main
        id="main-content"
        tabIndex={-1}
<<<<<<< HEAD
        className={`auth-popup-shell${isPopupWindow ? ' auth-popup-shell--window' : ''}`}
      >
        <section className={`auth-popup${isPopupWindow ? ' auth-popup--window' : ''}`}>
=======
        className={`auth-popup-shell${isPopupWindow ? " auth-popup-shell--window" : ""}`}
      >
        <section
          className={`auth-popup${isPopupWindow ? " auth-popup--window" : ""}`}
        >
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          <Outlet />
        </section>
      </main>
    </>
  );
}

export default AuthLayout;
