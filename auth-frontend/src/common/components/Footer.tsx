const Footer = () => {
  return (
    <footer className="mt-16 bg-slate-950 text-white">
      <div className="mx-auto max-w-7xl px-5 py-14 sm:px-8">
        <div className="grid gap-10 md:grid-cols-[1.5fr_1fr_1fr_1fr]">
          <div><h3 className="text-2xl font-bold tracking-tight">Aurelia<span className="text-amber-300">.</span></h3><p className="mt-4 max-w-xs text-sm leading-6 text-slate-400">A more considered way to discover, market, and move into your next address.</p></div>
          <div><h4 className="text-xs font-bold uppercase tracking-[0.2em] text-amber-300">Explore</h4><div className="mt-4 space-y-3 text-sm text-slate-400"><p>Listings</p><p>Search properties</p><p>About Aurelia</p></div></div>
          <div><h4 className="text-xs font-bold uppercase tracking-[0.2em] text-amber-300">Company</h4><div className="mt-4 space-y-3 text-sm text-slate-400"><p>Our story</p><p>Journal</p><p>Careers</p></div></div>
          <div><h4 className="text-xs font-bold uppercase tracking-[0.2em] text-amber-300">Connect</h4><div className="mt-4 space-y-3 text-sm text-slate-400"><p>concierge@aurelia.example</p><p>+1 555 1234</p><p>Instagram · LinkedIn</p></div></div>
        </div>
        <div className="mt-12 flex flex-col justify-between gap-3 border-t border-white/10 pt-6 text-xs text-slate-500 sm:flex-row"><span>© {new Date().getFullYear()} Aurelia. All rights reserved.</span><span>Privacy · Terms · Accessibility</span></div>
      </div>
    </footer>
  );
};

export default Footer;
