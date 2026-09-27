import { useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { resendOtpApi, verifyOtpApi } from "../../../api/authApi";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

const VerifyOtpPage = () => {
  const navigate = useNavigate();

  const [params] = useSearchParams();
  const [isResending, setIsResending] = useState(false);
  const [serverError, setServerError] = useState("");

  const email =
    params.get("email") || "";

  const { register, handleSubmit, formState: { isSubmitting } } =
    useForm();

  const onSubmit = async (
    data: any
  ) => {
    try {
      setServerError("");
      await verifyOtpApi({
        email,
        code: data.code,
      });
      navigate("/login");
    } catch (error: any) {
      setServerError(
        error?.response?.data?.message ||
          "Failed to verify OTP"
      );
    }
  };

  const resendOtp = async () => {
    try {
      setIsResending(true);
      setServerError("");
      await resendOtpApi({
        email,
      });
      alert("OTP sent successfully!");
    } catch (error: any) {
      setServerError(
        error?.response?.data?.message ||
          "Failed to resend OTP"
      );
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="auth-promo-shell min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md overflow-hidden rounded-[2rem] border border-white/10 bg-white shadow-2xl">
        {/* Header Section */}
        <div className="bg-slate-900 px-6 py-10 text-center">
          <p className="mb-4 text-xs font-bold uppercase tracking-[0.25em] text-amber-300">Secure verification</p>
          <h1 className="mb-2 text-3xl font-bold text-white">
            Verify OTP
          </h1>
          <p className="text-blue-100">
            Enter the OTP sent to your email
          </p>
        </div>

        {/* Form Section */}
        <div className="px-6 py-8 space-y-5">
          {/* Email Display */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-xs font-medium text-gray-600">
              Verification code sent to
            </p>
            <p className="text-lg font-semibold text-gray-800 break-all">
              {email}
            </p>
          </div>

          {/* Server Error */}
          {serverError && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded">
              <p className="text-red-700 text-sm font-medium">
                {serverError}
              </p>
            </div>
          )}

          {/* OTP Form */}
          <form
            onSubmit={handleSubmit(onSubmit)}
            className="space-y-5"
          >
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Enter OTP
              </label>
              <input
                {...register("code")}
                placeholder="000000"
                className="w-full px-4 py-3 text-center text-2xl tracking-widest border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition font-mono"
                maxLength={6}
              />
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full rounded-xl bg-slate-950 py-3.5 font-semibold text-white transition hover:-translate-y-0.5 hover:bg-slate-800 disabled:opacity-50"
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
                  Verifying...
                </span>
              ) : (
                "Verify OTP"
              )}
            </button>
          </form>
        </div>

        {/* Footer */}
        <div className="bg-gray-50 px-6 py-4 text-center border-t border-gray-200">
          <p className="text-gray-600 text-sm mb-3">
            Didn't receive the code?
          </p>
          <button
            type="button"
            onClick={resendOtp}
            disabled={isResending}
            className="w-full bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium py-2 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isResending ? "Sending..." : "Resend OTP"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default VerifyOtpPage;