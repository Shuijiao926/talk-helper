'use client'
import {useParams} from "next/navigation";
import type {LocaleTypes} from "@/i18n/settings";
import {useTranslation} from "@/i18n/client";

export const HowTo = () => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')

  return (
    <section id="how-to-use" className="flex flex-col items-center px-4 my-16">
      <div className="container">

        <div className="section-header">
          <h2 className="text-5xl text-center my-10 font-light dark:text-gray-100">{t('how_to_use_section.headline', {name: t('metadata.name')})}</h2>
          <p className="text-gray-400 text-center my-8">{t('how_to_use_section.sub_headline', {name: t('metadata.name')})}</p>
        </div>

        <div className="row g-4 g-lg-5" data-aos-delay="200">

          <div className="flex flex-col items-center">
            <ol className="list-decimal p-[revert]">
              <li className="font-bold dark:text-gray-200">{t("how_to_use_section.steps.step_1", {name: t('metadata.name')})}</li>
              <li className="font-bold dark:text-gray-200">{t("how_to_use_section.steps.step_2", {name: t('metadata.name')})}</li>
              <li className="font-bold dark:text-gray-200">{t("how_to_use_section.steps.step_3", {name: t('metadata.name')})}</li>
              <li className="font-bold dark:text-gray-200 hidden">{t("how_to_use_section.steps.step_4", {name: t('metadata.name')})}</li>
            </ol>
          </div>
        </div>
      </div>
    </section>
  )
}
