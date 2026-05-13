<<<<<<< HEAD
import { useCallback, useEffect, useState } from 'react';
import { getCurrentUser } from '../lib/api/auth';
import { getUserById, updateUser } from '../lib/api/users';
import { getErrorCode } from '../lib/http/errors';
import type { AuthMeResponse } from '../types/auth';
import type { UpdateUserRequest, UserResponse } from '../types/user';
=======
import { useCallback, useEffect, useState } from "react";
import { getCurrentUser } from "../lib/api/auth";
import { getUserById, updateUser } from "../lib/api/users";
import { getErrorCode } from "../lib/http/errors";
import type { AuthMeResponse } from "../types/auth";
import type { UpdateUserRequest, UserResponse } from "../types/user";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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
<<<<<<< HEAD
      setError(getErrorCode(profileError, 'PROFILE_LOAD_FAILED'));
=======
      setError(getErrorCode(profileError, "PROFILE_LOAD_FAILED"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
        throw new Error('User id is not available');
=======
        throw new Error("User id is not available");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      }

      const resolvedId = profile?.id ?? userId ?? me?.id;
      if (!resolvedId) {
<<<<<<< HEAD
        throw new Error('User id is not available');
=======
        throw new Error("User id is not available");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
