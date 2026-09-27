import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createAsyncAuditLogApi, createAuditLogApi } from "../../api/auditApi";
import type { AuditLogAction, AuditLogRequest } from "../../types/api";

const actions: AuditLogAction[] = ["CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "LOGIN_FAILED", "ACCESS_DENIED", "EXPORT", "IMPORT", "OTHER"];
const initialForm: AuditLogRequest = { serviceName: "", action: "CREATE", entityType: "", entityId: "", username: "", userId: "", ipAddress: "", description: "", metadata: {}, status: "SUCCESS", traceId: "" };
const fields: Array<[keyof AuditLogRequest, string]> = [["serviceName", "Service name"], ["entityType", "Entity type"], ["entityId", "Entity ID"], ["username", "Username"], ["userId", "User ID"], ["ipAddress", "IP address"], ["status", "Status"], ["traceId", "Trace ID"]];

const AuditLogCreatePage = ({ asyncMode = false }: { asyncMode?: boolean }) => {
  const navigate = useNavigate();
  const [form, setForm] = useState<AuditLogRequest>(initialForm);
  const [metadata, setMetadata] = useState("{}");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null);
  const update = (name: keyof AuditLogRequest, value: string) => setForm((current) => ({ ...current, [name]: value }));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setMessage(null);
    let parsedMetadata: Record<string, unknown>;
    try {
      const parsed: unknown = JSON.parse(metadata);
      if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") throw new Error("Metadata must be a JSON object.");
      parsedMetadata = parsed as Record<string, unknown>;
    } catch (error) {
      setMessage({ text: error instanceof Error ? error.message : "Metadata must be valid JSON.", error: true });
      return;
    }
    setLoading(true);
    try {
      const payload = { ...form, metadata: parsedMetadata };
      if (asyncMode) await createAsyncAuditLogApi(payload);
      else await createAuditLogApi(payload);
      setMessage({ text: asyncMode ? "Audit log queued successfully." : "Audit log created successfully.", error: false });
      if (!asyncMode) { setForm(initialForm); setMetadata("{}"); }
    } catch (error) {
      setMessage({ text: error instanceof Error ? error.message : "Unable to create audit log.", error: true });
    } finally { setLoading(false); }
  };

  return (
    <div className="max-w-4xl p-8">
      <div className="mb-6 flex items-start justify-between"><div><h2 className="text-2xl font-bold text-slate-900">{asyncMode ? "Create async audit log" : "Create audit log"}</h2><p className="mt-1 text-slate-600">{asyncMode ? "Queue an event for background processing." : "Record an event immediately."}</p></div><Link to="/admin/audit-logs" className="text-sm font-medium text-indigo-600 hover:underline">Back to logs</Link></div>
      <form onSubmit={submit} className="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="grid gap-4 sm:grid-cols-2">
          {fields.map(([name, label]) => <label key={name} className="text-sm font-medium text-slate-700">{label}<input required={name === "serviceName"} value={(form[name] as string) ?? ""} onChange={(event) => update(name, event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500" /></label>)}
          <label className="text-sm font-medium text-slate-700">Action<select value={form.action} onChange={(event) => update("action", event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal">{actions.map((action) => <option key={action}>{action}</option>)}</select></label>
        </div>
        <label className="block text-sm font-medium text-slate-700">Description<textarea value={form.description ?? ""} onChange={(event) => update("description", event.target.value)} rows={3} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500" /></label>
        <label className="block text-sm font-medium text-slate-700">Metadata (JSON)<textarea value={metadata} onChange={(event) => setMetadata(event.target.value)} rows={4} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-mono text-sm font-normal outline-none focus:ring-2 focus:ring-indigo-500" /></label>
        {message && <p className={`rounded-lg px-3 py-2 text-sm ${message.error ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"}`}>{message.text}</p>}
        <div className="flex justify-end gap-3"><button type="button" onClick={() => navigate("/admin/audit-logs")} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-700">Cancel</button><button disabled={loading} className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">{loading ? "Saving..." : asyncMode ? "Queue audit log" : "Create audit log"}</button></div>
      </form>
    </div>
  );
};

export default AuditLogCreatePage;
