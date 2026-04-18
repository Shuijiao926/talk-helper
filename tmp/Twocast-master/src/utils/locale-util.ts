import { fallbackLng } from "@/i18n/locales";
import type { LocaleTypes } from "@/i18n/settings";

export function getLocalePath(locale: LocaleTypes, pathname: string) {
  if (!pathname.startsWith('/')) {
    return pathname
  }
  if (locale === fallbackLng) {
    return pathname
  }
  return `/${locale}${pathname}`
}