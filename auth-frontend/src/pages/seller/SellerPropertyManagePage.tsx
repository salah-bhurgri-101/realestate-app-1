import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  addPropertyAmenitiesApi,
  addPropertyImagesApi,
  deletePropertyImageApi,
  getPropertyApi,
  removeAmenityApi,
  updatePropertyApi,
  updatePropertyStatusApi,
  type Amenity,
  type PropertyRequestDto,
  type PropertyResponseDto,
  type PropertyStatus,
} from "../../api/propertyApi";

const amenities: Amenity[] = ["PARKING", "ELEVATOR", "SECURITY", "GYM", "SWIMMING_POOL", "MOSQUE", "SCHOOL_NEARBY", "HOSPITAL_NEARBY", "INTERNET", "GENERATOR"];
const statuses: PropertyStatus[] = ["PENDING", "APPROVED", "REJECTED", "SOLD", "RENTED"];

const SellerPropertyManagePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState<PropertyResponseDto | null>(null);
  const [form, setForm] = useState<Partial<PropertyRequestDto>>({});
  const [imageUrl, setImageUrl] = useState("");
  const [status, setStatus] = useState<PropertyStatus>("PENDING");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    getPropertyApi(id).then((result) => {
      setProperty(result);
      setForm(result);
      setStatus(result.status);
    }).catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load property."));
  }, [id]);

  const updateField = (name: keyof PropertyRequestDto, value: string | number) => {
    setForm((current) => ({ ...current, [name]: value }));
  };

  const save = async (event: FormEvent) => {
    event.preventDefault();
    if (!id) return;
    try {
      const result = await updatePropertyApi(id, form);
      setProperty(result);
      setMessage("Property details updated.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update property.");
    }
  };

  const addImage = async () => {
    if (!id || !imageUrl.trim()) return;
    try {
      const result = await addPropertyImagesApi(id, [{ imageUrl: imageUrl.trim(), coverImage: !property?.images.length, sortOrder: property?.images.length ?? 0 }]);
      setProperty(result);
      setImageUrl("");
      setMessage("Image added.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to add image.");
    }
  };

  const removeImage = async (imageId: string) => {
    if (!id || !window.confirm("Delete this image?")) return;
    try {
      await deletePropertyImageApi(id, imageId);
      setProperty(await getPropertyApi(id));
      setMessage("Image deleted.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to delete image.");
    }
  };

  const toggleAmenity = async (amenity: Amenity) => {
    if (!id) return;
    try {
      const selected = property?.amenities?.includes(amenity);
      if (selected) await removeAmenityApi(id, amenity);
      else await addPropertyAmenitiesApi(id, [amenity]);
      const result = await getPropertyApi(id);
      setProperty(result);
      setMessage("Amenities updated.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update amenities.");
    }
  };

  const updateStatus = async () => {
    if (!id) return;
    try {
      const result = await updatePropertyStatusApi(id, { status });
      setProperty(result);
      setMessage("Property status updated.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update status.");
    }
  };

  if (!property) return <div className="max-w-4xl p-8"><p className="text-slate-500">{error ?? "Loading property..."}</p></div>;

  return (
    <div className="mx-auto max-w-5xl p-8">
      <div className="mb-6 flex items-center justify-between">
        <div><h2 className="text-2xl font-bold text-slate-900">Manage property</h2><p className="text-slate-600">{property.title}</p></div>
        <Link to="/seller/my-listings" className="text-sm text-indigo-600 hover:underline">Back to listings</Link>
      </div>
      {(message || error) && <p className={`mb-4 rounded-lg px-4 py-3 ${error ? "bg-red-50 text-red-700" : "bg-emerald-50 text-emerald-700"}`}>{error ?? message}</p>}
      <form onSubmit={save} className="grid gap-4 rounded-xl border border-slate-200 bg-white p-6 sm:grid-cols-2">
        <label className="text-sm font-medium text-slate-700">Title<input required value={form.title ?? ""} onChange={(e) => updateField("title", e.target.value)} className="mt-1 w-full rounded border border-slate-300 px-3 py-2 font-normal" /></label>
        <label className="text-sm font-medium text-slate-700">Property type<select value={form.propertyType ?? "HOUSE"} onChange={(e) => updateField("propertyType", e.target.value)} className="mt-1 w-full rounded border border-slate-300 px-3 py-2 font-normal">{["HOUSE", "APARTMENT", "VILLA", "PLOT", "COMMERCIAL"].map((type) => <option key={type}>{type}</option>)}</select></label>
        <label className="text-sm font-medium text-slate-700">Price<input type="number" min="0" required value={form.price ?? 0} onChange={(e) => updateField("price", Number(e.target.value))} className="mt-1 w-full rounded border border-slate-300 px-3 py-2 font-normal" /></label>
        <label className="text-sm font-medium text-slate-700">City<input required value={form.city ?? ""} onChange={(e) => updateField("city", e.target.value)} className="mt-1 w-full rounded border border-slate-300 px-3 py-2 font-normal" /></label>
        <label className="text-sm font-medium text-slate-700 sm:col-span-2">Description<textarea value={form.description ?? ""} onChange={(e) => updateField("description", e.target.value)} rows={4} className="mt-1 w-full rounded border border-slate-300 px-3 py-2 font-normal" /></label>
        <button className="rounded bg-indigo-600 px-4 py-2 text-sm font-medium text-white sm:col-span-2">Save changes</button>
      </form>
      <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6">
        <h3 className="font-semibold text-slate-900">Status</h3>
        <div className="mt-3 flex gap-2"><select value={status} onChange={(e) => setStatus(e.target.value as PropertyStatus)} className="rounded border border-slate-300 px-3 py-2">{statuses.map((value) => <option key={value}>{value}</option>)}</select><button onClick={() => void updateStatus()} className="rounded bg-slate-900 px-4 py-2 text-sm text-white">Update status</button></div>
      </section>
      <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6">
        <h3 className="font-semibold text-slate-900">Images</h3>
        <div className="mt-3 flex gap-2"><input value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="https://..." className="min-w-0 flex-1 rounded border border-slate-300 px-3 py-2" /><button onClick={() => void addImage()} className="rounded bg-indigo-600 px-4 py-2 text-sm text-white">Add image</button></div>
        <div className="mt-4 grid gap-3 sm:grid-cols-3">{property.images.map((image) => (
          <div key={image.id} className="relative">
            <img src={image.imageUrl} alt={property.title} className="h-32 w-full rounded object-cover" />
            <button onClick={() => void removeImage(image.id)} className="absolute right-2 top-2 rounded bg-red-600 px-2 py-1 text-xs text-white">Delete</button>
          </div>
        ))}</div>
      </section>
      <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6">
        <h3 className="font-semibold text-slate-900">Amenities</h3>
        <div className="mt-3 flex flex-wrap gap-2">{amenities.map((amenity) => <button key={amenity} onClick={() => void toggleAmenity(amenity)} className={`rounded-full px-3 py-2 text-xs ${property.amenities?.includes(amenity) ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-700"}`}>{amenity.replace(/_/g, " ")}</button>)}</div>
      </section>
      <button onClick={() => navigate(`/property/${property.id}`)} className="mt-6 text-sm text-indigo-600 hover:underline">View public property page</button>
    </div>
  );
};

export default SellerPropertyManagePage;
