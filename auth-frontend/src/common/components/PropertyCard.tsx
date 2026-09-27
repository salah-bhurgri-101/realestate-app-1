import { Link } from "react-router-dom";
import { ArrowUpRight, BedDouble, MapPin } from "lucide-react";

interface Property {
  id: string;
  title: string;
  price: number;
  location: string;
  image?: string;
  type?: string;
  bedrooms?: number;
}

const PropertyCard = ({ property }: { property: Property }) => {
  return (
    <div className="group overflow-hidden rounded-2xl border border-slate-200/80 bg-white shadow-[0_10px_35px_rgba(20,32,51,0.07)] transition duration-300 hover:-translate-y-1 hover:shadow-[0_18px_50px_rgba(20,32,51,0.14)]">
      <div className="relative flex h-56 items-center justify-center overflow-hidden bg-slate-100">
        {property.image ? (
          <img src={property.image} alt={property.title} className="h-full w-full object-cover transition duration-500 group-hover:scale-105" />
        ) : (
          <div className="text-sm text-slate-400">No image available</div>
        )}
        <span className="absolute left-4 top-4 rounded-full bg-white/90 px-3 py-1.5 text-[11px] font-bold uppercase tracking-widest text-slate-700 backdrop-blur">Featured</span>
        <span className="absolute bottom-4 left-4 rounded-full bg-slate-950/80 px-3 py-1.5 text-xs font-medium text-white backdrop-blur">{property.type ?? "Residence"}</span>
      </div>

      <div className="p-5">
        <h3 className="text-lg font-bold tracking-tight text-slate-900">{property.title}</h3>
        <p className="mt-1 flex items-center gap-1.5 text-sm text-slate-500"><MapPin size={14} className="text-amber-600" />{property.location}</p>

        <div className="mt-5 flex items-end justify-between">
          <div>
            <div className="text-xl font-extrabold tracking-tight text-slate-900">${property.price.toLocaleString()}</div>
            <div className="mt-1 flex items-center gap-1.5 text-xs text-slate-500"><BedDouble size={14} />{property.bedrooms ?? "-"} bedrooms</div>
          </div>

          <Link to={`/property/${property.id}`} className="flex items-center gap-1 rounded-lg bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-800 hover:bg-amber-100">
            Explore <ArrowUpRight size={15} />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default PropertyCard;
