import RoleLayout from "./RoleLayout";

const AdminLayout = () => {
  return (
    <RoleLayout
      title="Admin"
      subtitle="Platform management"
      navItems={[
        { label: "Overview", to: "/admin" },
        { label: "Users", to: "/admin/users" },
        { label: "Sellers", to: "/admin/sellers" },
        { label: "Properties", to: "/admin/properties" },
        { label: "Categories", to: "/admin/categories" },
        { label: "Audit Logs", to: "/admin/audit-logs" },
        { label: "Profile", to: "/admin/profile" },
        { label: "Settings", to: "/admin/settings" },
      ]}
    />
  );
};

export default AdminLayout;
