import { useLocation } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
import Navbar from "./common/components/Navbar";
import Footer from "./common/components/Footer";

function App() {
  const location = useLocation();
  const isProtectedLayoutRoute =
    location.pathname === "/admin" ||
    location.pathname.startsWith("/admin/") ||
    location.pathname === "/seller" ||
    location.pathname.startsWith("/seller/") ||
    location.pathname === "/buyer" ||
    location.pathname.startsWith("/buyer/");

  const isAuthRoute =
    location.pathname === "/login" ||
    location.pathname === "/register" ||
    location.pathname === "/verify-otp" ||
    location.pathname === "/forgot-password" ||
    location.pathname === "/reset-password" ||
    location.pathname === "/change-password";

  const showLayoutShell = !isProtectedLayoutRoute && !isAuthRoute;

  return (
    <div className="min-h-screen flex flex-col">
      {showLayoutShell && <Navbar />}
      <main className="flex-1">
        <AppRoutes />
      </main>
      {showLayoutShell && <Footer />}
    </div>
  );
}

export default App;