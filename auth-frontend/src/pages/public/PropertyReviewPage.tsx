import { useState, type FormEvent } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { addPropertyReviewApi, type PropertyReviewRequestDto } from "../../api/propertyApi";

const PropertyReviewPage = () => {
  const { propertyId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState<PropertyReviewRequestDto>({ rating: 5, comment: "" });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setMessage(null);
    if (!propertyId) {
      setMessage({ text: "Property ID is required.", error: true });
      return;
    }
    setLoading(true);
    try {
      await addPropertyReviewApi(propertyId, form);
      setMessage({ text: "Review added successfully.", error: false });
      setTimeout(() => navigate(`/property/${propertyId}`), 1500);
    } catch (error) {
      setMessage({ text: error instanceof Error ? error.message : "Unable to add review.", error: true });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-slate-900">Write a review</h2>
        <p className="mt-1 text-slate-600">Share your experience with this property.</p>
      </div>

      <form onSubmit={submit} className="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <label className="block text-sm font-medium text-slate-700">
          Rating
          <div className="mt-2 flex gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                onClick={() => setForm((c) => ({ ...c, rating: star }))}
                className={`text-2xl ${form.rating >= star ? "text-yellow-400" : "text-slate-300"}`}
              >
                ★
              </button>
            ))}
          </div>
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Comment
          <textarea
            value={form.comment ?? ""}
            onChange={(e) => setForm((c) => ({ ...c, comment: e.target.value }))}
            rows={4}
            maxLength={1000}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </label>

        {message && (
          <p className={`rounded-lg px-3 py-2 text-sm ${message.error ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"}`}>
            {message.text}
          </p>
        )}

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate(`/property/${propertyId}`)}
            className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-700"
          >
            Cancel
          </button>
          <button
            disabled={loading}
            className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {loading ? "Posting..." : "Post review"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default PropertyReviewPage;
