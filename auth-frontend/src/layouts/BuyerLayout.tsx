import RoleLayout from "./RoleLayout";

const BuyerLayout = () => {
  return (
    <RoleLayout
      title="Buyer"
      subtitle="Property discovery"
      navItems={[
        { label: "Overview", to: "/buyer" },
        { label: "Wishlist", to: "/buyer/wishlist" },
        { label: "Profile", to: "/buyer/profile" },
      ]}
    />
  );
};

export default BuyerLayout;
