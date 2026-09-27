const BuyerDashboardPage = () => {
  return (
    <div className="space-y-8">
      <div className="rounded-[2rem] bg-gradient-to-br from-amber-100 via-white to-white p-8 sm:p-10"><p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-700">Your private collection</p><h2 className="mt-3 text-4xl font-bold tracking-tight text-slate-950">Find a place that feels like you.</h2><p className="mt-3 max-w-xl text-slate-600">Keep your favorite spaces close, compare your shortlist, and make your next move with confidence.</p><a href="/search" className="mt-7 inline-block rounded-xl bg-slate-950 px-5 py-3 text-sm font-bold text-white shadow-lg shadow-slate-900/15">Explore homes</a></div>
      <div className="grid gap-5 sm:grid-cols-3">{[["24", "Saved properties"], ["6", "New matches"], ["3", "Open conversations"]].map(([value, label]) => <div key={label} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)]"><p className="text-3xl font-bold text-slate-950">{value}</p><p className="mt-2 text-sm text-slate-500">{label}</p></div>)}</div>
      <div className="rounded-2xl border border-slate-200 bg-white p-6"><div className="flex items-center justify-between"><h3 className="font-bold text-slate-900">Your journey</h3><span className="text-xs font-semibold text-amber-700">75% complete</span></div><div className="mt-5 h-2 rounded-full bg-slate-100"><div className="h-2 w-3/4 rounded-full bg-amber-400" /></div><p className="mt-4 text-sm text-slate-500">Complete your profile to receive more personal recommendations.</p></div>
    </div>
  );
};

export default BuyerDashboardPage;
