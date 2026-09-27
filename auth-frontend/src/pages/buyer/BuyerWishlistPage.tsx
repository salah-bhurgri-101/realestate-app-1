import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getBuyerFavoritesApi,
  togglePropertyFavoriteApi,
  type FavoriteResponseDto,
  type PageResponseDto,
} from "../../api/propertyApi";

const BuyerWishlistPage = () => {
  const [favorites, setFavorites] = useState<PageResponseDto<FavoriteResponseDto> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadWishlist = async (pageNo = 0) => {
    setLoading(true);
    setError(null);
    try {
      setFavorites(await getBuyerFavoritesApi(pageNo, 12));
      setPage(pageNo);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load your wishlist.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadWishlist();
  }, []);

  const removeFavorite = async (propertyId: string) => {
    setRemovingId(propertyId);
    setError(null);
    try {
      await togglePropertyFavoriteApi(propertyId);
      await loadWishlist(page);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to remove favorite.");
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-700">Your collection</p>
        <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">Wishlist</h2>
        <p className="mt-2 text-sm text-slate-500">Properties you saved for your next move.</p>
      </div>

      {error && <p className="bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}
      {loading && <p className="text-sm text-slate-500">Loading wishlist...</p>}

      {!loading && favorites?.content.length === 0 && (
        <div className="border border-slate-200 bg-white p-12 text-center">
          <h3 className="text-xl font-bold text-slate-900">Your wishlist is empty</h3>
          <p className="mt-2 text-sm text-slate-500">Save a property to keep it close.</p>
          <Link to="/listings" className="mt-5 inline-block bg-slate-950 px-4 py-3 text-sm font-semibold text-white">Browse approved properties</Link>
        </div>
      )}

      {!loading && favorites && favorites.content.length > 0 && (
        <>
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {favorites.content.map((favorite) => (
              <article key={favorite.id} className="overflow-hidden border border-slate-200 bg-white shadow-[0_10px_35px_rgba(20,32,51,0.06)]">
                {favorite.property.coverImageUrl ? (
                  <img src={favorite.property.coverImageUrl} alt={favorite.property.title} className="h-52 w-full object-cover" />
                ) : (
                  <div className="flex h-52 items-center justify-center bg-slate-100 text-sm text-slate-400">No image available</div>
                )}
                <div className="p-5">
                  <div className="flex items-start justify-between gap-3">
                    <h3 className="font-bold text-slate-900">{favorite.property.title}</h3>
                    <span className="text-amber-500">★</span>
                  </div>
                  <p className="mt-2 text-xl font-extrabold text-slate-950">${favorite.property.price.toLocaleString()}</p>
                  <p className="mt-1 text-sm text-slate-500">{favorite.property.bedrooms} bed · {favorite.property.bathrooms} bath · {favorite.property.area} sqft</p>
                  <p className="mt-2 text-xs text-slate-500">{favorite.property.areaName ? `${favorite.property.areaName}, ` : ""}{favorite.property.city}</p>
                  <div className="mt-5 flex gap-2">
                    <Link to={`/property/${favorite.propertyId}`} className="flex-1 bg-slate-950 px-3 py-2.5 text-center text-sm font-semibold text-white">View property</Link>
                    <button disabled={removingId === favorite.propertyId} onClick={() => void removeFavorite(favorite.propertyId)} className="border border-rose-200 px-3 py-2.5 text-sm font-semibold text-rose-600 disabled:opacity-50">{removingId === favorite.propertyId ? "Removing..." : "Remove"}</button>
                  </div>
                </div>
              </article>
            ))}
          </div>

          {favorites.totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-slate-200 pt-5">
              <span className="text-sm text-slate-500">Page {page + 1} of {favorites.totalPages}</span>
              <div className="flex gap-2">
                <button disabled={page === 0} onClick={() => void loadWishlist(page - 1)} className="border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-40">Previous</button>
                <button disabled={favorites.last} onClick={() => void loadWishlist(page + 1)} className="border border-slate-200 px-4 py-2 text-sm font-semibold disabled:opacity-40">Next</button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default BuyerWishlistPage;
