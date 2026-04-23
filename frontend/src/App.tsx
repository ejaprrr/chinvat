import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import AuthForm from "./components/AuthForm";
import LanguageSwitcher from "./components/LanguageSwitcher";

function App() {
  const { i18n, t } = useTranslation();

  useEffect(() => {
    document.documentElement.lang = i18n.resolvedLanguage ?? i18n.language;
    document.title = t("meta.pageTitle");
  }, [i18n.language, i18n.resolvedLanguage, t]);

  const isDesktopPlatform =
    typeof window !== "undefined" &&
    window.matchMedia(
      "(pointer: fine) and (hover: hover) and (min-width: 768px)",
    ).matches;

  const startCertificateLogin = () => {
    window.location.href = "/cert-login";
  };

  return (
    <>
      <a className="skip-link" href="#main-content">
        {t("accessibility.skipToContent")}
      </a>

      <main id="main-content" className="page-shell">
        <section className="auth-panel" aria-labelledby="login-title">
          <header className="panel-header">
            <p className="wordmark" aria-label={t("brand.projectName")}>
              {t("brand.projectName")}
            </p>
            <LanguageSwitcher />
          </header>
          <AuthForm
            isDesktopPlatform={isDesktopPlatform}
            onCertificateLogin={startCertificateLogin}
          />
        </section>
      </main>
    </>
  );
}

export default App;
