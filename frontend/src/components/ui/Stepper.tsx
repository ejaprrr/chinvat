import React from "react";

interface StepperProps {
  steps: string[];
  currentStep: number;
  totalSteps: number;
  className?: string;
}

const Stepper: React.FC<StepperProps> = ({
  steps,
  currentStep,
  totalSteps,
  className = "",
}) => {
  return (
    <div
      className={`flex items-center gap-2 ${className}`}
      aria-label="progress"
    >
      {steps.map((step, idx) => (
        <React.Fragment key={step}>
          <div
            className={`h-2 w-2 rounded-full ${idx < currentStep ? "bg-brand-500" : "bg-border-subtle"}`}
            aria-current={idx === currentStep ? "step" : undefined}
            title={step}
          />
          {idx < steps.length - 1 && (
            <div className="h-0.5 w-4 bg-border-subtle" />
          )}
        </React.Fragment>
      ))}
      <span className="ml-2 text-xs text-muted font-semibold">
        {currentStep + 1}/{totalSteps}
      </span>
    </div>
  );
};

export default Stepper;
