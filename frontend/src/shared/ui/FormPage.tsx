import type { ReactNode, Ref } from 'react';
import FormStatus from './FormStatus';
import FormPageHeader from './FormPageHeader';

type FormPageStatus = {
  content: ReactNode;
  tone?: 'default' | 'critical' | 'warning';
} | null;

type FormPageProps = {
  action?: ReactNode;
  'aria-labelledby': string;
  children: ReactNode;
  footer?: ReactNode;
  intro: string;
  introId?: string;
  progress?: ReactNode;
  status?: FormPageStatus;
  title: string;
  titleDescribedBy?: string;
  titleId: string;
  titleRef?: Ref<HTMLHeadingElement>;
  titleTabIndex?: number;
};

function FormPage({
  action,
  'aria-labelledby': ariaLabelledBy,
  children,
  footer,
  intro,
  introId,
  progress,
  status,
  title,
  titleDescribedBy,
  titleId,
  titleRef,
  titleTabIndex,
}: FormPageProps) {
  return (
    <div className="form-flow" aria-labelledby={ariaLabelledBy}>
      {progress}

      <FormPageHeader
        action={action}
        id={titleId}
        intro={intro}
        introId={introId}
        title={title}
        titleDescribedBy={titleDescribedBy}
        titleRef={titleRef}
        titleTabIndex={titleTabIndex}
      />

      <div className={status ? 'block' : 'hidden'} aria-hidden={status ? undefined : 'true'}>
        {status ? <FormStatus tone={status.tone}>{status.content}</FormStatus> : null}{' '}
      </div>

      {children}

      {footer}
    </div>
  );
}

export type { FormPageStatus };
export default FormPage;
