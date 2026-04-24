import AuthForm from "../components/AuthForm";
import { getCertificateLoginUrl } from "../api/auth";
import useDocumentTitle from "../hooks/useDocumentTitle";
import useIsDesktopPlatform from "../hooks/useIsDesktopPlatform";

function LoginPage() {
  useDocumentTitle("meta.pageTitle");
  const isDesktopPlatform = useIsDesktopPlatform();

  const startCertificateLogin = () => {
    window.location.assign(getCertificateLoginUrl());
  };

  return (
    <AuthForm
      isDesktopPlatform={isDesktopPlatform}
      onCertificateLogin={startCertificateLogin}
    />
  );
}

export default LoginPage;
