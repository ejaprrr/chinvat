import { Fragment } from 'react';
import { cx } from '@/shared/lib/cx';

interface ProgressStepperProps {
  ariaLabel?: string;
  steps: string[];
  currentStep: number;
  totalSteps?: number;
  className?: string;
}

function ProgressStepper({
  ariaLabel = 'Progress',
  steps,
  currentStep,
  totalSteps = steps.length,
  className = '',
}: ProgressStepperProps) {
  const lastStepIndex = Math.max(steps.length - 1, 0);
  const activeStepIndex = Math.min(Math.max(currentStep, 0), lastStepIndex);
  const completedStepCount = Math.min(activeStepIndex + 1, totalSteps);

  return (
    <div className={cx('flex items-center gap-2', className)} aria-label={ariaLabel}>
      {' '}
      {steps.map((step, idx) => (
        <Fragment key={step}>
          <div
            className={cx(
              'h-2 w-2 rounded-full',
              idx <= activeStepIndex ? 'bg-brand-500' : 'bg-border-subtle',
            )}
            aria-current={idx === activeStepIndex ? 'step' : undefined}
            title={step}
          />
          {idx < steps.length - 1 && (
            <div
              className={cx(
                'h-0.5 w-4',
                idx < activeStepIndex ? 'bg-brand-500' : 'bg-border-subtle',
              )}
            />
          )}
        </Fragment>
      ))}
      <span className="ml-2 text-xs font-semibold text-muted">
        {completedStepCount}/{totalSteps}
      </span>
    </div>
  );
}

export default ProgressStepper;
