'use client'
import {useTranslation} from "@/i18n/client";
import {useParams} from "next/navigation";
import type {LocaleTypes} from "@/i18n/settings";
import type { ReactNode } from 'react'


export const Explore = ({ children }: { children?: ReactNode }) => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')

  return (
    <section id="explore" className="relative bg-white dark:bg-transparent py-16 sm:py-24">
      <div className="absolute inset-x-0 top-0 h-48 bg-gradient-to-b from-blue-100 dark:from-blue-900/20 to-transparent"></div>
      <div className="relative container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center">
          <h2 className="text-4xl font-extrabold tracking-tight text-gray-900 dark:text-white sm:text-5xl lg:text-6xl">
            {t('explore_section.headline', {name: t('metadata.name')})}
          </h2>
          <p className="mt-4 max-w-2xl mx-auto text-xl text-gray-500 dark:text-gray-400">
            {t('explore_section.sub_headline', {name: t('metadata.name')})}
          </p>
        </div>

        <div className="mt-12">{children}</div>
      </div>
    </section>
  )
}
