import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loginSchema, type LoginFormData } from "../types/login.schema";
import { loginApi } from "../../../api/authApi";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuthStore } from "../store/authStore";

const LoginPage = () => {
  const navigate = useNavigate();

  const { setTokens, setUser } = useAuthStore();

  const [serverError, setServerError] =
    useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (
    data: LoginFormData
  ) => {
    try {
      setServerError("");

      const response = await loginApi(data);

      const authData = response?.data ?? response;

      setTokens(
        authData.accessToken,
        authData.refreshToken
      );

      if (authData.user) {
        setUser(authData.user);
      }

      // Redirect based on user role (Admin -> admin, Moderator/Seller -> seller, others -> buyer)
      const role = authData?.user?.role?.toLowerCase()?.replace(/^role_/, '');
      if (role === "admin") {
        navigate("/admin");
      } else if (role === "moderator" || role === "seller") {
        navigate("/seller");
      } else {
        navigate("/buyer");
      }
    } catch (error: any) {
      setServerError(
        error?.response?.data?.message ||
          "Login failed"
      );
    }
  };

  return (
    <div className="auth-promo-shell relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-12 sm:px-6 lg:px-8">
      <div className="absolute -left-32 -top-32 h-96 w-96 rounded-full bg-amber-400/20 blur-3xl" />
      <div className="absolute -bottom-40 -right-20 h-96 w-96 rounded-full bg-indigo-500/20 blur-3xl" />
      <div className="relative w-full max-w-md overflow-hidden rounded-[2rem] border border-white/10 bg-white shadow-2xl shadow-black/30">
        {/* Header Section */}
        <div className="bg-slate-900 px-6 py-10 text-center">
          <p className="mb-4 text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Aurelia private access</p>
          <h1 className="mb-2 text-3xl font-bold text-white">
            Welcome Back
          </h1>
          <p className="text-blue-100">
            Login to your account
          </p>
          <p className="mx-auto mt-4 max-w-xs text-xs leading-5 text-slate-300">Discover considered spaces, trusted listings, and a better way to move forward.</p>
        </div>

        {/* Form Section */}
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="px-6 py-8 space-y-5"
        >
          {/* Server Error */}
          {serverError && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded">
              <p className="text-red-700 text-sm font-medium">
                {serverError}
              </p>
            </div>
          )}

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email Address
            </label>

            <input
              type="email"
              placeholder="john@example.com"
              {...register("email")}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
                errors.email
                  ? "border-red-500"
                  : "border-gray-300"
              }`}
            />

            {errors.email && (
              <p className="text-red-500 text-xs mt-1">
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>

            <input
              type="password"
              placeholder="••••••••"
              {...register("password")}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
                errors.password
                  ? "border-red-500"
                  : "border-gray-300"
              }`}
            />

            {errors.password && (
              <p className="text-red-500 text-xs mt-1">
                {errors.password.message}
              </p>
            )}
          </div>

          {/* Remember & Forgot */}
          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center text-gray-700">
              <input
                type="checkbox"
                className="mr-2 w-4 h-4 rounded border-gray-300"
              />
              Remember me
            </label>
            <a
              href="/forgot-password"
              className="text-blue-600 hover:text-blue-700 font-medium"
            >
              Forgot password?
            </a>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-xl bg-slate-950 py-3.5 font-semibold text-white shadow-xl shadow-slate-900/20 transition hover:-translate-y-0.5 hover:bg-slate-800 disabled:opacity-50"
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center">
                <svg
                  className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Logging In...
              </span>
            ) : (
              "Login"
            )}
          </button>
        </form>

        {/* Footer */}
        <div className="bg-gray-50 px-6 py-4 text-center border-t border-gray-200">
          <p className="text-gray-600 text-sm">
            Don't have an account?{" "}
            <a
              href="/register"
              className="text-blue-600 font-semibold hover:text-blue-700 transition"
            >
              Register here
            </a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;