import type { InitOptions } from 'i18next'
import { fallbackLng } from './locales'
import localesJson from './locales.json'

export const locales = localesJson.map((locale) => locale.lang)
export type LocaleTypes = (typeof locales)[number]
export const defaultNS = 'common'
export const localeOptions = localesJson
export const cookieName = 'i18n'

export function getOptions(locale = fallbackLng, ns = defaultNS): InitOptions {
  return {
    debug: false,
    supportedLngs: locales,
    fallbackLng,
    lng: locale,
    fallbackNS: defaultNS,
    defaultNS,
    ns,
  }
}
