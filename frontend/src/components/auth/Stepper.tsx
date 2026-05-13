<<<<<<< HEAD
import { Fragment } from 'react';
import { cx } from '../../lib/cx';
=======
import { Fragment } from "react";
import { cx } from "../../lib/cx";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

interface StepperProps {
  ariaLabel?: string;
  steps: string[];
  currentStep: number;
  totalSteps?: number;
  className?: string;
}

function Stepper({
<<<<<<< HEAD
  ariaLabel = 'Progress',
  steps,
  currentStep,
  totalSteps = steps.length,
  className = '',
=======
  ariaLabel = "Progress",
  steps,
  currentStep,
  totalSteps = steps.length,
  className = "",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
}: StepperProps) {
  const lastStepIndex = Math.max(steps.length - 1, 0);
  const activeStepIndex = Math.min(Math.max(currentStep, 0), lastStepIndex);
  const completedStepCount = Math.min(activeStepIndex + 1, totalSteps);

  return (
<<<<<<< HEAD
    <div className={cx('flex items-center gap-2', className)} aria-label={ariaLabel}>
=======
    <div
      className={cx("flex items-center gap-2", className)}
      aria-label={ariaLabel}
    >
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      {steps.map((step, idx) => (
        <Fragment key={step}>
          <div
            className={cx(
<<<<<<< HEAD
              'h-2 w-2 rounded-full',
              idx <= activeStepIndex ? 'bg-brand-500' : 'bg-border-subtle',
            )}
            aria-current={idx === activeStepIndex ? 'step' : undefined}
=======
              "h-2 w-2 rounded-full",
              idx <= activeStepIndex ? "bg-brand-500" : "bg-border-subtle",
            )}
            aria-current={idx === activeStepIndex ? "step" : undefined}
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
            title={step}
          />
          {idx < steps.length - 1 && (
            <div
              className={cx(
<<<<<<< HEAD
                'h-0.5 w-4',
                idx < activeStepIndex ? 'bg-brand-500' : 'bg-border-subtle',
=======
                "h-0.5 w-4",
                idx < activeStepIndex ? "bg-brand-500" : "bg-border-subtle",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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

export default Stepper;
