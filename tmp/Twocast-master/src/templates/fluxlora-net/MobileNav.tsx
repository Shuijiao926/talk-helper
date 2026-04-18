import React from 'react';
import Link from 'next/link';
import { X } from 'lucide-react';

interface MobileNavProps {
  isOpen: boolean;
  onClose: () => void;
}

const MobileNav: React.FC<MobileNavProps> = ({ isOpen, onClose }) => {
  return (
    <div
      role="dialog"
      aria-labelledby="mobile-menu"
      className={`fixed z-50 inset-y-0 right-0 w-[250px] sm:max-w-sm h-full bg-background p-6 shadow-lg border-l transition ease-in-out ${
        isOpen
          ? 'animate-in slide-in-from-right duration-500'
          : 'animate-out slide-out-to-right duration-300'
      }`}
    >
      <div className="flex flex-col items-start justify-center">
        <Link href="/" className="block px-3 py-2 text-lg font-bold">
          Home
        </Link>
        <Link href="/playground" className="block px-3 py-2 text-lg">
          Playground
        </Link>
        <Link href="/pricing" className="block px-3 py-2 text-lg">
          Pricing
        </Link>
        <Link href="/auth/login" className="block px-3 py-2 text-lg">
          Login
        </Link>
      </div>
      <button
        type="button"
        onClick={onClose}
        className="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
      >
        <X className="size-4" />
        <span className="sr-only">Close</span>
      </button>
    </div>
  );
};

export default MobileNav;
