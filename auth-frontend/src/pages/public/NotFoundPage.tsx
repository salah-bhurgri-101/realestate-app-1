import { ArrowLeft, Compass, Home } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";

const NotFoundPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <section className="relative flex min-h-[calc(100vh-4rem)] items-center overflow-hidden bg-slate-950 px-5 py-20 text-white sm:px-8">
      <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-amber-400/15 blur-3xl" />
      <div className="absolute -bottom-32 -left-24 h-96 w-96 rounded-full bg-indigo-500/15 blur-3xl" />

      <div className="relative mx-auto w-full max-w-5xl">
        <div className="grid items-center gap-12 lg:grid-cols-[1fr_0.8fr]">
          <div>
            <div className="mb-8 flex h-14 w-14 items-center justify-center bg-amber-300 text-slate-950">
              <Compass size={28} strokeWidth={1.8} />
            </div>
            <p className="text-sm font-bold uppercase tracking-[0.3em] text-amber-300">Aurelia / 404</p>
            <h1 className="mt-5 max-w-2xl text-5xl font-bold tracking-tight sm:text-7xl">
              This address has moved.
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-8 text-slate-300">
              We could not find the page you requested. Let us guide you back to the places worth discovering.
            </p>
            <p className="mt-4 max-w-xl break-all border-l border-amber-300/50 pl-4 text-sm text-slate-500">
              {location.pathname}
            </p>
            <div className="mt-9 flex flex-wrap gap-3">
              <Link to="/" className="inline-flex items-center gap-2 bg-amber-300 px-5 py-3 text-sm font-bold text-slate-950 transition hover:bg-amber-200">
                <Home size={17} />
                Return home
              </Link>
              <button
                type="button"
                onClick={() => navigate(-1)}
                className="inline-flex items-center gap-2 border border-white/20 px-5 py-3 text-sm font-bold text-white transition hover:border-amber-300 hover:text-amber-300"
              >
                <ArrowLeft size={17} />
                Go back
              </button>
            </div>
          </div>

          <div className="hidden lg:block">
            <div className="border border-white/10 bg-white/[0.04] p-8">
              <p className="text-8xl font-bold tracking-tighter text-white/10">404</p>
              <div className="mt-12 border-t border-white/10 pt-6">
                <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Continue exploring</p>
                <p className="mt-3 text-2xl font-semibold">Find a better address.</p>
                <Link to="/search" className="mt-6 inline-flex text-sm font-semibold text-slate-300 underline decoration-amber-300 underline-offset-4 hover:text-white">
                  Browse properties
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default NotFoundPage;
