import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PropertyCard from "../../common/components/PropertyCard";
import {
  searchPropertiesApi,
  type PageResponseDto,
  type PropertySummaryDto,
} from "../../api/propertyApi";

const Listings = () => {
  const [result, setResult] = useState<PageResponseDto<PropertySummaryDto> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadListings = async (pageNo = 0) => {
    setLoading(true);
    setError(null);
    try {
      const response = await searchPropertiesApi({
        status: "APPROVED",
        pageNo,
        pageSize: 12,
      });
      setResult(response);
      setPage(pageNo);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load approved properties.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadListings();
  }, []);

  return (
    <div className="mx-auto max-w-7xl px-5 py-14 sm:px-8">
      <div className="mb-10 flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-700">The Aurelia collection</p>
          <h1 className="mt-3 text-4xl font-bold tracking-tight text-slate-950">Approved properties</h1>
          <p className="mt-2 text-slate-500">Explore properties reviewed and approved for the marketplace.</p>
        </div>
        <Link to="/search" className="w-fit bg-slate-950 px-4 py-3 text-sm font-semibold text-white">Open advanced search</Link>
      </div>

      {error && <p className="mb-6 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}
      {loading && <p className="text-sm text-slate-500">Loading approved properties...</p>}

      {!loading && result?.content.length === 0 && (
        <div className="border border-slate-200 bg-white p-12 text-center">
          <h2 className="text-xl font-bold text-slate-900">No approved properties yet</h2>
          <p className="mt-2 text-sm text-slate-500">Please check back soon for new listings.</p>
        </div>
      )}

      {!loading && result && result.content.length > 0 && (
        <>
          <div className="grid grid-cols-1 gap-7 sm:grid-cols-2 lg:grid-cols-3">
            {result.content.map((property) => (
              <PropertyCard
                key={property.id}
                property={{
                  id: property.id,
                  title: property.title,
                  price: property.price,
                  location: property.areaName ? `${property.areaName}, ${property.city}` : property.city,
                  image: property.coverImageUrl,
                  type: property.propertyType,
                  bedrooms: property.bedrooms,
                }}
              />
            ))}
          </div>

          {result.totalPages > 1 && (
            <div className="mt-10 flex items-center justify-between border-t border-slate-200 pt-5">
              <span className="text-sm text-slate-500">Page {page + 1} of {result.totalPages}</span>
              <div className="flex gap-2">
                <button disabled={page === 0} onClick={() => void loadListings(page - 1)} className="border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:opacity-40">Previous</button>
                <button disabled={result.last} onClick={() => void loadListings(page + 1)} className="border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:opacity-40">Next</button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Listings;
