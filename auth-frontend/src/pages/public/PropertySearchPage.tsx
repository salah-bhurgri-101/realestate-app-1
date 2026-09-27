import { useEffect, useState, type FormEvent, type ChangeEvent } from "react";
import { searchPropertiesApi, type PropertySearchFilterDto, type PropertySummaryDto, type PageResponseDto } from "../../api/propertyApi";

const propertyTypes = ["HOUSE", "APARTMENT", "VILLA", "PLOT", "COMMERCIAL"] as const;
const statuses = ["PENDING", "APPROVED", "REJECTED", "SOLD", "RENTED"] as const;

const PropertySearchPage = () => {
  const [filters, setFilters] = useState<PropertySearchFilterDto>({
    city: "",
    propertyType: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    minBedrooms: undefined,
    maxBedrooms: undefined,
    minArea: undefined,
    maxArea: undefined,
    status: "APPROVED",
    featured: undefined,
    keyword: "",
    pageNo: 0,
    pageSize: 12,
  });

  const [results, setResults] = useState<PageResponseDto<PropertySummaryDto> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const search = async (nextPage = 0) => {
    setLoading(true);
    setError(null);
    try {
      const response = await searchPropertiesApi({ ...filters, pageNo: nextPage });
      setResults(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to search properties.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    search();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleFilterChange = (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = event.target;
    const inputValue = type === "checkbox" ? (event.target as HTMLInputElement).checked : value;
    setFilters((current) => ({ ...current, [name]: inputValue, pageNo: 0 }));
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    search(0);
  };

  return (
    <div className="max-w-7xl p-8">
      <h2 className="text-2xl font-bold text-slate-900 mb-6">Search properties</h2>

      <form onSubmit={handleSubmit} className="mb-6 grid gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:grid-cols-2 lg:grid-cols-4">
        <input
          name="keyword"
          value={filters.keyword}
          onChange={handleFilterChange}
          placeholder="Search keyword"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <input
          name="city"
          value={filters.city}
          onChange={handleFilterChange}
          placeholder="City"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <select
          name="propertyType"
          value={filters.propertyType || ""}
          onChange={handleFilterChange}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="">All property types</option>
          {propertyTypes.map((type) => (
            <option key={type}>{type}</option>
          ))}
        </select>

        <select
          name="status"
          value={filters.status || ""}
          onChange={handleFilterChange}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        >
          {statuses.map((status) => (
            <option key={status}>{status}</option>
          ))}
        </select>

        <input
          name="minPrice"
          type="number"
          value={filters.minPrice || ""}
          onChange={handleFilterChange}
          placeholder="Min price"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <input
          name="maxPrice"
          type="number"
          value={filters.maxPrice || ""}
          onChange={handleFilterChange}
          placeholder="Max price"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <input
          name="minBedrooms"
          type="number"
          value={filters.minBedrooms || ""}
          onChange={handleFilterChange}
          placeholder="Min bedrooms"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <input
          name="maxBedrooms"
          type="number"
          value={filters.maxBedrooms || ""}
          onChange={handleFilterChange}
          placeholder="Max bedrooms"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <div className="flex gap-2 sm:col-span-2 lg:col-span-4">
          <button
            type="submit"
            className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Search
          </button>
          <button
            type="button"
            onClick={() => {
              setFilters({
                city: "",
                propertyType: undefined,
                minPrice: undefined,
                maxPrice: undefined,
                minBedrooms: undefined,
                maxBedrooms: undefined,
                minArea: undefined,
                maxArea: undefined,
                status: "APPROVED",
                featured: undefined,
                keyword: "",
                pageNo: 0,
                pageSize: 12,
              });
              search(0);
            }}
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50"
          >
            Clear
          </button>
        </div>
      </form>

      {error && <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-red-700">{error}</p>}

      {loading && <p className="text-slate-500">Loading properties...</p>}

      {!loading && results && results.content.length > 0 && (
        <>
          <div className="mb-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {results.content.map((property) => (
              <a
                key={property.id}
                href={`/property/${property.id}`}
                className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
              >
                {property.coverImageUrl && (
                  <img src={property.coverImageUrl} alt={property.title} className="h-48 w-full object-cover" />
                )}
                <div className="p-4">
                  <h3 className="font-semibold text-slate-900">{property.title}</h3>
                  <p className="text-lg font-bold text-indigo-600">${property.price.toLocaleString()}</p>
                  <p className="text-sm text-slate-600">
                    {property.bedrooms} bed • {property.bathrooms} bath • {property.area} sqft
                  </p>
                  <p className="mt-2 text-xs text-slate-500">{property.areaName}, {property.city}</p>
                </div>
              </a>
            ))}
          </div>

          {results.totalPages > 1 && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-slate-600">
                Page {results.pageNo + 1} of {results.totalPages}
              </span>
              <div className="flex gap-2">
                <button
                  disabled={results.pageNo === 0}
                  onClick={() => search(results.pageNo - 1)}
                  className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  disabled={results.last}
                  onClick={() => search(results.pageNo + 1)}
                  className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {!loading && results && results.content.length === 0 && <p className="text-slate-500">No properties found.</p>}
    </div>
  );
};

export default PropertySearchPage;
