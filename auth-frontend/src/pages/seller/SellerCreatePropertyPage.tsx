import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createPropertyApi, type PropertyRequestDto } from "../../api/propertyApi";

const propertyTypes = ["HOUSE", "APARTMENT", "VILLA", "PLOT", "COMMERCIAL"] as const;

const SellerCreatePropertyPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<PropertyRequestDto>({
    title: "",
    description: "",
    propertyType: "APARTMENT",
    price: 0,
    area: 0,
    bedrooms: 1,
    bathrooms: 1,
    city: "",
    areaName: "",
    address: "",
    featured: false,
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null);

  const update = (name: keyof PropertyRequestDto, value: string | number | boolean) => 
    setForm((current) => ({ ...current, [name]: value }));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setMessage(null);
    if (!form.title.trim() || !form.city.trim() || form.price < 0 || !Number.isFinite(form.price)) {
      setMessage({ text: "Enter a title, city, and valid non-negative price.", error: true });
      return;
    }
    setLoading(true);
    try {
      await createPropertyApi(form);
      setMessage({ text: "Property created successfully.", error: false });
      setTimeout(() => navigate("/seller/my-listings"), 1500);
    } catch (error) {
      setMessage({ text: error instanceof Error ? error.message : "Unable to create property.", error: true });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl p-8">
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Create property</h2>
          <p className="mt-1 text-slate-600">List a new property for sale or rent.</p>
        </div>
        <Link to="/seller/my-listings" className="text-sm font-medium text-indigo-600 hover:underline">
          Back to listings
        </Link>
      </div>

      <form onSubmit={submit} className="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="text-sm font-medium text-slate-700">
            Title
            <input
              required
              value={form.title}
              onChange={(e) => update("title", e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            Property Type
            <select
              value={form.propertyType}
              onChange={(e) => update("propertyType", e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal"
            >
              {propertyTypes.map((type) => (
                <option key={type}>{type}</option>
              ))}
            </select>
          </label>
          <label className="text-sm font-medium text-slate-700">
            Price
            <input
              type="number"
              required
              value={form.price}
              onChange={(e) => update("price", parseFloat(e.target.value))}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            Area (sq ft)
            <input
              type="number"
              required
              value={form.area}
              onChange={(e) => update("area", parseFloat(e.target.value))}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            Bedrooms
            <input
              type="number"
              required
              value={form.bedrooms}
              onChange={(e) => update("bedrooms", parseInt(e.target.value))}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            Bathrooms
            <input
              type="number"
              required
              value={form.bathrooms}
              onChange={(e) => update("bathrooms", parseInt(e.target.value))}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            City
            <input
              required
              value={form.city}
              onChange={(e) => update("city", e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
          <label className="text-sm font-medium text-slate-700">
            Area Name
            <input
              required
              value={form.areaName}
              onChange={(e) => update("areaName", e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </label>
        </div>

        <label className="block text-sm font-medium text-slate-700">
          Address
          <input
            required
            value={form.address}
            onChange={(e) => update("address", e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Description
          <textarea
            value={form.description}
            onChange={(e) => update("description", e.target.value)}
            rows={4}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </label>

        <label className="flex items-center gap-2 text-sm font-medium text-slate-700">
          <input
            type="checkbox"
            checked={form.featured}
            onChange={(e) => update("featured", e.target.checked)}
            className="rounded border-slate-300"
          />
          Featured property
        </label>

        {message && (
          <p className={`rounded-lg px-3 py-2 text-sm ${message.error ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"}`}>
            {message.text}
          </p>
        )}

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate("/seller/my-listings")}
            className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-700"
          >
            Cancel
          </button>
          <button
            disabled={loading}
            className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {loading ? "Creating..." : "Create property"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default SellerCreatePropertyPage;
