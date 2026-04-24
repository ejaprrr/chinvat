import AuthForm from "../components/AuthForm";
import { getCertificateLoginUrl } from "../api/auth";
import useDocumentTitle from "../hooks/useDocumentTitle";

function LoginPage() {
  useDocumentTitle("meta.pageTitle");

  const isDesktopPlatform =
    typeof window !== "undefined" &&
    window.matchMedia(
      "(pointer: fine) and (hover: hover) and (min-width: 768px)",
    ).matches;

  const startCertificateLogin = () => {
    window.location.href = getCertificateLoginUrl();
  };

  return (
    <AuthForm
      isDesktopPlatform={isDesktopPlatform}
      onCertificateLogin={startCertificateLogin}
    />
  );
}

export default LoginPage;
