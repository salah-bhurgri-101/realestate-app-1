import { z } from "zod";

export const registerSchema = z.object({
  name: z.string().min(2),
  email: z.email(),
  password: z.string().min(8),
  phone: z.string().regex(/^\+?[0-9]{10,15}$/, "Enter a valid phone number"),
  bio: z.string().max(2000).optional(),
  avatarUrl: z.string().url().or(z.literal("")).optional(),
  coverUrl: z.string().url().or(z.literal("")).optional(),
  dateOfBirth: z.string().optional(),
  gender: z.string().max(20).optional(),
});

export type RegisterFormData =
  z.infer<typeof registerSchema>;