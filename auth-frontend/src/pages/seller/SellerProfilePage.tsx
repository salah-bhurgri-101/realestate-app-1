import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getMeApi, logoutAllApi } from "../../api/authApi";
import { useAuthStore } from "../../features/auth/store/authStore";
import type { MeResponse, UserResponse } from "../../features/auth/types/auth.types";
import { getUserProfileApi, type UserProfile } from "../../api/userApi";

const SellerProfilePage = () => {
  const navigate = useNavigate();
  const storedUser = useAuthStore((state) => state.user);
  const { clearTokens, clearUser } = useAuthStore();
  const [user, setUser] = useState<UserResponse | null>(storedUser);
  const [sessions, setSessions] = useState<MeResponse["activeSessions"]>([]);
  const [extendedProfile, setExtendedProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoggingOutAll, setIsLoggingOutAll] = useState(false);

  const handleLogoutAll = async () => {
    try {
      setIsLoggingOutAll(true);
      await logoutAllApi();
      clearTokens();
      clearUser();
      navigate("/login", { replace: true });
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to log out of all devices.");
      setIsLoggingOutAll(false);
    }
  };

  useEffect(() => {
    getMeApi()
      .then(async (response) => {
        const data = response?.data || response;
        setUser(data.user);
        setSessions(data.activeSessions ?? []);
        if (data.user?.id) {
          setExtendedProfile(await getUserProfileApi(data.user.id).catch(() => null));
        }
      })
      .catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load your profile."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-sm text-slate-500">Loading your seller profile...</p>;

  return (
    <div className="space-y-8">
      <div className="bg-slate-950 p-8 text-white sm:p-10">
        <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Seller identity</p>
        <h2 className="mt-3 text-4xl font-bold tracking-tight">Present your business with confidence.</h2>
        <p className="mt-3 max-w-xl text-slate-300">Review your account details and keep your seller security current.</p>
      </div>
      {error && <p className="bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}
      {user && (
        <>
          <section className="border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)] sm:p-8">
            <div className="flex flex-col gap-5 border-b border-slate-200 pb-6 sm:flex-row sm:items-center">
              <div className="flex h-16 w-16 items-center justify-center bg-amber-100 text-2xl font-bold text-amber-800">{user.name.charAt(0).toUpperCase()}</div>
              <div><h3 className="text-2xl font-bold text-slate-950">{user.name}</h3><p className="mt-1 text-sm text-slate-500">{user.email}</p></div>
              <span className={`w-fit px-3 py-2 text-xs font-bold sm:ml-auto ${user.isVerified ? "bg-emerald-100 text-emerald-700" : "bg-amber-100 text-amber-700"}`}>{user.isVerified ? "Verified account" : "Verification pending"}</span>
            </div>
            <div className="grid gap-6 pt-6 sm:grid-cols-2">
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Full name</p><p className="mt-2 font-semibold text-slate-900">{user.name}</p></div>
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Email address</p><p className="mt-2 font-semibold text-slate-900">{user.email}</p></div>
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Phone number</p><p className="mt-2 font-semibold text-slate-900">{user.phone || "Not added"}</p></div>
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Bio</p><p className="mt-2 font-semibold text-slate-900">{extendedProfile?.bio || "Not added"}</p></div>
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Seller since</p><p className="mt-2 font-semibold text-slate-900">{new Date(user.createdAt).toLocaleDateString()}</p></div>
            </div>
          </section>
          <section className="flex flex-col justify-between gap-5 border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)] sm:flex-row sm:items-center sm:p-8">
            <div><h3 className="text-lg font-bold text-slate-950">Account security</h3><p className="mt-1 text-sm text-slate-500">Use a strong password to protect your listings.</p></div>
            <Link to="/change-password" className="w-fit bg-slate-950 px-4 py-3 text-sm font-semibold text-white">Change password</Link>
          </section>
          <section className="flex flex-col justify-between gap-5 border border-rose-200 bg-rose-50 p-6 sm:flex-row sm:items-center sm:p-8">
            <div><h3 className="text-lg font-bold text-slate-950">Sign out everywhere</h3><p className="mt-1 text-sm text-slate-600">End all active sessions, including this device.</p></div>
            <button type="button" onClick={() => void handleLogoutAll()} disabled={isLoggingOutAll} className="w-fit bg-rose-600 px-4 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60">
              {isLoggingOutAll ? "Signing out..." : "Logout all devices"}
            </button>
          </section>
          {sessions.length > 0 && <section className="border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)] sm:p-8"><h3 className="text-lg font-bold text-slate-950">Active sessions</h3><div className="mt-5 grid gap-3 sm:grid-cols-2">{sessions.map((session) => <div key={session.id} className="border border-slate-200 p-4"><p className="font-semibold text-slate-900">{session.deviceName}</p><p className="mt-1 text-sm text-slate-500">{session.deviceType} · {session.ipAddress}</p><p className="mt-2 text-xs text-slate-400">Last active {new Date(session.lastActiveAt).toLocaleDateString()}</p></div>)}</div></section>}
        </>
      )}
    </div>
  );
};

export default SellerProfilePage;
