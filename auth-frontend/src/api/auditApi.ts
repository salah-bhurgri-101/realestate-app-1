import axios from "axios";
import type { AuditLogQueryParams, AuditLogRequest, AuditLogResponse, PageAuditLogResponse } from "../types/api";

const auditApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1",
});

auditApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers["Authorization"] = "Bearer " + token;
  return config;
});

const normalizePageableParams = (params: AuditLogQueryParams = {}) => {
  const { pageable, ...rest } = params;
  return { ...rest, ...(pageable ? { page: pageable.page ?? 0, size: pageable.size ?? 20, ...(pageable.sort?.length ? { sort: pageable.sort } : {}) } : {}) };
};

export const searchAuditLogsApi = async (params: AuditLogQueryParams = {}): Promise<PageAuditLogResponse> => (await auditApi.get("/audit-logs", { params: normalizePageableParams(params) })).data;
export const createAuditLogApi = async (payload: AuditLogRequest): Promise<AuditLogResponse> => (await auditApi.post("/audit-logs", payload)).data;
export const createAsyncAuditLogApi = async (payload: AuditLogRequest): Promise<void> => { await auditApi.post("/audit-logs/async", payload); };
export const getAuditLogByIdApi = async (id: string): Promise<AuditLogResponse> => (await auditApi.get(`/audit-logs/${encodeURIComponent(id)}`)).data;

export default auditApi;
