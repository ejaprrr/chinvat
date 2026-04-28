import RegisterForm from "../components/RegisterForm";
import useDocumentTitle from "../hooks/useDocumentTitle";

function RegisterPage() {
  useDocumentTitle("meta.registerPageTitle");

  return <RegisterForm />;
}

export default RegisterPage;
