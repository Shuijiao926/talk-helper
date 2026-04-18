'use client'
import {useParams} from "next/navigation";
import type {LocaleTypes} from "@/i18n/settings";
import {useTranslation} from "@/i18n/client";

export const Features = () => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')

  return (
    <section id="features" className="flex flex-col items-center px-4 bg-gray-50 dark:bg-gray-800">
      <div className="container">
        <div className="section-header">
          <h2 className="text-5xl text-center my-10 dark:text-gray-100">{t('feature_section.headline', {name: t('metadata.name')})}</h2>
          <p className="text-gray-400 dark:text-gray-200 text-center my-8">{t('feature_section.sub_headline', {name: t('metadata.name')})}</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4">
          <div className="flex">
            <div className="p-6">
              <div className="icon"><i className="bi bi-activity icon"/></div>
              <h4 className="mb-4 font-bold text-xl text-gray-500 dark:text-gray-100"><a className="stretched-link">{t('feature_section.features.feature_1.title', {name: t('metadata.name')})}</a></h4>
              <p className="font-light dark:text-gray-200">{t('feature_section.features.feature_1.description', {name: t('metadata.name')})}</p>
            </div>
          </div>
          <div className="flex">
            <div className="p-6">
              <div className="icon"><i className="bi bi-bounding-box-circles icon"/></div>
              <h4 className="mb-4 font-bold text-xl text-gray-500 dark:text-gray-100"><a className="stretched-link">{t('feature_section.features.feature_2.title', {name: t('metadata.name')})}</a></h4>
              <p className="font-light dark:text-gray-200">{t('feature_section.features.feature_2.description', {name: t('metadata.name')})}
              </p>
            </div>
          </div>
          <div className="flex">
            <div className="p-6">
              <div className="icon"><i className="bi bi-calendar4-week icon"/></div>
              <h4 className="mb-4 font-bold text-xl text-gray-500 dark:text-gray-100"><a className="stretched-link">{t('feature_section.features.feature_3.title', {name: t('metadata.name')})}</a></h4>
              <p className="font-light dark:text-gray-200">{t('feature_section.features.feature_3.description', {name: t('metadata.name')})}</p>
            </div>
          </div>
          <div className="flex">
            <div className="p-6">
              <div className="icon"><i className="bi bi-broadcast icon"/></div>
              <h4 className="mb-4 font-bold text-xl text-gray-500 dark:text-gray-100"><a className="stretched-link">{t('feature_section.features.feature_4.title', {name: t('metadata.name')})}</a></h4>
              <p className="font-light dark:text-gray-200">{t('feature_section.features.feature_4.description', {name: t('metadata.name')})}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
