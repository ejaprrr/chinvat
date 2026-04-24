import ResetPasswordForm from "../components/ResetPasswordForm";
import { getConfiguredResetPasswordUrl } from "../api/auth";
import useDocumentTitle from "../hooks/useDocumentTitle";

function ResetPasswordPage() {
  useDocumentTitle("meta.resetPasswordPageTitle");

  return <ResetPasswordForm recoveryUrl={getConfiguredResetPasswordUrl()} />;
}

export default ResetPasswordPage;
