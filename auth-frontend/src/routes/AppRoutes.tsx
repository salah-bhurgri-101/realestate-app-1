import { Routes, Route } from "react-router-dom";
import LoginPage from "../features/auth/pages/LoginPage";
import RegisterPage from "../features/auth/pages/RegisterPage";
import VerifyOtpPage from "../features/auth/pages/VerifyOtpPage";
import ChangePasswordPage from "../features/auth/pages/ChangePasswordPage";
import ForgotPasswordPage from "../features/auth/pages/ForgotPasswordPage";
import ResetPasswordPage from "../features/auth/pages/ResetPasswordPage";
import ProfilePage from "../features/profile/pages/ProfilePage";
import HomePage from "../pages/public/HomePage";

import Listings from "../pages/public/Listings";
import About from "../pages/public/About";
import Contact from "../pages/public/Contact";
import NotFoundPage from "../pages/public/NotFoundPage";

import RoleProtectedRoute from "./RoleProtectedRoute";

import BuyerDashboardPage from "../pages/buyer/BuyerDashboardPage";
import BuyerWishlistPage from "../pages/buyer/BuyerWishlistPage";
import BuyerProfilePage from "../pages/buyer/BuyerProfilePage";

import SellerDashboardPage from "../pages/seller/SellerDashboardPage";
import MyListingsPage from "../pages/seller/MyListingsPage";
import SellerInquiriesPage from "../pages/seller/SellerInquiriesPage";
import SellerProfilePage from "../pages/seller/SellerProfilePage";

import AdminDashboardPage from "../pages/admin/AdminDashboardPage";
import AdminUsersPage from "../pages/admin/AdminUsersPage";
import AdminSellersPage from "../pages/admin/AdminSellersPage";
import AdminPropertiesPage from "../pages/admin/AdminPropertiesPage";
import AdminCategoriesPage from "../pages/admin/AdminCategoriesPage";
import AdminAuditLogsPage from "../pages/admin/AdminAuditLogsPage";
import AdminSettingsPage from "../pages/admin/AdminSettingsPage";
import AdminProfilePage from "../pages/admin/AdminProfilePage";
import AuditLogCreatePage from "../pages/admin/AuditLogCreatePage";
import AuditLogDetailsPage from "../pages/admin/AuditLogDetailsPage";

import BuyerLayout from "../layouts/BuyerLayout";
import SellerLayout from "../layouts/SellerLayout";
import AdminLayout from "../layouts/AdminLayout";

import PropertyDetailsPage from "../pages/public/PropertyDetailsPage";
import PropertySearchPage from "../pages/public/PropertySearchPage";
import PropertyReviewPage from "../pages/public/PropertyReviewPage";
import SellerCreatePropertyPage from "../pages/seller/SellerCreatePropertyPage";
import SellerPropertyManagePage from "../pages/seller/SellerPropertyManagePage";
import BuyerFavoritesPage from "../pages/buyer/BuyerFavoritesPage";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/listings" element={<Listings />} />
      <Route path="/property/:id" element={<PropertyDetailsPage />} />
      <Route path="/search" element={<PropertySearchPage />} />
      <Route path="/property/:propertyId/review" element={<PropertyReviewPage />} />
      <Route path="/about" element={<About />} />
      <Route path="/contact" element={<Contact />} />

      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-otp" element={<VerifyOtpPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/change-password" element={<ChangePasswordPage />} />

      <Route path="/profile" element={<ProfilePage />} />

      <Route
        path="/buyer"
        element={
         <RoleProtectedRoute allowedRoles={["buyer", "user"]}>
           <BuyerLayout />
         </RoleProtectedRoute>
        }
      >
        <Route index element={<BuyerDashboardPage />} />
        <Route path="wishlist" element={<BuyerWishlistPage />} />
        <Route path="profile" element={<BuyerProfilePage />} />
        <Route path="favorites" element={<BuyerFavoritesPage />} />
      </Route>

      <Route
        path="/seller"
        element={
         <RoleProtectedRoute allowedRoles={["seller", "moderator","user"]}>
           <SellerLayout />
         </RoleProtectedRoute>
        }
      >
        <Route index element={<SellerDashboardPage />} />
        <Route path="add-property" element={<SellerCreatePropertyPage />} />
        <Route path="my-listings" element={<MyListingsPage />} />
        <Route path="property/:id/edit" element={<SellerPropertyManagePage />} />
        <Route path="inquiries" element={<SellerInquiriesPage />} />
        <Route path="profile" element={<SellerProfilePage />} />
      </Route>

      <Route
        path="/admin"
        element={
         <RoleProtectedRoute allowedRoles={["admin"]}>
           <AdminLayout />
          </RoleProtectedRoute>
        }
      >
        <Route index element={<AdminDashboardPage />} />
        <Route path="users" element={<AdminUsersPage />} />
        <Route path="sellers" element={<AdminSellersPage />} />
        <Route path="properties" element={<AdminPropertiesPage />} />
        <Route path="categories" element={<AdminCategoriesPage />} />
        <Route path="audit-logs" element={<AdminAuditLogsPage />} />
        <Route path="audit-logs/create" element={<AuditLogCreatePage />} />
        <Route path="audit-logs/create-async" element={<AuditLogCreatePage asyncMode />} />
        <Route path="audit-logs/:id" element={<AuditLogDetailsPage />} />
        <Route path="settings" element={<AdminSettingsPage />} />
        <Route path="profile" element={<AdminProfilePage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;