import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getBuyerFavoritesApi, type FavoriteResponseDto, type PageResponseDto } from "../../api/propertyApi";

const BuyerFavoritesPage = () => {
  const [favorites, setFavorites] = useState<PageResponseDto<FavoriteResponseDto> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchFavorites = async (pageNo: number) => {
    setLoading(true);
    setError(null);
    try {
      const response = await getBuyerFavoritesApi(pageNo, 12);
      setFavorites(response);
      setPage(pageNo);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load favorites.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFavorites(0);
  }, []);

  return (
    <div className="max-w-7xl p-8">
      <h2 className="text-2xl font-bold text-slate-900 mb-6">My favorites</h2>

      {error && <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>}

      {loading && <p className="text-slate-500">Loading favorites...</p>}

      {!loading && favorites && favorites.content.length > 0 && (
        <>
          <div className="mb-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {favorites.content.map((favorite) => (
              <Link
                key={favorite.id}
                to={`/property/${favorite.propertyId}`}
                className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
              >
                {favorite.property.coverImageUrl && (
                  <img src={favorite.property.coverImageUrl} alt={favorite.property.title} className="h-48 w-full object-cover" />
                )}
                <div className="p-4">
                  <h3 className="font-semibold text-slate-900">{favorite.property.title}</h3>
                  <p className="text-lg font-bold text-indigo-600">${favorite.property.price.toLocaleString()}</p>
                  <p className="text-sm text-slate-600">
                    {favorite.property.bedrooms} bed • {favorite.property.bathrooms} bath
                  </p>
                  <p className="mt-2 text-xs text-slate-500">{favorite.property.areaName}, {favorite.property.city}</p>
                </div>
              </Link>
            ))}
          </div>

          {favorites.totalPages > 1 && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-slate-600">
                Page {page + 1} of {favorites.totalPages}
              </span>
              <div className="flex gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => fetchFavorites(page - 1)}
                  className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  disabled={favorites.last}
                  onClick={() => fetchFavorites(page + 1)}
                  className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {!loading && favorites && favorites.content.length === 0 && (
        <div className="rounded-lg border border-slate-200 bg-slate-50 p-8 text-center">
          <p className="text-slate-600">No favorites yet.</p>
          <Link to="/listings" className="mt-4 inline-block text-sm font-medium text-indigo-600 hover:underline">
            Browse properties
          </Link>
        </div>
      )}
    </div>
  );
};

export default BuyerFavoritesPage;
