export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export type AuditLogAction =
  | "CREATE"
  | "READ"
  | "UPDATE"
  | "DELETE"
  | "LOGIN"
  | "LOGOUT"
  | "LOGIN_FAILED"
  | "ACCESS_DENIED"
  | "EXPORT"
  | "IMPORT"
  | "OTHER";

export interface Pageable {
  page?: number;
  size?: number;
  sort?: string[];
}

export interface AuditLogRequest {
  serviceName: string;
  action: AuditLogAction;
  entityType?: string;
  entityId?: string;
  username?: string;
  userId?: string;
  ipAddress?: string;
  description?: string;
  metadata?: Record<string, unknown>;
  status?: string;
  traceId?: string;
}

export interface AuditLogResponse extends AuditLogRequest {
  id?: string;
  createdAt?: string;
}

export interface AuditLogQueryParams extends Partial<AuditLogRequest> {
  from?: string;
  to?: string;
  pageable?: Pageable;
}

export interface PageAuditLogResponse {
  totalElements: number;
  totalPages: number;
  size: number;
  content: AuditLogResponse[];
  number: number;
  numberOfElements: number;
  sort?: {
    empty?: boolean;
    sorted?: boolean;
    unsorted?: boolean;
  };
  pageable?: {
    offset?: number;
    sort?: {
      empty?: boolean;
      sorted?: boolean;
      unsorted?: boolean;
    };
    paged?: boolean;
    pageSize?: number;
    pageNumber?: number;
    unpaged?: boolean;
  };
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}