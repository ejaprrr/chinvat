import { useCallback, useEffect, useState } from 'react';
import type { AuthSessionResponse } from '../types/auth';
import { listSessions, revokeAllSessions, revokeSession } from '../api/auth';

export function useSessions() {
  const [sessions, setSessions] = useState<AuthSessionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshSessions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const nextSessions = await listSessions();
      setSessions(nextSessions);
    } catch (sessionError) {
      setError(sessionError instanceof Error ? sessionError.message : 'Unable to load sessions');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Defer refresh to avoid synchronous state changes in effect
    Promise.resolve().then(() => void refreshSessions());
  }, [refreshSessions]);

  const revokeOne = useCallback(async (sessionId: string) => {
    await revokeSession(sessionId);
    await refreshSessions();
  }, [refreshSessions]);

  const revokeAll = useCallback(async () => {
    await revokeAllSessions();
    await refreshSessions();
  }, [refreshSessions]);

  return {
    sessions,
    loading,
    error,
    refreshSessions,
    revokeSession: revokeOne,
    revokeAllSessions: revokeAll,
  };
}
