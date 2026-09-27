import { useState } from "react";

type ToggleKey =
  | "maintenanceMode"
  | "newSellerApproval"
  | "autoEmail"
  | "propertyModeration"
  | "twoFactorAdmin";

const AdminSettingsPage = () => {
  const [form, setForm] = useState({
    siteName: "Haven Realty",
    supportEmail: "support@havenrealty.com",
    timezone: "UTC+05:30",
    defaultCurrency: "USD",
    primaryColor: "#4f46e5",
    maintenanceMode: false,
    newSellerApproval: true,
    autoEmail: true,
    propertyModeration: true,
    twoFactorAdmin: true,
  });

  const toggleSetting = (key: ToggleKey) => {
    setForm((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <div className="mx-auto max-w-7xl p-6 md:p-8">
      <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-indigo-600">Admin</p>
          <h2 className="mt-2 text-3xl font-bold text-slate-900">Site settings</h2>
        </div>
        <div className="flex gap-3">
          <button className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
            Cancel
          </button>
          <button className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-500">
            Save changes
          </button>
        </div>
      </div>

      <div className="mb-8 grid gap-4 md:grid-cols-4">
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Active users</p>
          <p className="mt-2 text-2xl font-bold text-slate-900">12,480</p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Listings</p>
          <p className="mt-2 text-2xl font-bold text-slate-900">3,920</p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">Pending reviews</p>
          <p className="mt-2 text-2xl font-bold text-amber-600">41</p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm text-slate-500">System status</p>
          <p className="mt-2 text-xl font-bold text-emerald-600">Healthy</p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
        <div className="space-y-6">
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-5 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900">General settings</h3>
              <span className="rounded-full bg-indigo-100 px-2.5 py-1 text-xs font-semibold text-indigo-700">
                Live
              </span>
            </div>

            <div className="grid gap-5 md:grid-cols-2">
              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-slate-700">Site name</span>
                <input
                  value={form.siteName}
                  onChange={(event) => handleChange("siteName", event.target.value)}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-slate-900 outline-none transition focus:border-indigo-400 focus:bg-white"
                />
              </label>

              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-slate-700">Support email</span>
                <input
                  type="email"
                  value={form.supportEmail}
                  onChange={(event) => handleChange("supportEmail", event.target.value)}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-slate-900 outline-none transition focus:border-indigo-400 focus:bg-white"
                />
              </label>

              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-slate-700">Timezone</span>
                <select
                  value={form.timezone}
                  onChange={(event) => handleChange("timezone", event.target.value)}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-slate-900 outline-none transition focus:border-indigo-400 focus:bg-white"
                >
                  <option>UTC+00:00</option>
                  <option>UTC+05:30</option>
                  <option>UTC-05:00</option>
                  <option>UTC+01:00</option>
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-slate-700">Default currency</span>
                <select
                  value={form.defaultCurrency}
                  onChange={(event) => handleChange("defaultCurrency", event.target.value)}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-slate-900 outline-none transition focus:border-indigo-400 focus:bg-white"
                >
                  <option>USD</option>
                  <option>EUR</option>
                  <option>GBP</option>
                  <option>INR</option>
                </select>
              </label>
            </div>
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="mb-5 text-lg font-semibold text-slate-900">Branding & appearance</h3>

            <div className="grid gap-5 md:grid-cols-[1fr_auto] md:items-end">
              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-slate-700">Primary brand color</span>
                <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5">
                  <input
                    type="color"
                    value={form.primaryColor}
                    onChange={(event) => handleChange("primaryColor", event.target.value)}
                    className="h-10 w-12 cursor-pointer rounded border-0 bg-transparent p-0"
                  />
                  <input
                    value={form.primaryColor}
                    onChange={(event) => handleChange("primaryColor", event.target.value)}
                    className="w-full border-0 bg-transparent text-slate-900 outline-none"
                  />
                </div>
              </label>

              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-4 text-center text-sm text-slate-600">
                Logo preview
                <div className="mt-3 inline-flex h-12 w-12 items-center justify-center rounded-lg text-base font-bold text-white" style={{ backgroundColor: form.primaryColor }}>
                  HR
                </div>
              </div>
            </div>
          </section>
        </div>

        <aside className="space-y-6">
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="mb-5 text-lg font-semibold text-slate-900">Security & access</h3>

            <div className="space-y-4">
              {[
                { key: "maintenanceMode", label: "Maintenance mode", value: form.maintenanceMode },
                { key: "newSellerApproval", label: "Seller approval required", value: form.newSellerApproval },
                { key: "propertyModeration", label: "Property moderation", value: form.propertyModeration },
                { key: "twoFactorAdmin", label: "2FA for admins", value: form.twoFactorAdmin },
              ].map((item) => (
                <div key={item.key} className="flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-slate-50 p-3">
                  <span className="text-sm font-medium text-slate-700">{item.label}</span>
                  <button
                    type="button"
                    aria-label={item.label}
                    onClick={() => toggleSetting(item.key as ToggleKey)}
                    className={`relative h-6 w-11 rounded-full transition ${
                      item.value ? "bg-indigo-600" : "bg-slate-300"
                    }`}
                  >
                    <span
                      className={`absolute top-1 h-4 w-4 rounded-full bg-white transition ${
                        item.value ? "left-6" : "left-1"
                      }`}
                    />
                  </button>
                </div>
              ))}
            </div>
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="mb-5 text-lg font-semibold text-slate-900">Communication</h3>

            <div className="space-y-4">
              {[
                { key: "autoEmail", label: "Automated email alerts", value: form.autoEmail },
              ].map((item) => (
                <div key={item.key} className="flex items-center justify-between gap-4 rounded-xl border border-slate-200 bg-slate-50 p-3">
                  <span className="text-sm font-medium text-slate-700">{item.label}</span>
                  <button
                    type="button"
                    aria-label={item.label}
                    onClick={() => toggleSetting(item.key as ToggleKey)}
                    className={`relative h-6 w-11 rounded-full transition ${
                      item.value ? "bg-emerald-500" : "bg-slate-300"
                    }`}
                  >
                    <span
                      className={`absolute top-1 h-4 w-4 rounded-full bg-white transition ${
                        item.value ? "left-6" : "left-1"
                      }`}
                    />
                  </button>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
};

export default AdminSettingsPage;
