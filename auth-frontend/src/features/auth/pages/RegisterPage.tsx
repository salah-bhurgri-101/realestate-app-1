import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  registerSchema,
  type RegisterFormData,
} from "../types/register.schema";

import { registerApi } from "../../../api/authApi";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

const RegisterPage = () => {
  const navigate = useNavigate();
  const [serverError, setServerError] =
    useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (
    data: RegisterFormData
  ) => {
    try {
      setServerError("");
      const payload = Object.fromEntries(
        Object.entries(data).filter(([, value]) => value !== ""),
      );
      await registerApi(payload);

      navigate(
        `/verify-otp?email=${encodeURIComponent(
          data.email
        )}`
      );
    } catch (error: any) {
      setServerError(
        error?.response?.data?.message ||
          "Registration failed"
      );
    }
  };

  return (
    <div className="auth-promo-shell relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-12 sm:px-6 lg:px-8">
      <div className="absolute -left-32 -top-32 h-96 w-96 rounded-full bg-amber-400/20 blur-3xl" />
      <div className="relative w-full max-w-md overflow-hidden rounded-[2rem] border border-white/10 bg-white shadow-2xl shadow-black/30">
        {/* Header Section */}
        <div className="bg-slate-900 px-6 py-10 text-center">
          <p className="mb-4 text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Join Aurelia</p>
          <h1 className="mb-2 text-3xl font-bold text-white">
            Create Account
          </h1>
          <p className="text-blue-100">
            Join us today and get started
          </p>
          <p className="mx-auto mt-4 max-w-xs text-xs leading-5 text-slate-300">Join a refined property community built for confident buyers and ambitious sellers.</p>
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

          {/* Name and Phone Row */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Full Name
              </label>
              <input
                type="text"
                placeholder="John Doe"
                {...register("name")}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
                  errors.name
                    ? "border-red-500"
                    : "border-gray-300"
                }`}
              />
              {errors.name && (
                <p className="text-red-500 text-xs mt-1">
                  {errors.name.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Phone Number
              </label>
              <input
                type="tel"
                placeholder="+1 (555) 000-0000"
                {...register("phone")}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
                  errors.phone
                    ? "border-red-500"
                    : "border-gray-300"
                }`}
              />
              {errors.phone && (
                <p className="text-red-500 text-xs mt-1">
                  {errors.phone.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Bio (optional)</label>
              <textarea
                placeholder="Tell us a little about yourself"
                {...register("bio")}
                className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
              />
              {errors.bio && <p className="text-red-500 text-xs mt-1">{errors.bio.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Date of birth (optional)</label>
                <input type="date" {...register("dateOfBirth")} className="w-full rounded-lg border border-gray-300 px-4 py-2" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Gender (optional)</label>
                <input type="text" placeholder="Optional" {...register("gender")} className="w-full rounded-lg border border-gray-300 px-4 py-2" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Avatar URL (optional)</label>
                <input type="url" placeholder="https://..." {...register("avatarUrl")} className="w-full rounded-lg border border-gray-300 px-4 py-2" />
                {errors.avatarUrl && <p className="text-red-500 text-xs mt-1">{errors.avatarUrl.message}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Cover URL (optional)</label>
                <input type="url" placeholder="https://..." {...register("coverUrl")} className="w-full rounded-lg border border-gray-300 px-4 py-2" />
                {errors.coverUrl && <p className="text-red-500 text-xs mt-1">{errors.coverUrl.message}</p>}
              </div>
            </div>
          </div>

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
                Creating Account...
              </span>
            ) : (
              "Create Account"
            )}
          </button>
        </form>

        {/* Footer */}
        <div className="bg-gray-50 px-6 py-4 text-center border-t border-gray-200">
          <p className="text-gray-600 text-sm">
            Already have an account?{" "}
            <a
              href="/login"
              className="text-blue-600 font-semibold hover:text-blue-700 transition"
            >
              Login here
            </a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;