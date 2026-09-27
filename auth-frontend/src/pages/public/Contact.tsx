const Contact = () => {
  return (
    <div className="mx-auto max-w-6xl px-5 py-16 sm:px-8">
      <div className="grid gap-8 lg:grid-cols-[1fr_0.8fr]">
        <section className="rounded-[2rem] bg-slate-950 p-8 text-white sm:p-12">
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Concierge support</p>
          <h1 className="mt-4 text-5xl font-bold tracking-tight">Let’s find the right way forward.</h1>
          <p className="mt-6 max-w-lg leading-8 text-slate-300">Whether you are exploring a new address or preparing to list one, our team is here to make the next step feel effortless.</p>
          <div className="mt-10 space-y-4 text-sm text-slate-300"><p><span className="font-semibold text-white">Email</span><br />concierge@aurelia.example</p><p><span className="font-semibold text-white">Phone</span><br />+1 555 1234</p><p><span className="font-semibold text-white">Hours</span><br />Mon–Fri · 9:00–18:00</p></div>
        </section>
        <form className="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-[0_12px_45px_rgba(20,32,51,0.08)] sm:p-10"><h2 className="text-2xl font-bold text-slate-950">Start a conversation</h2><p className="mt-2 text-sm text-slate-500">Tell us a little about what you need.</p><div className="mt-7 space-y-4"><input placeholder="Your name" className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-500" /><input type="email" placeholder="Email address" className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-500" /><textarea rows={5} placeholder="How can we help?" className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-amber-500" /><button type="button" className="w-full rounded-xl bg-slate-950 px-4 py-3.5 text-sm font-bold text-white hover:bg-slate-800">Send inquiry</button></div></form>
      </div>
    </div>
  );
};

export default Contact;
