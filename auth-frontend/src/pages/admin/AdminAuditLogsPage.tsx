import { useEffect, useState, type ChangeEvent, type FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  searchAuditLogsApi,
} from "../../api/auditApi";
import type {
  AuditLogAction,
  AuditLogResponse,
  PageAuditLogResponse,
} from "../../types/api";

const actionOptions: AuditLogAction[] = [
  "CREATE",
  "READ",
  "UPDATE",
  "DELETE",
  "LOGIN",
  "LOGOUT",
  "LOGIN_FAILED",
  "ACCESS_DENIED",
  "EXPORT",
  "IMPORT",
  "OTHER",
];

const pageSize = 10;

const AdminAuditLogsPage = () => {
  const [logs, setLogs] = useState<AuditLogResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [filters, setFilters] = useState({
    serviceName: "",
    action: "",
    username: "",
    entityType: "",
    entityId: "",
    from: "",
    to: "",
  });

  const fetchLogs = async (nextPage = page) => {
    setLoading(true);
    setError(null);

    try {
      const params = {
        pageable: { page: nextPage, size: pageSize },
        ...(filters.serviceName ? { serviceName: filters.serviceName } : {}),
        ...(filters.action ? { action: filters.action as AuditLogAction } : {}),
        ...(filters.username ? { username: filters.username } : {}),
        ...(filters.entityType ? { entityType: filters.entityType } : {}),
        ...(filters.entityId ? { entityId: filters.entityId } : {}),
        ...(filters.from ? { from: new Date(filters.from).toISOString() } : {}),
        ...(filters.to ? { to: new Date(filters.to).toISOString() } : {}),
      };

      const response: PageAuditLogResponse = await searchAuditLogsApi(params);
      setLogs(response.content ?? []);
      setTotalPages(response.totalPages ?? 0);
      setTotalElements(response.totalElements ?? 0);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load audit logs.";
      setError(message);
      setLogs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleFilterChange = (
    event: ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const handlePageChange = (nextPage: number) => {
    const safePage = Math.max(0, nextPage);
    setPage(safePage);
    fetchLogs(safePage);
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    setPage(0);
    fetchLogs(0);
  };

  return (
    <div className="max-w-7xl mx-auto p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Audit Logs</h2>
          <p className="text-gray-600">Track platform actions, login events, and security-related activity.</p>
        </div>
        <div className="flex items-center gap-2">
          <Link to="/admin/audit-logs/create" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700">Create log</Link>
          <Link to="/admin/audit-logs/create-async" className="rounded-lg border border-indigo-200 px-4 py-2 text-sm font-medium text-indigo-700 hover:bg-indigo-50">Create async</Link>
          <div className="rounded-lg bg-blue-50 px-4 py-2 text-sm font-medium text-blue-700">
            {totalElements} total events
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="mb-6 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-6 gap-3 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        <input
          name="serviceName"
          value={filters.serviceName}
          onChange={handleFilterChange}
          placeholder="Service name"
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />

        <select
          name="action"
          value={filters.action}
          onChange={handleFilterChange}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">All actions</option>
          {actionOptions.map((action) => (
            <option key={action} value={action}>
              {action}
            </option>
          ))}
        </select>

        <input
          name="username"
          value={filters.username}
          onChange={handleFilterChange}
          placeholder="Username"
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />

        <input
          name="entityType"
          value={filters.entityType}
          onChange={handleFilterChange}
          placeholder="Entity type"
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />

        <input
          name="entityId"
          value={filters.entityId}
          onChange={handleFilterChange}
          placeholder="Entity ID"
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />

        <div className="flex gap-2">
          <button
            type="submit"
            className="flex-1 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            Apply
          </button>
          <button
            type="button"
            onClick={() => {
              setFilters({
                serviceName: "",
                action: "",
                username: "",
                entityType: "",
                entityId: "",
                from: "",
                to: "",
              });
              setPage(0);
              fetchLogs(0);
            }}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            Clear
          </button>
        </div>

        <input
          type="datetime-local"
          name="from"
          value={filters.from}
          onChange={handleFilterChange}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />

        <input
          type="datetime-local"
          name="to"
          value={filters.to}
          onChange={handleFilterChange}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500"
        />
      </form>

      {error ? (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : null}

      <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-gray-50 text-gray-700">
              <tr>
                <th className="px-4 py-3 font-semibold">Time</th>
                <th className="px-4 py-3 font-semibold">Service</th>
                <th className="px-4 py-3 font-semibold">Action</th>
                <th className="px-4 py-3 font-semibold">User</th>
                <th className="px-4 py-3 font-semibold">Entity</th>
                <th className="px-4 py-3 font-semibold">Status</th>
                <th className="px-4 py-3 font-semibold">Trace ID</th>
                <th className="px-4 py-3 font-semibold">Description</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={8} className="px-4 py-10 text-center text-gray-500">
                    Loading audit logs...
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-4 py-10 text-center text-gray-500">
                    No audit logs found.
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id ?? `${log.serviceName}-${log.createdAt}-${log.traceId}`} className="border-t border-gray-200">
                    <td className="px-4 py-3 whitespace-nowrap text-gray-700">
                      {log.id ? <Link to={`/admin/audit-logs/${encodeURIComponent(log.id)}`} className="text-indigo-600 hover:underline">{log.createdAt ? new Date(log.createdAt).toLocaleString() : "View details"}</Link> : (log.createdAt ? new Date(log.createdAt).toLocaleString() : "—")}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-gray-700">{log.serviceName ?? "—"}</td>
                    <td className="px-4 py-3">
                      <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">
                        {log.action ?? "—"}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-700">{log.username ?? log.userId ?? "—"}</td>
                    <td className="px-4 py-3 text-gray-700">
                      {log.entityType ? `${log.entityType}:${log.entityId ?? ""}` : log.entityId ?? "—"}
                    </td>
                    <td className="px-4 py-3 text-gray-700">{log.status ?? "—"}</td>
                    <td className="px-4 py-3 text-gray-700">{log.traceId ?? "—"}</td>
                    <td className="px-4 py-3 text-gray-600 max-w-md break-words">
                      {log.description ?? "—"}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {!loading && totalPages > 1 ? (
        <div className="mt-6 flex items-center justify-between">
          <span className="text-sm text-gray-600">
            Page {page + 1} of {totalPages}
          </span>

          <div className="flex gap-2">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => handlePageChange(page - 1)}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Previous
            </button>
            <button
              type="button"
              disabled={page >= totalPages - 1}
              onClick={() => handlePageChange(page + 1)}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
};

export default AdminAuditLogsPage;
