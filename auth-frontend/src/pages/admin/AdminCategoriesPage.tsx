type CategoryStatus = "Active" | "Draft" | "Archived";

type Category = {
  id: string;
  name: string;
  description: string;
  listings: number;
  status: CategoryStatus;
  featured: boolean;
};

const categories: Category[] = [
  {
    id: "residential",
    name: "Residential",
    description: "Houses, apartments, condos and villas",
    listings: 284,
    status: "Active",
    featured: true,
  },
  {
    id: "commercial",
    name: "Commercial",
    description: "Retail, office and mixed-use properties",
    listings: 96,
    status: "Active",
    featured: true,
  },
  {
    id: "land",
    name: "Land & Plots",
    description: "Vacant land, development sites and agriculture",
    listings: 42,
    status: "Draft",
    featured: false,
  },
  {
    id: "vacation",
    name: "Vacation Homes",
    description: "Short-term and resort-style rental properties",
    listings: 68,
    status: "Archived",
    featured: false,
  },
];

const statusClasses: Record<CategoryStatus, string> = {
  Active: "bg-emerald-100 text-emerald-700",
  Draft: "bg-amber-100 text-amber-700",
  Archived: "bg-slate-200 text-slate-700",
};

const AdminCategoriesPage = () => {
  const summary = {
    total: categories.length,
    active: categories.filter((item) => item.status === "Active").length,
    draft: categories.filter((item) => item.status === "Draft").length,
    listings: categories.reduce((total, item) => total + item.listings, 0),
  };

  return (
    <div className="max-w-7xl mx-auto p-8">
      <div className="mb-8 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-indigo-600">Catalog</p>
          <h2 className="text-3xl font-bold text-slate-900">Manage Categories</h2>
        </div>
        <button className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500">
          Add category
        </button>
      </div>

      <div className="mb-8 grid gap-4 md:grid-cols-4">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Total categories</p>
          <p className="mt-2 text-2xl font-bold text-slate-900">{summary.total}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Active</p>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{summary.active}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Drafts</p>
          <p className="mt-2 text-2xl font-bold text-amber-600">{summary.draft}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Active listings</p>
          <p className="mt-2 text-2xl font-bold text-slate-900">{summary.listings}</p>
        </div>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h3 className="text-lg font-semibold text-slate-900">Category directory</h3>
          <button className="text-sm font-medium text-indigo-600 hover:text-indigo-500">Bulk actions</button>
        </div>

        <div className="divide-y divide-slate-200">
          {categories.map((category) => (
            <div key={category.id} className="grid gap-4 px-6 py-5 md:grid-cols-[1.6fr_1.2fr_0.9fr_0.9fr_auto] md:items-center">
              <div>
                <div className="flex items-center gap-2">
                  <p className="text-lg font-semibold text-slate-900">{category.name}</p>
                  {category.featured && (
                    <span className="rounded-full bg-violet-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-violet-700">
                      Featured
                    </span>
                  )}
                </div>
                <p className="mt-1 text-sm text-slate-500">{category.description}</p>
              </div>

              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Listings</p>
                <p className="mt-1 text-base font-semibold text-slate-900">{category.listings}</p>
              </div>

              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Status</p>
                <span className={`mt-1 inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${statusClasses[category.status]}`}>
                  {category.status}
                </span>
              </div>

              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Order</p>
                <p className="mt-1 text-base font-semibold text-slate-900">#{category.id.length}</p>
              </div>

              <div className="flex gap-2 md:justify-end">
                <button className="rounded-md border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Edit
                </button>
                <button className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-500">
                  View
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AdminCategoriesPage;
