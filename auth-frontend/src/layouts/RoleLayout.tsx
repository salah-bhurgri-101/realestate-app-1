import { NavLink, Navigate, Outlet, useNavigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuthStore } from "../features/auth/store/authStore";
import { Bell, Building2, ChevronRight, LogOut } from "lucide-react";

interface NavItem {
  label: string;
  to: string;
}

interface RoleLayoutProps {
  title: string;
  subtitle: string;
  navItems: NavItem[];
  actions?: ReactNode;
}

const RoleLayout = ({ title, subtitle, navItems, actions }: RoleLayoutProps) => {
  const navigate = useNavigate();
  const { user, clearUser, clearTokens } = useAuthStore();
  const role = user?.role?.toLowerCase()?.replace(/^role_/, "") ?? "";

  if (!user && !localStorage.getItem("accessToken")) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-[#f7f8fa]">
      <div className="flex min-h-screen">
        <aside className="hidden w-72 shrink-0 border-r border-white/10 bg-[#111827] p-6 text-slate-100 md:flex md:flex-col">
          <div className="mb-10">
            <div className="flex items-center gap-2.5">
              <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-400 text-slate-950">
                <Building2 size={18} strokeWidth={2.5} />
              </span>
              <span className="text-xl font-bold tracking-tight text-white">Aurelia<span className="text-amber-300">.</span></span>
            </div>
            <p className="mt-8 text-[10px] font-bold uppercase tracking-[0.25em] text-amber-300">Workspace</p>
            <h1 className="mt-2 text-2xl font-bold text-white">{title}</h1>
            <p className="mt-1 text-sm text-slate-400">{subtitle}</p>
          </div>

          <nav className="space-y-1.5">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "." || item.to === ""}
                className={({ isActive }) =>
                  `block rounded-xl px-4 py-3 text-sm font-medium transition ${
                    isActive
                      ? "bg-amber-400 font-semibold text-slate-950 shadow-lg shadow-amber-500/15"
                      : "text-slate-400 hover:bg-white/10 hover:text-white"
                  }`
                }
              >
                <span className="flex items-center justify-between">{item.label}<ChevronRight size={15} className="opacity-50" /></span>
              </NavLink>
            ))}
          </nav>

          <div className="mt-auto rounded-2xl border border-white/10 bg-white/5 p-4">
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Signed in</p>
            <p className="mt-2 text-lg font-semibold text-white">{user?.name || "User"}</p>
            <p className="text-sm capitalize text-amber-300">{role || "member"}</p>
          </div>
        </aside>

        <div className="flex-1">
          <header className="border-b border-slate-200/80 bg-white/85 backdrop-blur-xl">
            <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
              <div>
                <p className="text-[10px] font-bold uppercase tracking-[0.25em] text-amber-700">Aurelia / {title}</p>
                <h2 className="mt-1 text-xl font-bold tracking-tight text-slate-900">{subtitle}</h2>
              </div>

              <div className="flex items-center gap-3">
                {actions}
                <button type="button" aria-label="Notifications" className="hidden rounded-xl border border-slate-200 p-2.5 text-slate-500 hover:bg-slate-50 sm:block"><Bell size={17} /></button>
                <button
                  type="button"
                  onClick={() => {
                    clearTokens();
                    clearUser();
                    navigate("/login");
                  }}
                  className="flex items-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-600 transition hover:border-red-200 hover:text-red-600"
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </div>
            </div>
          </header>

          <main className="mx-auto max-w-7xl p-4 sm:p-6 lg:p-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
};

export default RoleLayout;
