import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { useDocumentTitle } from '@/shared/lib/documentTitle';
import { useAuth } from '@/shared/auth';
import { handleEidasCallback } from '@/features/eidas/api';
import { appRoutes } from '../router/routes';

function EidasCallbackPage() {
  useDocumentTitle('meta.pageTitle');
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { refreshUser } = useAuth();

  useEffect(() => {
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
        await handleEidasCallback({
          providerCode,
          state,
          authorizationCode,
          externalSubjectId,
          levelOfAssurance,
        });

        // Refresh auth state
        await refreshUser();

        // Redirect to profile
        navigate(appRoutes.profile, { replace: true });
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
  }, [searchParams, navigate, refreshUser]);

  return (
    <div className="flex items-center justify-center min-h-screen bg-canvas">
      <div className="text-center">
        <p className="text-muted">Processing eIDAS authentication...</p>
      </div>
    </div>
  );
}

export default EidasCallbackPage;
