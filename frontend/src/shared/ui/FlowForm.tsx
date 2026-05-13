import type { FormHTMLAttributes, HTMLAttributes, ReactNode } from 'react';
import { cx } from '@/shared/lib/cx';
import ActionGroup from './ActionGroup';

type FlowFormProps = FormHTMLAttributes<HTMLFormElement>;

function FlowForm({ className, ...props }: FlowFormProps) {
  return <form {...props} className={cx('flow-form', className)} noValidate />;
}

type FlowFormSectionProps = HTMLAttributes<HTMLDivElement>;

function FlowFormSection({ className, ...props }: FlowFormSectionProps) {
  return <div {...props} className={cx('flow-section-stack', className)} />;
}

type FormActionsProps = HTMLAttributes<HTMLDivElement>;

function FormActions({ className, ...props }: FormActionsProps) {
  return <ActionGroup {...props} className={cx('form-actions', className)} direction="column" />;
}

type FlowStepFormProps = FlowFormProps & {
  actions: ReactNode;
  children: ReactNode;
};

function FlowStepForm({ actions, children, className, ...props }: FlowStepFormProps) {
  return (
    <FlowForm {...props} className={className}>
      <FlowFormSection>{children}</FlowFormSection>
      <FormActions>{actions}</FormActions>
    </FlowForm>
  );
}

export { FlowForm, FlowFormSection, FlowStepForm, FormActions };
