import { useTranslation } from "react-i18next";
import { appRoutes } from "../router/paths";
import {
  ActionAnchor,
  ActionLink,
} from "./ui/Action";
import AuthPageHeader from "./ui/AuthPageHeader";
import InfoPanel from "./ui/InfoPanel";

type ResetPasswordFormProps = {
  recoveryUrl: string | null;
};

const styles = {
  root: "flex flex-col gap-4",
  actions: "flex flex-col gap-2.5",
} as const;

function ResetPasswordForm({ recoveryUrl }: ResetPasswordFormProps) {
  const { t } = useTranslation();
  const hasRecoveryUrl = Boolean(recoveryUrl);

  return (
    <div className={styles.root} aria-labelledby="reset-password-title">
      <AuthPageHeader
        id="reset-password-title"
        title={t("auth.resetPassword.title")}
        intro={t("auth.resetPassword.intro")}
      />

      <InfoPanel role="status" aria-live="polite">
        {hasRecoveryUrl
          ? t("auth.resetPassword.configuredHint")
          : t("auth.resetPassword.unavailable")}
      </InfoPanel>

      <div className={styles.actions}>
        {hasRecoveryUrl ? (
          <ActionAnchor
            href={recoveryUrl ?? undefined}
            variant="primary"
          >
            {t("auth.actions.continueToReset")}
          </ActionAnchor>
        ) : null}

        <ActionLink
          to={appRoutes.login}
          variant={hasRecoveryUrl ? "secondary" : "primary"}
        >
          {t("auth.actions.backToSignIn")}
        </ActionLink>
      </div>
    </div>
  );
}

export default ResetPasswordForm;
