import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getAuditLogByIdApi } from "../../api/auditApi";
import type { AuditLogResponse } from "../../types/api";

const AuditLogDetailsPage = () => {
  const { id } = useParams();
  const [log, setLog] = useState<AuditLogResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    if (!id) { setError("An audit log ID is required."); return; }
    getAuditLogByIdApi(id).then(setLog).catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load audit log."));
  }, [id]);
  return <div className="max-w-4xl p-8"><div className="mb-6 flex justify-between"><div><h2 className="text-2xl font-bold text-slate-900">Audit log details</h2><p className="mt-1 text-slate-600">Inspect a single audit event.</p></div><Link to="/admin/audit-logs" className="text-sm font-medium text-indigo-600 hover:underline">Back to logs</Link></div>{!log && !error && <p className="text-slate-500">Loading audit log...</p>}{error && <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>}{log && <dl className="grid gap-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm sm:grid-cols-2">{Object.entries(log).map(([key, value]) => <div key={key} className="rounded-lg bg-slate-50 p-3"><dt className="text-xs font-semibold uppercase tracking-wide text-slate-500">{key}</dt><dd className="mt-1 break-words text-sm text-slate-900">{typeof value === "object" ? JSON.stringify(value) : String(value ?? "—")}</dd></div>)}</dl>}</div>;
};

export default AuditLogDetailsPage;
