import type { FormHTMLAttributes, HTMLAttributes, ReactNode } from "react";
import { cx } from "../../lib/cx";
import ActionGroup from "../actions/ActionGroup";

type AuthFormProps = FormHTMLAttributes<HTMLFormElement>;

function AuthForm({ className, ...props }: AuthFormProps) {
  return <form {...props} className={cx("auth-form", className)} noValidate />;
}

type AuthFormSectionProps = HTMLAttributes<HTMLDivElement>;

function AuthFormSection({ className, ...props }: AuthFormSectionProps) {
  return <div {...props} className={cx("auth-section-stack", className)} />;
}

type FormActionsProps = HTMLAttributes<HTMLDivElement>;

function FormActions({ className, ...props }: FormActionsProps) {
  return (
    <ActionGroup
      {...props}
      className={cx("form-actions", className)}
      direction="column"
    />
  );
}

type AuthStepFormProps = AuthFormProps & {
  actions: ReactNode;
  children: ReactNode;
};

function AuthStepForm({
  actions,
  children,
  className,
  ...props
}: AuthStepFormProps) {
  return (
    <AuthForm {...props} className={className}>
      <AuthFormSection>{children}</AuthFormSection>
      <FormActions>{actions}</FormActions>
    </AuthForm>
  );
}

export { AuthForm, AuthFormSection, AuthStepForm, FormActions };
