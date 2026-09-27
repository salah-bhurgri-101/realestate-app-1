const SellerDashboardPage = () => {
  return (
    <div className="space-y-8">
      <div className="rounded-[2rem] bg-slate-950 p-8 text-white sm:p-10"><p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Seller studio</p><h2 className="mt-3 text-4xl font-bold tracking-tight">Turn your spaces into stories.</h2><p className="mt-3 max-w-xl text-slate-300">Manage your portfolio, understand demand, and present every listing at its very best.</p><a href="/seller/add-property" className="mt-7 inline-block rounded-xl bg-amber-400 px-5 py-3 text-sm font-bold text-slate-950 hover:bg-amber-300">Create a listing</a></div>
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">{[["18", "Live listings"], ["4,892", "Total views"], ["7", "Active inquiries"], ["$284k", "Portfolio value"]].map(([value, label]) => <div key={label} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)]"><p className="text-3xl font-bold tracking-tight text-slate-950">{value}</p><p className="mt-2 text-sm text-slate-500">{label}</p><p className="mt-4 text-xs font-bold text-emerald-600">+12.4% this month</p></div>)}</div>
      <div className="grid gap-5 lg:grid-cols-2"><div className="rounded-2xl border border-slate-200 bg-white p-6"><h3 className="font-bold text-slate-900">Listing performance</h3><div className="mt-8 flex h-36 items-end gap-3">{[35, 48, 42, 64, 55, 72, 60, 84, 74, 90].map((height, index) => <div key={index} className="flex-1 rounded-t-lg bg-gradient-to-t from-slate-900 to-slate-500" style={{ height: `${height}%` }} />)}</div></div><div className="rounded-2xl border border-slate-200 bg-white p-6"><h3 className="font-bold text-slate-900">Next best actions</h3><div className="mt-4 space-y-3">{["Add cover photos to 2 listings", "Reply to 3 new inquiries", "Review your seller profile"].map((item) => <div key={item} className="rounded-xl bg-amber-50 p-4 text-sm font-medium text-slate-700">{item}</div>)}</div></div></div>
    </div>
  );
};

export default SellerDashboardPage;
