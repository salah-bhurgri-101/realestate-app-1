import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  deletePropertyApi,
  getMyPropertiesApi,
  type PageResponseDto,
  type PropertySummaryDto,
} from "../../api/propertyApi";

const MyListingsPage = () => {
  const [listings, setListings] = useState<PageResponseDto<PropertySummaryDto> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadListings = async (pageNo = 0) => {
    setLoading(true);
    setError(null);
    try {
      setListings(await getMyPropertiesApi(pageNo, 12));
      setPage(pageNo);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load your properties.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadListings();
  }, []);

  const removeListing = async (propertyId: string) => {
    if (!window.confirm("Delete this property?")) return;
    try {
      await deletePropertyApi(propertyId);
      await loadListings(page);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to delete property.");
    }
  };

  return (
    <div className="mx-auto max-w-7xl p-8">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">My listings</h2>
          <p className="mt-1 text-slate-600">Manage properties created by your account.</p>
        </div>
        <Link to="/seller/add-property" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white">
          Add property
        </Link>
      </div>

      {error && <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>}
      {loading && <p className="text-slate-500">Loading listings...</p>}

      {!loading && listings?.content.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-10 text-center">
          <p className="text-slate-600">You have not created any properties yet.</p>
        </div>
      )}

      {!loading && listings && listings.content.length > 0 && (
        <>
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {listings.content.map((property) => (
              <article key={property.id} className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                {property.coverImageUrl ? (
                  <img src={property.coverImageUrl} alt={property.title} className="h-48 w-full object-cover" />
                ) : (
                  <div className="flex h-48 items-center justify-center bg-slate-100 text-sm text-slate-400">No image</div>
                )}
                <div className="p-4">
                  <div className="flex items-start justify-between gap-3">
                    <h3 className="font-semibold text-slate-900">{property.title}</h3>
                    <span className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-600">{property.status}</span>
                  </div>
                  <p className="mt-2 text-lg font-bold text-indigo-600">${property.price.toLocaleString()}</p>
                  <p className="text-sm text-slate-600">{property.bedrooms} bed · {property.bathrooms} bath · {property.area} sqft</p>
                  <p className="mt-2 text-xs text-slate-500">{property.areaName}, {property.city}</p>
                  <div className="mt-4 flex gap-2">
                    <Link to={`/property/${property.id}`} className="rounded border border-slate-300 px-3 py-2 text-sm text-slate-700">View</Link>
                    <Link to={`/seller/property/${property.id}/edit`} className="rounded bg-indigo-600 px-3 py-2 text-sm text-white">Manage</Link>
                    <button onClick={() => removeListing(property.id)} className="rounded border border-red-200 px-3 py-2 text-sm text-red-600">Delete</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
          {listings.totalPages > 1 && (
            <div className="mt-6 flex items-center justify-between">
              <span className="text-sm text-slate-600">Page {page + 1} of {listings.totalPages}</span>
              <div className="flex gap-2">
                <button disabled={page === 0} onClick={() => void loadListings(page - 1)} className="rounded border border-slate-300 px-3 py-2 text-sm disabled:opacity-50">Previous</button>
                <button disabled={listings.last} onClick={() => void loadListings(page + 1)} className="rounded border border-slate-300 px-3 py-2 text-sm disabled:opacity-50">Next</button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default MyListingsPage;
