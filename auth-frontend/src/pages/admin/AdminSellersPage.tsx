import { useEffect, useState } from "react";
import { getAdminUsersApi, updateAdminUserRoleApi, updateAdminUserStatusApi } from "../../api/authApi";
import type { UserResponse } from "../../features/auth/types/auth.types";

const AdminSellersPage = () => {
  const [sellers, setSellers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const load = async () => { try { setSellers(await getAdminUsersApi("ROLE_MODERATOR")); } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to load sellers."); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, []);
  const update = async (seller: UserResponse, action: "status" | "role") => { try { const updated = action === "status" ? await updateAdminUserStatusApi(seller.id, !seller.isActive) : await updateAdminUserRoleApi(seller.id, "ROLE_USER"); setSellers((items) => action === "role" ? items.filter((item) => item.id !== seller.id) : items.map((item) => item.id === updated.id ? updated : item)); } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to update seller."); } };
  return <div className="max-w-7xl mx-auto p-8"><div className="mb-8"><h2 className="text-3xl font-bold text-slate-900">Manage Sellers</h2><p className="mt-2 text-slate-500">Approve, suspend, or remove seller access.</p></div>{error && <p className="mb-4 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}<div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">{loading ? <p className="p-6 text-sm text-slate-500">Loading sellers...</p> : sellers.length === 0 ? <p className="p-6 text-sm text-slate-500">No sellers found.</p> : <div className="divide-y divide-slate-200">{sellers.map((seller) => <div key={seller.id} className="flex flex-col gap-4 p-5 md:flex-row md:items-center md:justify-between"><div><p className="font-semibold text-slate-900">{seller.name}</p><p className="text-sm text-slate-500">{seller.email} · Joined {new Date(seller.createdAt).toLocaleDateString()}</p></div><div className="flex flex-wrap items-center gap-2"><span className={`rounded-full px-3 py-1 text-xs font-semibold ${seller.isActive ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-700"}`}>{seller.isActive ? "Active" : "Suspended"}</span><button onClick={() => void update(seller, "status")} className="rounded-md border px-3 py-2 text-sm font-medium">{seller.isActive ? "Suspend" : "Activate"}</button><button onClick={() => void update(seller, "role")} className="rounded-md bg-slate-950 px-3 py-2 text-sm font-medium text-white">Remove seller</button></div></div>)}</div>}</div></div>;
};
export default AdminSellersPage;
