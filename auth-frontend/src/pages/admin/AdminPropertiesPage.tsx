import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  searchPropertiesApi,
  updatePropertyStatusApi,
  type PageResponseDto,
  type PropertyStatus,
  type PropertySummaryDto,
} from "../../api/propertyApi";

const statuses: PropertyStatus[] = ["PENDING", "APPROVED", "REJECTED", "SOLD", "RENTED"];

const badgeClasses: Record<PropertyStatus, string> = {
  PENDING: "bg-amber-100 text-amber-700",
  APPROVED: "bg-emerald-100 text-emerald-700",
  REJECTED: "bg-rose-100 text-rose-700",
  SOLD: "bg-slate-200 text-slate-700",
  RENTED: "bg-blue-100 text-blue-700",
};

const formatPrice = (price: number) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(price);

const AdminPropertiesPage = () => {
  const [result, setResult] = useState<PageResponseDto<PropertySummaryDto> | null>(null);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<PropertyStatus | "ALL">("ALL");
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const loadProperties = async (pageNo = 0, status = statusFilter) => {
    setLoading(true);
    setError(null);
    try {
      const response = await searchPropertiesApi({
        pageNo,
        pageSize: 12,
        ...(status === "ALL" ? {} : { status }),
      });
      setResult(response);
      setPage(pageNo);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load properties.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadProperties(0);
  }, []);

  const summary = useMemo(() => {
    const items = result?.content ?? [];
    return {
      total: result?.totalElements ?? 0,
      approved: items.filter((item) => item.status === "APPROVED").length,
      pending: items.filter((item) => item.status === "PENDING").length,
      rejected: items.filter((item) => item.status === "REJECTED").length,
    };
  }, [result]);

  const changeStatus = async (propertyId: string, status: PropertyStatus) => {
    const property = result?.content.find((item) => item.id === propertyId);
    if (!property || property.status === status) return;

    setUpdatingId(propertyId);
    setError(null);
    setMessage(null);
    try {
      const updatedProperty = await updatePropertyStatusApi(propertyId, { status });
      setMessage("Property status updated successfully.");
      setResult((current) => current
        ? {
            ...current,
            content: current.content.map((item) =>
              item.id === updatedProperty.id
                ? { ...item, status: updatedProperty.status }
                : item,
            ),
          }
        : current);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update property status.");
    } finally {
      setUpdatingId(null);
    }
  };

  const selectFilter = (status: PropertyStatus | "ALL") => {
    setStatusFilter(status);
    void loadProperties(0, status);
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-amber-700">Admin workspace</p>
          <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">Manage properties</h2>
          <p className="mt-2 text-sm text-slate-500">Review live listings and control their publication status.</p>
        </div>
        <Link to="/search" className="w-fit bg-slate-950 px-4 py-3 text-sm font-semibold text-white">View marketplace</Link>
      </div>

      {error && <p className="bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}
      {message && <p className="bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{message}</p>}

      <div className="grid gap-4 md:grid-cols-4">
        {[
          ["Total listings", summary.total, "text-slate-950"],
          ["Approved on page", summary.approved, "text-emerald-600"],
          ["Pending on page", summary.pending, "text-amber-600"],
          ["Rejected on page", summary.rejected, "text-rose-600"],
        ].map(([label, value, color]) => (
          <div key={label} className="border border-slate-200 bg-white p-5 shadow-[0_10px_35px_rgba(20,32,51,0.05)]">
            <p className="text-sm text-slate-500">{label}</p>
            <p className={`mt-2 text-3xl font-bold ${color}`}>{value}</p>
          </div>
        ))}
      </div>

      <div className="overflow-hidden border border-slate-200 bg-white shadow-[0_10px_35px_rgba(20,32,51,0.05)]">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-6 py-4">
          <h3 className="text-lg font-bold text-slate-900">Property listings</h3>
          <div className="flex flex-wrap gap-2 text-sm">
            {(["ALL", ...statuses] as const).map((status) => (
              <button
                key={status}
                onClick={() => selectFilter(status)}
                className={`px-3 py-2 font-semibold ${statusFilter === status ? "bg-slate-950 text-white" : "bg-slate-100 text-slate-600"}`}
              >
                {status === "ALL" ? "All" : status}
              </button>
            ))}
          </div>
        </div>

        {loading && <p className="px-6 py-10 text-sm text-slate-500">Loading properties...</p>}
        {!loading && result?.content.length === 0 && <p className="px-6 py-10 text-sm text-slate-500">No properties found for this filter.</p>}
        {!loading && result && result.content.length > 0 && (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50 text-left text-xs uppercase tracking-wider text-slate-500">
                <tr>
                  <th className="px-6 py-4 font-semibold">Property</th>
                  <th className="px-6 py-4 font-semibold">Seller ID</th>
                  <th className="px-6 py-4 font-semibold">Status</th>
                  <th className="px-6 py-4 font-semibold">Price</th>
                  <th className="px-6 py-4 font-semibold">Created</th>
                  <th className="px-6 py-4 text-right font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 text-sm text-slate-700">
                {result.content.map((property) => (
                  <tr key={property.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4">
                      <p className="font-semibold text-slate-900">{property.title}</p>
                      <p className="text-slate-500">{property.areaName ? `${property.areaName}, ` : ""}{property.city}</p>
                    </td>
                    <td className="px-6 py-4 font-mono text-xs text-slate-500">Not exposed</td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-2.5 py-1 text-xs font-semibold ${badgeClasses[property.status]}`}>{property.status}</span>
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-900">{formatPrice(property.price)}</td>
                    <td className="px-6 py-4 text-slate-500">{new Date(property.createdAt).toLocaleDateString()}</td>
                    <td className="px-6 py-4">
                      <div className="flex justify-end gap-2">
                        <Link to={`/property/${property.id}`} className="border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700">View</Link>
                        <select
                          value={property.status}
                          disabled={updatingId === property.id}
                          onChange={(event) => void changeStatus(property.id, event.target.value as PropertyStatus)}
                          className="border border-slate-200 bg-white px-2 py-2 text-xs font-semibold text-slate-700 disabled:opacity-50"
                        >
                          {statuses.map((status) => <option key={status} value={status}>{updatingId === property.id ? "Updating..." : status}</option>)}
                        </select>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {result && result.totalPages > 1 && (
          <div className="flex items-center justify-between border-t border-slate-200 px-6 py-4">
            <span className="text-sm text-slate-500">Page {page + 1} of {result.totalPages}</span>
            <div className="flex gap-2">
              <button disabled={page === 0} onClick={() => void loadProperties(page - 1)} className="border border-slate-200 px-3 py-2 text-sm disabled:opacity-40">Previous</button>
              <button disabled={result.last} onClick={() => void loadProperties(page + 1)} className="border border-slate-200 px-3 py-2 text-sm disabled:opacity-40">Next</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPropertiesPage;
