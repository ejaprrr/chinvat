<<<<<<< HEAD
import type { FormHTMLAttributes, HTMLAttributes, ReactNode } from 'react';
import { cx } from '../../lib/cx';
import { ActionGroup } from '../../components/forms';
=======
import type { FormHTMLAttributes, HTMLAttributes, ReactNode } from "react";
import { cx } from "../../lib/cx";
import { ActionGroup } from "../../components/forms";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

type AuthFormProps = FormHTMLAttributes<HTMLFormElement>;

function AuthForm({ className, ...props }: AuthFormProps) {
<<<<<<< HEAD
  return <form {...props} className={cx('auth-form', className)} noValidate />;
=======
  return <form {...props} className={cx("auth-form", className)} noValidate />;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

type AuthFormSectionProps = HTMLAttributes<HTMLDivElement>;

function AuthFormSection({ className, ...props }: AuthFormSectionProps) {
<<<<<<< HEAD
  return <div {...props} className={cx('auth-section-stack', className)} />;
=======
  return <div {...props} className={cx("auth-section-stack", className)} />;
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

type FormActionsProps = HTMLAttributes<HTMLDivElement>;

function FormActions({ className, ...props }: FormActionsProps) {
<<<<<<< HEAD
  return <ActionGroup {...props} className={cx('form-actions', className)} direction="column" />;
=======
  return (
    <ActionGroup
      {...props}
      className={cx("form-actions", className)}
      direction="column"
    />
  );
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}

type AuthStepFormProps = AuthFormProps & {
  actions: ReactNode;
  children: ReactNode;
};

<<<<<<< HEAD
function AuthStepForm({ actions, children, className, ...props }: AuthStepFormProps) {
=======
function AuthStepForm({
  actions,
  children,
  className,
  ...props
}: AuthStepFormProps) {
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return (
    <AuthForm {...props} className={className}>
      <AuthFormSection>{children}</AuthFormSection>
      <FormActions>{actions}</FormActions>
    </AuthForm>
  );
}

export { AuthForm, AuthFormSection, AuthStepForm, FormActions };
