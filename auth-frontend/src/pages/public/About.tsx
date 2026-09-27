const About = () => {
  return (
    <div className="mx-auto max-w-6xl px-5 py-16 sm:px-8">
      <section className="rounded-[2rem] bg-slate-950 p-8 text-white shadow-2xl sm:p-14">
        <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-300">The Aurelia story</p>
        <h1 className="mt-4 max-w-3xl text-5xl font-bold tracking-tight">Property decisions, made beautifully simple.</h1>
        <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">Aurelia is a considered property experience for people who expect more from the places they live, invest, and build their future in.</p>
      </section>
      <div className="mt-8 grid gap-5 md:grid-cols-3">
        {[["01", "A sharper search", "Thoughtful tools help you move from inspiration to a confident shortlist."], ["02", "A trusted network", "Every interaction is designed to bring clarity to buyers and sellers alike."], ["03", "A higher standard", "We pair premium presentation with practical property intelligence."]].map(([number, title, text]) => <article key={number} className="rounded-2xl border border-slate-200 bg-white p-7 shadow-[0_10px_35px_rgba(20,32,51,0.05)]"><span className="text-sm font-bold text-amber-700">{number}</span><h2 className="mt-5 text-xl font-bold text-slate-950">{title}</h2><p className="mt-3 text-sm leading-6 text-slate-500">{text}</p></article>)}
      </div>
    </div>
  );
};

export default About;
