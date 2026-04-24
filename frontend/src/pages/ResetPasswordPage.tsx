import ResetPasswordForm from "../components/ResetPasswordForm";
import useDocumentTitle from "../hooks/useDocumentTitle";

function ResetPasswordPage() {
  useDocumentTitle("meta.resetPasswordPageTitle");

  return <ResetPasswordForm />;
}

export default ResetPasswordPage;
