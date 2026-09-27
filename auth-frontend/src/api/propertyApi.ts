import axios from "axios";

// Keep property requests same-origin in development so PATCH preflight is handled by Vite.
// const propertyApi = axios.create({ baseURL: "/property-api" });
const propertyApi = axios.create({
  baseURL: import.meta.env.VITE_PROPERTY_API_BASE_URL ?? "http://localhost:8080/api/",
});

propertyApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers["Authorization"] = `Bearer ${token}`;

  return config;
});

propertyApi.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message;
    if (message && error instanceof Error) {
      error.message = message;
    }
    return Promise.reject(error);
  },
);

export type PropertyType = "HOUSE" | "APARTMENT" | "VILLA" | "PLOT" | "COMMERCIAL";
export type PropertyStatus = "PENDING" | "APPROVED" | "REJECTED" | "SOLD" | "RENTED";
export type Amenity =
  | "PARKING"
  | "ELEVATOR"
  | "SECURITY"
  | "GYM"
  | "SWIMMING_POOL"
  | "MOSQUE"
  | "SCHOOL_NEARBY"
  | "HOSPITAL_NEARBY"
  | "INTERNET"
  | "GENERATOR";

export interface PropertyRequestDto {
  title: string;
  description?: string;
  propertyType: PropertyType;
  price: number;
  area?: number;
  bedrooms?: number;
  bathrooms?: number;
  floors?: number;
  city: string;
  areaName?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  amenities?: Amenity[];
  featured?: boolean;
}

export interface PropertyImageUploadRequestDto {
  imageUrl: string;
  fileName?: string;
  fileSize?: number;
  coverImage: boolean;
  sortOrder?: number;
}

export interface PropertyReviewRequestDto {
  rating: number;
  comment?: string;
}

export interface PropertyStatusUpdateDto {
  status: PropertyStatus;
  reason?: string;
}

export interface PropertyImageDto {
  id: string;
  imageUrl: string;
  coverImage: boolean;
  fileName?: string;
  fileSize?: number;
  sortOrder?: number;
}

export interface PropertyResponseDto extends PropertyRequestDto {
  id: string;
  sellerId: string;
  status: PropertyStatus;
  active?: boolean;
  totalViews?: number;
  averageRating?: number;
  reviewCount?: number;
  images: PropertyImageDto[];
  amenities?: Amenity[];
  createdAt: string;
  updatedAt: string;
}

export interface PropertyReviewResponseDto {
  id: string;
  buyerId: string;
  propertyId: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface PropertySummaryDto {
  id: string;
  title: string;
  propertyType: PropertyType;
  price: number;
  area: number;
  bedrooms: number;
  bathrooms: number;
  city: string;
  areaName?: string;
  coverImageUrl?: string;
  status: PropertyStatus;
  featured: boolean;
  createdAt: string;
}

export interface FavoriteResponseDto {
  id: string;
  propertyId: string;
  property: PropertySummaryDto;
  createdAt: string;
}

export interface PageResponseDto<T> {
  content: T[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PropertySearchFilterDto {
  city?: string;
  propertyType?: PropertyType;
  minPrice?: number;
  maxPrice?: number;
  minBedrooms?: number;
  maxBedrooms?: number;
  minArea?: number;
  maxArea?: number;
  status?: PropertyStatus;
  featured?: boolean;
  keyword?: string;
  sortBy?: string;
  sortDirection?: string;
  pageNo?: number;
  pageSize?: number;
}

export const createPropertyApi = async (payload: PropertyRequestDto): Promise<PropertyResponseDto> =>
  (await propertyApi.post("/properties", payload)).data;

export const getPropertyApi = async (propertyId: string): Promise<PropertyResponseDto> =>
  (await propertyApi.get(`/properties/${propertyId}`)).data;

export const updatePropertyApi = async (propertyId: string, payload: Partial<PropertyRequestDto>): Promise<PropertyResponseDto> =>
  (await propertyApi.put(`/properties/${propertyId}`, payload)).data;

export const deletePropertyApi = async (propertyId: string): Promise<void> => {
  await propertyApi.delete(`/properties/${propertyId}`);
};

export const getMyPropertiesApi = async (pageNo = 0, pageSize = 10): Promise<PageResponseDto<PropertySummaryDto>> =>
  (await propertyApi.get("/properties/my", { params: { pageNo, pageSize } })).data;

export const addPropertyImagesApi = async (propertyId: string, images: PropertyImageUploadRequestDto[]): Promise<PropertyResponseDto> =>
  (await propertyApi.post(`/properties/${propertyId}/images`, images)).data;

export const addPropertyAmenitiesApi = async (propertyId: string, amenities: Amenity[]): Promise<PropertyResponseDto> =>
  (await propertyApi.post(`/properties/${propertyId}/amenities`, amenities)).data;

export const removeAmenityApi = async (propertyId: string, amenity: Amenity): Promise<void> => {
  await propertyApi.delete(`/properties/${propertyId}/amenities/${amenity}`);
};

export const updatePropertyStatusApi = async (propertyId: string, payload: PropertyStatusUpdateDto): Promise<PropertyResponseDto> =>
  (await propertyApi.patch(`/properties/${propertyId}/status`, payload)).data;

export const getPropertyReviewsApi = async (propertyId: string, pageNo = 0, pageSize = 10): Promise<PageResponseDto<PropertyReviewResponseDto>> =>
  (await propertyApi.get(`/properties/${propertyId}/reviews`, { params: { pageNo, pageSize } })).data;

export const addPropertyReviewApi = async (propertyId: string, payload: PropertyReviewRequestDto): Promise<PropertyReviewResponseDto> =>
  (await propertyApi.post(`/properties/${propertyId}/reviews`, payload)).data;

export const deleteReviewApi = async (reviewId: string): Promise<void> => {
  await propertyApi.delete(`/reviews/${reviewId}`);
};

export const deletePropertyImageApi = async (propertyId: string, imageId: string): Promise<void> => {
  await propertyApi.delete(`/properties/${propertyId}/images/${imageId}`);
};

export const togglePropertyFavoriteApi = async (propertyId: string): Promise<Record<string, boolean>> =>
  (await propertyApi.post(`/properties/${propertyId}/favorite`)).data;

export const searchPropertiesApi = async (filters: PropertySearchFilterDto): Promise<PageResponseDto<PropertySummaryDto>> =>
  (await propertyApi.get("/properties/search", { params: filters })).data;

export const getFeaturedPropertiesApi = async (pageNo = 0, pageSize = 3): Promise<PageResponseDto<PropertySummaryDto>> =>
  (await propertyApi.get("/properties/featured", { params: { pageNo, pageSize } })).data;

export const getBuyerFavoritesApi = async (pageNo = 0, pageSize = 10): Promise<PageResponseDto<FavoriteResponseDto>> =>
  (await propertyApi.get("/favorites/my", { params: { pageNo, pageSize } })).data;

export default propertyApi;
