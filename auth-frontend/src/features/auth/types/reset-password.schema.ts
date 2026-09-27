import { z } from "zod";

export const resetPasswordSchema = z
  .object({
    email: z
      .string()
      .email("Invalid email address"),
    otp: z
      .string()
      .length(6, "OTP must be 6 digits"),
    newPassword: z
      .string()
      .min(8, "Password must be at least 8 characters"),
    confirmPassword: z
      .string()
      .min(1, "Please confirm your password"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

export type ResetPasswordFormData = z.infer<
  typeof resetPasswordSchema
>;
