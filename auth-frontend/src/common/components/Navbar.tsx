import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/store/authStore";
import { Building2, Menu, Search, X } from "lucide-react";
import { useState } from "react";

const Navbar = () => {
  const navigate = useNavigate();
  const { user, clearUser, clearTokens } = useAuthStore();

  const handleLogout = () => {
    clearTokens();
    clearUser();
    navigate("/");
  };

  const role = user?.role?.toLowerCase();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/90 shadow-[0_8px_30px_rgba(20,32,51,0.06)] backdrop-blur-xl">
      <div className="mx-auto max-w-7xl px-5 sm:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-10">
            <Link to="/" className="group flex items-center gap-2.5 text-xl font-bold tracking-tight text-slate-900">
              <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-900 text-amber-300 shadow-lg shadow-slate-900/20">
                <Building2 size={18} strokeWidth={2.5} />
              </span>
              Aurelia<span className="text-amber-600">.</span>
            </Link>

            <div className="hidden items-center gap-7 md:flex">
              <Link to="/listings" className="text-sm font-medium text-slate-600 hover:text-slate-950">
                Listings
              </Link>
              <Link to="/search" className="flex items-center gap-1 text-sm font-medium text-slate-600 hover:text-slate-950">
                <Search size={15} />
                Search
              </Link>
              <Link to="/about" className="text-sm font-medium text-slate-600 hover:text-slate-950">
                About
              </Link>
              <Link to="/contact" className="text-sm font-medium text-slate-600 hover:text-slate-950">
                Contact
              </Link>
            </div>
          </div>

          <div className="flex items-center gap-4">
            {user ? (
              <div className="flex items-center gap-3">
                <span className="hidden text-sm text-slate-600 sm:inline">Hi, {user.name}</span>
                {role === "role_admin" && (
                  <Link to="/admin" className="rounded-lg bg-slate-900 px-3.5 py-2 text-sm font-semibold text-white shadow-lg shadow-slate-900/15">Admin</Link>
                )}
                {(role === "role_seller" || role === "role_moderator") && (
                  <Link to="/seller" className="rounded-lg bg-slate-900 px-3.5 py-2 text-sm font-semibold text-white shadow-lg shadow-slate-900/15">Seller</Link>
                )}
                {(role !== "role_admin" && role !== "role_seller" && role !== "role_moderator") && (
                  <Link to="/buyer" className="rounded-lg bg-slate-900 px-3.5 py-2 text-sm font-semibold text-white shadow-lg shadow-slate-900/15">Buyer</Link>
                )}

                <button onClick={handleLogout} className="text-sm font-medium text-slate-500 hover:text-red-600">
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-3">
                <Link to="/login" className="hidden text-sm font-semibold text-slate-600 hover:text-slate-950 sm:block">Sign in</Link>
                <Link to="/register" className="rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-slate-900/15 hover:-translate-y-0.5 hover:bg-slate-800">Get started</Link>
              </div>
            )}
            <button aria-label="Toggle menu" onClick={() => setMenuOpen((open) => !open)} className="rounded-lg p-2 text-slate-600 md:hidden">
              {menuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>
        {menuOpen && (
          <div className="grid gap-1 border-t border-slate-100 py-3 md:hidden">
            {["/listings", "/search", "/about", "/contact"].map((path) => (
              <Link key={path} to={path} onClick={() => setMenuOpen(false)} className="rounded-lg px-3 py-2.5 text-sm font-medium capitalize text-slate-600 hover:bg-slate-50">
                {path.slice(1)}
              </Link>
            ))}
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
