import api from "./axios";

export interface UserProfile {
  id?: string;
  userId: string;
  displayName?: string;
  bio?: string;
  avatarUrl?: string;
  coverUrl?: string;
  dateOfBirth?: string;
  gender?: string;
  phone?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type Theme = "LIGHT" | "DARK" | "SYSTEM";
export type ProfileVisibility = "PUBLIC" | "FRIENDS_ONLY" | "PRIVATE";

export interface UserSettings {
  id?: string;
  userId: string;
  language: string;
  timezone: string;
  theme: Theme;
  profileVisibility: ProfileVisibility;
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserProfileRequest {
  displayName?: string;
  bio?: string;
  avatarUrl?: string;
  coverUrl?: string;
  dateOfBirth?: string;
  gender?: string;
  phone?: string;
}

export interface UserSettingsRequest {
  language?: string;
  timezone?: string;
  theme?: Theme;
  profileVisibility?: ProfileVisibility;
  emailNotifications?: boolean;
  pushNotifications?: boolean;
  smsNotifications?: boolean;
}

export interface Address {
  id: string;
  userId: string;
  label?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  default: boolean;
}

export interface AddressRequest {
  label?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  isDefault?: boolean;
}

const userPath = (userId: string) => `/users/${encodeURIComponent(userId)}`;

export const getUserProfileApi = async (userId: string): Promise<UserProfile> =>
  (await api.get(`${userPath(userId)}/profile`)).data;

export const updateUserProfileApi = async (
  userId: string,
  payload: UserProfileRequest,
): Promise<UserProfile> => (await api.put(`${userPath(userId)}/profile`, payload)).data;

export const deleteUserProfileApi = async (userId: string): Promise<void> => {
  await api.delete(`${userPath(userId)}/profile`);
};

export const getUserSettingsApi = async (userId: string): Promise<UserSettings> =>
  (await api.get(`${userPath(userId)}/settings`)).data;

export const updateUserSettingsApi = async (
  userId: string,
  payload: UserSettingsRequest,
): Promise<UserSettings> => (await api.patch(`${userPath(userId)}/settings`, payload)).data;

export interface Follow {
  id: string;
  followerId: string;
  followeeId: string;
  createdAt: string;
}

export interface FollowCounts {
  userId: string;
  followersCount: number;
  followingCount: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

const paged = (page = 0, size = 20) => ({ params: { page, size } });

export const followUserApi = async (userId: string, targetUserId: string): Promise<Follow> =>
  (await api.post(`${userPath(userId)}/follows/${encodeURIComponent(targetUserId)}`)).data;

export const unfollowUserApi = async (userId: string, targetUserId: string): Promise<void> => {
  await api.delete(`${userPath(userId)}/follows/${encodeURIComponent(targetUserId)}`);
};

export const getFollowingApi = async (userId: string, page = 0, size = 20): Promise<PageResponse<Follow>> =>
  (await api.get(`${userPath(userId)}/follows/following`, paged(page, size))).data;

export const getFollowersApi = async (userId: string, page = 0, size = 20): Promise<PageResponse<Follow>> =>
  (await api.get(`${userPath(userId)}/follows/followers`, paged(page, size))).data;

export const getFollowCountsApi = async (userId: string): Promise<FollowCounts> =>
  (await api.get(`${userPath(userId)}/follows/counts`)).data;

export const blockUserApi = async (userId: string, targetUserId: string): Promise<void> => {
  await api.post(`${userPath(userId)}/blocks/${encodeURIComponent(targetUserId)}`);
};

export const unblockUserApi = async (userId: string, targetUserId: string): Promise<void> => {
  await api.delete(`${userPath(userId)}/blocks/${encodeURIComponent(targetUserId)}`);
};

export const isUserBlockedApi = async (userId: string, targetUserId: string): Promise<boolean> =>
  (await api.get(`${userPath(userId)}/blocks/${encodeURIComponent(targetUserId)}/status`)).data;

export const getBlockedUsersApi = async (userId: string, page = 0, size = 20): Promise<PageResponse<string>> =>
  (await api.get(`${userPath(userId)}/blocks`, paged(page, size))).data;

export const getAddressesApi = async (userId: string): Promise<Address[]> =>
  (await api.get(`${userPath(userId)}/addresses`)).data;

export const addAddressApi = async (userId: string, payload: AddressRequest): Promise<Address> =>
  (await api.post(`${userPath(userId)}/addresses`, payload)).data;

export const updateAddressApi = async (userId: string, addressId: string, payload: AddressRequest): Promise<Address> =>
  (await api.put(`${userPath(userId)}/addresses/${encodeURIComponent(addressId)}`, payload)).data;

export const deleteAddressApi = async (userId: string, addressId: string): Promise<void> => {
  await api.delete(`${userPath(userId)}/addresses/${encodeURIComponent(addressId)}`);
};
