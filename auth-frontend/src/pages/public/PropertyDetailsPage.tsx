import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getPropertyApi,
  getPropertyReviewsApi,
  togglePropertyFavoriteApi,
  type PageResponseDto,
  type PropertyResponseDto,
  type PropertyReviewResponseDto,
} from "../../api/propertyApi";

const PropertyDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState<PropertyResponseDto | null>(null);
  const [reviews, setReviews] = useState<PageResponseDto<PropertyReviewResponseDto> | null>(null);
  const [favoriteMessage, setFavoriteMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setError("Property ID is required.");
      return;
    }
    Promise.all([getPropertyApi(id), getPropertyReviewsApi(id)])
      .then(([propertyResult, reviewsResult]) => {
        setProperty(propertyResult);
        setReviews(reviewsResult);
      })
      .catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load property."));
  }, [id]);

  if (error) return <div className="max-w-4xl p-8"><p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p></div>;
  if (!property) return <div className="max-w-4xl p-8"><p className="text-slate-500">Loading property...</p></div>;

  return (
    <div className="max-w-6xl p-8">
      <button onClick={() => navigate(-1)} className="mb-6 text-sm font-medium text-indigo-600 hover:underline">
        ← Back
      </button>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          {property.images && property.images.length > 0 && (
            <div className="mb-6 overflow-hidden rounded-xl bg-slate-200 shadow-sm">
              <img src={property.images[0]?.imageUrl} alt={property.title} className="h-96 w-full object-cover" />
            </div>
          )}

          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <h1 className="text-3xl font-bold text-slate-900">{property.title}</h1>
            <p className="mt-2 text-xl font-semibold text-indigo-600">
              ${property.price.toLocaleString()}
            </p>

            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              <div className="rounded-lg bg-slate-50 p-3">
                <dt className="text-sm font-medium text-slate-700">Property Type</dt>
                <dd className="text-slate-900">{property.propertyType}</dd>
              </div>
              <div className="rounded-lg bg-slate-50 p-3">
                <dt className="text-sm font-medium text-slate-700">Area</dt>
                <dd className="text-slate-900">{property.area} sq ft</dd>
              </div>
              <div className="rounded-lg bg-slate-50 p-3">
                <dt className="text-sm font-medium text-slate-700">Bedrooms</dt>
                <dd className="text-slate-900">{property.bedrooms}</dd>
              </div>
              <div className="rounded-lg bg-slate-50 p-3">
                <dt className="text-sm font-medium text-slate-700">Bathrooms</dt>
                <dd className="text-slate-900">{property.bathrooms}</dd>
              </div>
            </div>

            <div className="mt-6 border-t border-slate-200 pt-6">
              <h2 className="text-lg font-semibold text-slate-900">Description</h2>
              <p className="mt-2 text-slate-600">{property.description}</p>
            </div>

            {property.amenities && property.amenities.length > 0 && (
              <div className="mt-6 border-t border-slate-200 pt-6">
                <h2 className="text-lg font-semibold text-slate-900">Amenities</h2>
                <ul className="mt-3 grid gap-2 sm:grid-cols-2">
                  {property.amenities.map((amenity: string) => (
                    <li key={amenity} className="flex items-center gap-2 text-slate-600">
                      <span className="h-2 w-2 rounded-full bg-indigo-600"></span>
                      {amenity.replace(/_/g, " ")}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div className="mt-6 border-t border-slate-200 pt-6">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-slate-900">Reviews</h2>
                <button
                  onClick={() => {
                    togglePropertyFavoriteApi(property.id)
                      .then(() => setFavoriteMessage("Favorite updated."))
                      .catch((reason: unknown) => setFavoriteMessage(reason instanceof Error ? reason.message : "Unable to update favorite."));
                  }}
                  className="rounded-lg border border-indigo-200 px-3 py-2 text-sm text-indigo-700"
                >
                  Save favorite
                </button>
              </div>
              {favoriteMessage && <p className="mt-2 text-sm text-slate-500">{favoriteMessage}</p>}
              {!reviews?.content.length && <p className="mt-3 text-sm text-slate-500">No reviews yet.</p>}
              <div className="mt-3 space-y-3">
                {reviews?.content.map((review) => (
                  <article key={review.id} className="rounded-lg bg-slate-50 p-3">
                    <p className="text-amber-500">{"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}</p>
                    {review.comment && <p className="mt-1 text-sm text-slate-700">{review.comment}</p>}
                    <p className="mt-1 text-xs text-slate-500">{new Date(review.createdAt).toLocaleDateString()}</p>
                  </article>
                ))}
              </div>
              <button onClick={() => navigate(`/property/${property.id}/review`)} className="mt-4 text-sm font-medium text-indigo-600 hover:underline">Write a review</button>
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="space-y-4">
            <div className="border-b border-slate-200 pb-4">
              <dt className="text-sm font-medium text-slate-700">Status</dt>
              <dd className="mt-1">
                <span className={`rounded-full px-3 py-1 text-xs font-medium ${
                  property.status === "APPROVED" ? "bg-emerald-100 text-emerald-700" :
                  property.status === "PENDING" ? "bg-yellow-100 text-yellow-700" :
                  "bg-red-100 text-red-700"
                }`}>
                  {property.status}
                </span>
              </dd>
            </div>

            <div className="border-b border-slate-200 pb-4">
              <dt className="text-sm font-medium text-slate-700">Location</dt>
              <dd className="mt-1 text-slate-600">{property.areaName}, {property.city}</dd>
            </div>

            <div className="border-b border-slate-200 pb-4">
              <dt className="text-sm font-medium text-slate-700">Address</dt>
              <dd className="mt-1 text-slate-600">{property.address}</dd>
            </div>

            <div className="text-sm text-slate-500">
              <dt>Created</dt>
              <dd>{new Date(property.createdAt).toLocaleDateString()}</dd>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PropertyDetailsPage;
