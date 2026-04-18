'use client'
import {useTranslation} from "@/i18n/client";
import {useParams} from "next/navigation";
import type {LocaleTypes} from "@/i18n/settings";


export const Introduction = () => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')

  return (
    <section id="game-intro" className="py-16 bg-gray-50 dark:bg-gray-800 dark:text-gray-100">
      <div className="container mx-auto px-4">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-6">{t("intro_section.headline", { name: t("metadata.name") })}</h2>
          <p className="text-xl mb-8">{t("intro_section.sub_headline", { name: t("metadata.name") })}</p>
          <p className="mb-8">{t("intro_section.description", { name: t("metadata.name") })}</p>
          <ul className="text-left list-disc list-inside mb-8">
            {Object.values(t("intro_section.key_points", { returnObjects: true })).map((point, index) => (
              <li key={index} className="mb-2">{point.replace('{{name}}', t("metadata.name"))}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  )
}
