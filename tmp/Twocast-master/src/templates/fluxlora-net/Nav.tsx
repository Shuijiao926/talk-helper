import React from 'react';
import Link from 'next/link';
import LangSwitch from './LangSwitch';

// New MobileMenuButton component
const MobileMenuButton: React.FC = () => (
  <button
    className="inline-flex items-center justify-center text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input bg-transparent text-primary hover:bg-primary/10 h-9 w-9 rounded-lg md:hidden"
    aria-label="Menu"
    type="button"
    aria-haspopup="dialog"
    aria-expanded="false"
    aria-controls="radix-:r34:"
    data-state="closed"
  >
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-menu size-4">
      <line x1="4" x2="20" y1="12" y2="12"></line>
      <line x1="4" x2="20" y1="6" y2="6"></line>
      <line x1="4" x2="20" y1="18" y2="18"></line>
    </svg>
  </button>
);

const Nav: React.FC = () => {
    return (
        <nav className="bg-background/80 fixed left-0 top-0 z-20 w-full backdrop-blur-lg shadow-none transition-[box-shadow] duration-200"
            data-test="navigation">
            <div className="bg-primary/10 text-foreground relative inset-0 bottom-auto px-8 py-3 text-center text-sm data-[state='open']:block data-[state='closed']:hidden"
                data-test="banner">
                <div><strong>New:</strong> Flux Lora Online</div>
                <button
                    className="inline-flex items-center justify-center text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 text-primary underline-offset-4 hover:underline h-9 rounded-lg px-4 py-2 absolute right-1 top-1"
                    aria-label="Hide banner">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                        viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                        strokeLinejoin="round" className="lucide lucide-x size-4">
                        <path d="M18 6 6 18"></path>
                        <path d="m6 6 12 12"></path>
                    </svg>
                </button>
            </div>
            <div className="container">
                <div className="flex items-center justify-stretch gap-6 py-8 transition-[padding] duration-200">
                    <div className="flex flex-1 justify-start">
                        <Link href="/" className="block hover:no-underline active:no-underline">
                            <span className="flex items-center">
                                <img src="https://cdn.fluxlora.net/fluxloralogo.jpg"
                                    alt="Flux Lora" width={40} height={40} className="rounded-full" />
                                <span className="ml-3 text-lg">Flux Lora</span>
                            </span>
                        </Link>
                    </div>
                    <div className="hidden flex-1 items-center justify-center md:flex">
                        <Link href="/" className="block px-3 py-2 text-lg font-bold">Home</Link>
                        <Link href="/playground" className="block px-3 py-2 text-lg">Playground</Link>
                        <Link href="/pricing" className="block px-3 py-2 text-lg">Pricing</Link>
                    </div>
                    <div className="flex flex-1 items-center justify-end gap-3">
                        {/* Color mode toggle button */}
                        {/* Language toggle button */}
                        <LangSwitch />
                        {/* Mobile menu button */}
                        <MobileMenuButton />
                        <Link href="/auth/login"
                            className="items-center justify-center text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 text-primary hover:bg-primary/10 hover:text-primary h-9 rounded-lg px-4 py-2 hidden md:block">
                            Login
                        </Link>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Nav;
