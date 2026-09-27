import { useEffect, useState } from "react";
import { getAdminUsersApi, updateAdminUserRoleApi, updateAdminUserStatusApi } from "../../api/authApi";
import type { UserResponse } from "../../features/auth/types/auth.types";

const AdminUsersPage = () => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);
  const load = async () => { try { setLoading(true); setUsers(await getAdminUsersApi("ROLE_USER")); } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to load users."); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, []);
  const toggleStatus = async (user: UserResponse) => { try { setBusyId(user.id); const updated = await updateAdminUserStatusApi(user.id, !user.isActive); setUsers((items) => items.map((item) => item.id === updated.id ? updated : item)); } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to update user."); } finally { setBusyId(null); } };
  const promote = async (user: UserResponse) => { try { setBusyId(user.id); await updateAdminUserRoleApi(user.id, "ROLE_MODERATOR"); setUsers((items) => items.filter((item) => item.id !== user.id)); } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to update role."); } finally { setBusyId(null); } };
  return <div className="max-w-7xl mx-auto p-8"><div className="mb-8"><h2 className="text-3xl font-bold text-slate-900">Manage Users</h2><p className="mt-2 text-slate-500">Review regular accounts and control access.</p></div>{error && <p className="mb-4 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}<div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">{loading ? <p className="p-6 text-sm text-slate-500">Loading users...</p> : users.length === 0 ? <p className="p-6 text-sm text-slate-500">No regular users found.</p> : <div className="divide-y divide-slate-200">{users.map((user) => <div key={user.id} className="flex flex-col gap-4 p-5 md:flex-row md:items-center md:justify-between"><div><p className="font-semibold text-slate-900">{user.name}</p><p className="text-sm text-slate-500">{user.email} · {user.phone || "No phone"}</p></div><div className="flex flex-wrap items-center gap-2"><span className={`rounded-full px-3 py-1 text-xs font-semibold ${user.isActive ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-700"}`}>{user.isActive ? "Active" : "Inactive"}</span><button disabled={busyId === user.id} onClick={() => void toggleStatus(user)} className="rounded-md border px-3 py-2 text-sm font-medium">{user.isActive ? "Deactivate" : "Activate"}</button><button disabled={busyId === user.id} onClick={() => void promote(user)} className="rounded-md bg-slate-950 px-3 py-2 text-sm font-medium text-white">Make seller</button></div></div>)}</div>}</div></div>;
};
export default AdminUsersPage;
