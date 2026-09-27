const AdminDashboardPage = () => {
  return (
    <div className="space-y-8">
      <div className="rounded-[2rem] bg-slate-950 p-8 text-white shadow-2xl shadow-slate-900/10 sm:p-10">
        <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Executive overview</p>
        <h2 className="mt-3 text-3xl font-bold tracking-tight">Good morning, administrator.</h2>
        <p className="mt-3 max-w-2xl text-slate-300">A calm, high-level view of platform health, inventory, and the people shaping the Aurelia marketplace.</p>
      </div>
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {[["12,480", "Registered users", "+8.4%"], ["1,284", "Active listings", "+12.1%"], ["384", "New sellers", "+5.7%"], ["98.7%", "Platform health", "Excellent"]].map(([value, label, trend]) => (
          <div key={label} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[0_10px_35px_rgba(20,32,51,0.05)]"><p className="text-3xl font-bold tracking-tight text-slate-950">{value}</p><p className="mt-2 text-sm text-slate-500">{label}</p><p className="mt-4 text-xs font-bold text-emerald-600">{trend}</p></div>
        ))}
      </div>
      <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
        <div className="rounded-2xl border border-slate-200 bg-white p-6"><div className="flex justify-between"><h3 className="font-bold text-slate-900">Marketplace activity</h3><span className="text-xs font-semibold text-slate-400">Last 30 days</span></div><div className="mt-8 flex h-40 items-end gap-3">{[42, 58, 48, 76, 62, 88, 72, 96, 80, 92, 78, 100].map((height, index) => <div key={index} className="flex-1 rounded-t-lg bg-gradient-to-t from-amber-500 to-amber-200" style={{ height: `${height}%` }} />)}</div><div className="mt-3 flex justify-between text-xs text-slate-400"><span>Jun 01</span><span>Jun 15</span><span>Jun 30</span></div></div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6"><h3 className="font-bold text-slate-900">Priority queue</h3><div className="mt-5 space-y-4">{["12 listings awaiting review", "8 seller verifications", "3 support escalations"].map((item, index) => <div key={item} className="flex items-center justify-between rounded-xl bg-slate-50 p-4"><span className="text-sm text-slate-600">{item}</span><span className={`h-2.5 w-2.5 rounded-full ${index === 2 ? "bg-red-400" : "bg-amber-400"}`} /></div>)}</div></div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
