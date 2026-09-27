import { useEffect, useState } from "react";
import hero from "../../assets/images/hero.png";
import PropertyCard from "../../common/components/PropertyCard";
import { getFeaturedPropertiesApi, type PropertySummaryDto } from "../../api/propertyApi";
import { ArrowRight, KeyRound, ShieldCheck, Sparkles } from "lucide-react";

const HomePage = () => {
  const [featured, setFeatured] = useState<PropertySummaryDto[]>([]);
  const [featuredLoading, setFeaturedLoading] = useState(true);
  const [featuredError, setFeaturedError] = useState<string | null>(null);

  useEffect(() => {
    getFeaturedPropertiesApi()
      .then((response) => setFeatured(response.content))
      .catch((reason: unknown) => {
        setFeaturedError(reason instanceof Error ? reason.message : "Unable to load featured properties.");
      })
      .finally(() => setFeaturedLoading(false));
  }, []);

  return (
    <div className="overflow-hidden">
      <section className="relative bg-slate-950 text-white">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_78%_10%,rgba(191,151,76,0.27),transparent_32rem)]" />
        <div className="relative mx-auto flex max-w-7xl flex-col items-center gap-12 px-5 py-20 sm:px-8 md:flex-row md:py-28">
          <div className="md:w-[55%]">
            <p className="mb-5 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.25em] text-amber-300"><Sparkles size={15} /> Curated living, elevated</p>
            <h1 className="max-w-3xl text-5xl font-bold leading-[1.05] tracking-[-0.04em] sm:text-6xl">A better address for your next chapter.</h1>
            <p className="mt-6 max-w-xl text-lg leading-8 text-slate-300">Discover remarkable homes, refined spaces, and confident property decisions with a marketplace designed around how you actually live.</p>
            <div className="mt-9 flex flex-wrap gap-3">
              <a href="/search" className="flex items-center gap-2 rounded-xl bg-amber-400 px-5 py-3.5 text-sm font-bold text-slate-950 shadow-xl shadow-amber-500/20 hover:-translate-y-0.5 hover:bg-amber-300">Explore properties <ArrowRight size={17} /></a>
              <a href="/register" className="rounded-xl border border-white/20 px-5 py-3.5 text-sm font-semibold text-white hover:bg-white/10">List your property</a>
            </div>
            <div className="mt-12 flex gap-10 border-t border-white/10 pt-6">
              <div><p className="text-2xl font-bold">2.4k+</p><p className="mt-1 text-xs text-slate-400">Premium listings</p></div>
              <div><p className="text-2xl font-bold">98%</p><p className="mt-1 text-xs text-slate-400">Client satisfaction</p></div>
              <div><p className="text-2xl font-bold">24/7</p><p className="mt-1 text-xs text-slate-400">Expert support</p></div>
            </div>
          </div>

          <div className="relative md:w-[45%]">
            <div className="absolute -inset-4 rounded-[2rem] border border-amber-300/20" />
            <img src={hero} alt="Featured home" className="relative mx-auto w-full max-w-md rounded-[1.75rem] object-cover shadow-2xl shadow-black/40" />
            <div className="absolute -bottom-5 -left-3 flex items-center gap-3 rounded-2xl border border-white/10 bg-white/10 px-4 py-3 backdrop-blur-xl">
              <div className="rounded-xl bg-amber-400 p-2 text-slate-950"><ShieldCheck size={18} /></div>
              <div><p className="text-xs font-bold">Verified spaces</p><p className="text-[11px] text-slate-300">Trusted by design</p></div>
            </div>
          </div>
        </div>
      </section>

      <div className="mx-auto max-w-7xl px-5 py-20 sm:px-8">
        <div className="mb-8 flex items-end justify-between">
          <div><p className="text-xs font-bold uppercase tracking-[0.2em] text-amber-700">The collection</p><h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">Featured properties</h2></div>
          <a href="/listings" className="hidden items-center gap-2 text-sm font-bold text-slate-700 hover:text-amber-700 sm:flex">View all <ArrowRight size={16} /></a>
        </div>
        <div className="grid grid-cols-1 gap-7 sm:grid-cols-2 lg:grid-cols-3">
          {featuredLoading && <p className="text-sm text-slate-500">Loading featured properties...</p>}
          {!featuredLoading && featuredError && <p className="text-sm text-rose-600">{featuredError}</p>}
          {!featuredLoading && !featuredError && featured.length === 0 && (
            <p className="text-sm text-slate-500">No featured properties are available right now.</p>
          )}
          {!featuredLoading &&
            !featuredError &&
            featured.map((property) => (
              <PropertyCard
                key={property.id}
                property={{
                  id: property.id,
                  title: property.title,
                  price: property.price,
                  location: [property.areaName, property.city].filter(Boolean).join(", "),
                  image: property.coverImageUrl,
                  type: property.propertyType,
                  bedrooms: property.bedrooms,
                }}
              />
            ))}
        </div>

        <div className="mt-20 grid grid-cols-1 gap-5 md:grid-cols-3">
          {[
            { icon: KeyRound, title: "Buy with clarity", text: "Shortlist exceptional homes with details you can trust." },
            { icon: Sparkles, title: "Rent beautifully", text: "Find a space that fits your rhythm, taste, and future." },
            { icon: ShieldCheck, title: "Sell confidently", text: "Put your property in front of serious, qualified buyers." },
          ].map(({ icon: Icon, title, text }) => (
          <div key={title} className="rounded-2xl border border-slate-200 bg-white p-7 shadow-[0_10px_35px_rgba(20,32,51,0.05)]">
            <div className="mb-5 flex h-11 w-11 items-center justify-center rounded-xl bg-amber-50 text-amber-700"><Icon size={21} /></div>
            <h3 className="text-lg font-bold text-slate-900">{title}</h3>
            <p className="mt-2 text-sm leading-6 text-slate-500">{text}</p>
          </div>
          ))}
        </div>

        <section className="mt-20 rounded-[2rem] bg-amber-100/60 p-8 sm:p-12">
          <div className="max-w-2xl"><p className="text-xs font-bold uppercase tracking-[0.2em] text-amber-800">Your next move</p><h2 className="mt-3 text-3xl font-bold tracking-tight text-slate-950">Make room for what matters.</h2><p className="mt-3 leading-7 text-slate-600">From the first search to the final key, Aurelia makes every property moment feel considered.</p><a href="/search" className="mt-6 inline-flex items-center gap-2 rounded-xl bg-slate-950 px-5 py-3 text-sm font-bold text-white hover:bg-slate-800">Start exploring <ArrowRight size={16} /></a></div>
        </section>
      </div>
    </div>
  );
};

export default HomePage;