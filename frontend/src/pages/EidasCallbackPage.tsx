import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { useTranslation } from 'react-i18next';
import { useDocumentTitle } from '@/shared/lib/documentTitle';
import { handleEidasCallback } from '@/features/eidas/api';
import { appRoutes } from '../router/routes';

function EidasCallbackPage() {
  useDocumentTitle('meta.pageTitle');
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const hasProcessed = useRef(false);

  useEffect(() => {
    // Guard against React StrictMode double-invocation in development
    if (hasProcessed.current) return;
    hasProcessed.current = true;

    const processCallback = async () => {
      try {
        // Extract parameters from query string
        const providerCode = searchParams.get('provider_code');
        const state = searchParams.get('state');
        const authorizationCode = searchParams.get('code');
        const externalSubjectId = searchParams.get('subject_id');
        const levelOfAssurance = searchParams.get('loa') || 'high';

        if (!providerCode || !state || !authorizationCode || !externalSubjectId) {
          throw new Error('Missing required callback parameters');
        }

        // Process the eIDAS callback
        const result = await handleEidasCallback({
          providerCode,
          state,
          authorizationCode,
          externalSubjectId,
          levelOfAssurance,
        });

        if (result.profileCompletionRequired) {
          // New user: navigate to registration with eIDAS context to complete profile
          navigate(appRoutes.register, {
            replace: true,
            state: {
              eidasCallback: {
                providerCode: result.providerCode,
                externalSubjectId: result.externalSubjectId,
                levelOfAssurance: result.levelOfAssurance,
              },
            },
          });
        } else {
          // Existing linked user: no session token is issued in this flow — redirect to sign in
          navigate(appRoutes.login, {
            replace: true,
            state: { message: t('auth.eidas.callbackVerified') },
          });
        }
      } catch (error) {
        // On error, redirect to login with error state
        const message = error instanceof Error ? error.message : 'eIDAS callback failed';
        navigate(appRoutes.login, {
          replace: true,
          state: { error: message },
        });
      }
    };

    processCallback();
  }, [searchParams, navigate, t]);

  return (
    <div className="flex items-center justify-center min-h-screen bg-canvas">
      <div className="text-center">
        <p className="text-muted">Processing eIDAS authentication...</p>
      </div>
    </div>
  );
}

export default EidasCallbackPage;
