import RoleLayout from "./RoleLayout";

const SellerLayout = () => {
  return (
    <RoleLayout
      title="Seller"
      subtitle="Property management"
      navItems={[
        { label: "Overview", to: "/seller" },
        { label: "Add Property", to: "/seller/add-property" },
        { label: "My Listings", to: "/seller/my-listings" },
        { label: "Inquiries", to: "/seller/inquiries" },
        { label: "Profile", to: "/seller/profile" },
      ]}
    />
  );
};

export default SellerLayout;
