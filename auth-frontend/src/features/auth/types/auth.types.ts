export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  name: string;
  password: string;
  phone?: string;
  bio?: string;
  avatarUrl?: string;
  coverUrl?: string;
  dateOfBirth?: string;
  gender?: string;
}

export interface UserResponse {
  id: string;
  email: string;
  phone?: string;
  name: string;
  role: string;
  isVerified: boolean;
  isActive: boolean;
  createdAt: string;
}

export interface SessionResponse {
  id: string;
  deviceName: string;
  deviceType: string;
  ipAddress: string;
  lastActiveAt: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
}

export interface MeResponse {
  user: UserResponse;
  activeSessions: SessionResponse[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}