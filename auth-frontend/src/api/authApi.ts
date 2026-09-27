import api from "./axios";
import type { UserResponse } from "../features/auth/types/auth.types";

export const getAdminUsersApi = async (role?: string): Promise<UserResponse[]> =>
  (await api.get("/auth/admin/users", { params: role ? { role } : undefined })).data;

export const updateAdminUserStatusApi = async (userId: string, active: boolean): Promise<UserResponse> =>
  (await api.patch(`/auth/admin/users/${userId}/status`, { active })).data;

export const updateAdminUserRoleApi = async (userId: string, role: string): Promise<UserResponse> =>
  (await api.patch(`/auth/admin/users/${userId}/role`, { role })).data;

export const loginApi = async (payload: any) => {
  const response = await api.post(
    "/auth/login",
    payload
  );

  return response.data;
};

export const registerApi = async (payload: any) => {
  const response = await api.post(
    "/auth/register",
    payload
  );

  return response.data;
};

export const getMeApi = async () => {
  const response = await api.get(
    "/auth/me"
  );

  return response.data;
};

export const verifyOtpApi = async (
  payload: any
) => {
  const response = await api.post(
    "/auth/otp/verify",
    payload
  );

  return response.data;
};

export const resendOtpApi = async (
  payload: any
) => {
  const response = await api.post(
    "/auth/otp/resend",
    payload
  );

  return response.data;
};

export const logoutApi = async (refreshToken: string) => {
  const response = await api.post(
    "/auth/logout",
    { refreshToken }
  );

  return response.data;
};

export const logoutAllApi = async () => {
  const response = await api.post(
    "/auth/logout/all"
  );

  return response.data;
};

export const refreshTokenApi = async (refreshToken: string) => {
  const response = await api.post(
    "/auth/token/refresh",
    { refreshToken }
  );

  return response.data;
};

export const forgotPasswordApi = async (email: string) => {
  const response = await api.post(
    "/auth/password/forgot",
    { email }
  );

  return response.data;
};

export const resetPasswordApi = async (
  email: string,
  otp: string,
  newPassword: string
) => {
  const response = await api.post(
    "/auth/password/reset",
    { email, otp, newPassword }
  );

  return response.data;
};

export const changePasswordApi = async (
  currentPassword: string,
  newPassword: string
) => {
  const response = await api.patch(
    "/auth/password/change",
    { currentPassword, newPassword }
  );

  return response.data;
};