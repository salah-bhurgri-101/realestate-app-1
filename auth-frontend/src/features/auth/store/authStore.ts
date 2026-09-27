import { create } from "zustand";
import type { UserResponse } from "../types/auth.types";

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserResponse | null;
  isLoading: boolean;

  setTokens: (accessToken: string, refreshToken: string) => void;
  clearTokens: () => void;
  setUser: (user: UserResponse) => void;
  clearUser: () => void;
  setIsLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: localStorage.getItem("accessToken") ?? null,
  refreshToken: localStorage.getItem("refreshToken") ?? null,
  user: (() => {
    const storedUser = localStorage.getItem("authUser");
    if (!storedUser) return null;

    try {
      return JSON.parse(storedUser) as UserResponse;
    } catch {
      localStorage.removeItem("authUser");
      return null;
    }
  })(),
  isLoading: false,

  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    set({
      accessToken,
      refreshToken,
    });
  },

  clearTokens: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");

    set({
      accessToken: null,
      refreshToken: null,
    });
  },

  setUser: (user) => {
    localStorage.setItem("userId", user.id);
    localStorage.setItem("authUser", JSON.stringify(user));
    set({ user });
  },

  clearUser: () => {
    localStorage.removeItem("userId");
    localStorage.removeItem("authUser");
    set({ user: null });
  },

  setIsLoading: (loading) => {
    set({ isLoading: loading });
  },
}));