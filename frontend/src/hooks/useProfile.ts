import { useCallback, useEffect, useState } from "react";
import { getCurrentUser } from "../lib/api/auth";
import { getUserById, updateUser } from "../lib/api/users";
import { getErrorCode } from "../lib/http/errors";
import type { AuthMeResponse } from "../types/auth";
import type { UpdateUserRequest, UserResponse } from "../types/user";

export function useProfile(userId?: number) {
  const [me, setMe] = useState<AuthMeResponse | null>(null);
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshProfile = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const currentUser = await getCurrentUser();
      setMe(currentUser);

      const resolvedId = userId ?? currentUser.id;
      if (resolvedId) {
        const userProfile = await getUserById(resolvedId);
        setProfile(userProfile);
      }
    } catch (profileError) {
      setError(getErrorCode(profileError, "PROFILE_LOAD_FAILED"));
      setMe(null);
      setProfile(null);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    Promise.resolve().then(() => void refreshProfile());
  }, [refreshProfile]);

  const saveProfile = useCallback(
    async (data: UpdateUserRequest) => {
      if (!profile?.id && !userId && !me?.id) {
        throw new Error("User id is not available");
      }

      const resolvedId = profile?.id ?? userId ?? me?.id;
      if (!resolvedId) {
        throw new Error("User id is not available");
      }

      const updated = await updateUser(resolvedId, data);
      setProfile(updated);
      return updated;
    },
    [me?.id, profile?.id, userId],
  );

  return {
    me,
    profile,
    loading,
    error,
    refreshProfile,
    saveProfile,
  };
}
