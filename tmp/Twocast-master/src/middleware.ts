import { NextResponse, NextRequest, NextFetchEvent } from "next/server";
import { cookieName, locales } from "@/i18n/settings";
import { fallbackLng } from "@/i18n/locales";
import acceptLanguage from "accept-language";
import { clerkMiddleware, createRouteMatcher } from "@clerk/nextjs/server";

acceptLanguage.languages(locales);

async function getStdResponse(request: NextRequest) {
  const { pathname, search } = request.nextUrl

  // 默认语言重定向: /en/about?foo=bar -> /about?foo=bar
  if (pathname.startsWith(`/${fallbackLng}/`) || pathname === `/${fallbackLng}`) {
    const newPathname = pathname.replace(`/${fallbackLng}`, pathname === `/${fallbackLng}` ? '/' : '')
    const newUrl = new URL(`${newPathname}${search}`, request.url)
    return NextResponse.redirect(newUrl)
  }

  const pathnameIsMissingLocale = locales.every(
    (locale) => !pathname.startsWith(`/${locale}/`) && pathname !== `/${locale}`
  )

  if (pathnameIsMissingLocale) {
    // console.log('[i18nMid] no Locale.')
    // e.g. incoming request is /about?foo=bar
    // Tell Next.js it should pretend it's /en/about?foo=bar
    const newUrl = new URL(`/${fallbackLng}${pathname}${search}`, request.url)
    return NextResponse.rewrite(newUrl)
  }
  return NextResponse.next()
}

function parseRequest(request: NextRequest) {
  let pathname = request.nextUrl.pathname;
  let lang = fallbackLng
  for (const locale of locales) {
    if (pathname.startsWith(`/${locale}/`)) {
      lang = locale;
      pathname = pathname.replace(`/${locale}`, '');
      break;
    }
  }
  return { pathname, lang };
}

async function i18nMid(request: NextRequest) {
  // Check if there is any supported locale in the pathname
  const u = new URL(request.url);
  if (u.pathname.startsWith("/api")) {
    // console.log('.......1-1')
    return NextResponse.next();
  }
  if (isPublicRoute(request)) {
    return getStdResponse(request);
  }
}

export const config = {
  // Do not run the middleware on the following paths
  // prettier-ignore
  matcher:
    "/((?!static|track|data|css|scripts|assets|_next|sign-in|sign-up|sign-out|search.json|ads.txt).*|sitemap.xml)"
};


/************ clerk ************/
const isPublicRoute = createRouteMatcher([
  "/",
  "/([^/]{2})",
  "(/[a-z]{2})?/podcast/(.*)",
  "/api/voices",
  "/api/get-user-info",
]);

async function clerkMid(request: NextRequest, event: NextFetchEvent) {
    return clerkMiddleware(async (auth, req) => {
      const u = new URL(req.url);
      console.log("[clerkMid]......isPublicRoute", isPublicRoute(req), req.url, u.pathname);
      const { userId } = await auth();
      if (isPublicRoute(req)) {
        // console.log('.......1')
        if (u.pathname.startsWith("/api")) {
          // console.log('.......1-1')
          return NextResponse.next();
        }
        // console.log('.......1-2')
        return i18nMid(req);
      }
      if (!userId) {
        // console.log('.......2')
        if (u.pathname.startsWith("/api")) {
          return NextResponse.json(
            { code: -2, message: "no auth" },
            { status: 401 }
          );
        }
        // console.log('.......2-1')
        return NextResponse.redirect(new URL("/sign-in", req.url));
      }
      // console.log('.......3')
    })(request, event);
}

export async function middleware(request: NextRequest, event: NextFetchEvent) {
  if (process.env.NEXT_PUBLIC_CLERK_ENABLED) {
    return clerkMid(request, event)
  }
  return i18nMid(request);
}