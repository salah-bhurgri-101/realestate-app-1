import type React from 'react';

export { default as Navbar } from "./Navbar";
export { default as Footer } from "./Footer";
export { default as PropertyCard } from "./PropertyCard";

// Minimal SearchFilter component
interface SearchFilterProps {
  value?: string;
  onChange?: (value: string) => void;
}

export const SearchFilter = ({ value = "", onChange }: SearchFilterProps) => {
  return (
    <div className="flex items-center gap-2">
      <input
        aria-label="Search"
        className="px-3 py-2 border rounded w-full"
        placeholder="Search properties..."
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
      />
    </div>
  );
};

// Minimal Modal component
interface ModalProps {
  open: boolean;
  onClose?: () => void;
  title?: string;
  children?: React.ReactNode;
}

export const Modal = ({ open, onClose, title, children }: ModalProps) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black opacity-40" onClick={onClose} />
      <div className="bg-white rounded shadow-lg z-10 max-w-lg w-full p-4">
        {title && <h3 className="text-lg font-semibold mb-2">{title}</h3>}
        <div>{children}</div>
        <div className="mt-4 text-right">
          <button onClick={onClose} className="px-3 py-2 rounded border">
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

// Minimal Loader component
export const Loader = ({ size = 24 }: { size?: number }) => {
  return (
    <div className="flex items-center justify-center">
      <svg
        className="animate-spin text-indigo-600"
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" opacity="0.25" />
        <path
          d="M22 12a10 10 0 00-10-10"
          stroke="currentColor"
          strokeWidth="4"
          strokeLinecap="round"
        />
      </svg>
    </div>
  );
};
