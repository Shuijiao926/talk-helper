'use client'
import { useParams } from "next/navigation";
import type { LocaleTypes } from "@/i18n/settings";
import { useTranslation } from "@/i18n/client";

export const Faq = () => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')

  const num = 10;

  const faqs = Array.from({ length: num }, (_, i) => i + 1);

  return (
    <section id="faq" className="flex flex-col items-center p-5">
      <div className="container-fluid">
        <div className="row gy-4">
          <div className="col-lg-7 d-flex flex-column justify-content-center align-items-stretch  order-2 order-lg-1">
            <div className="content px-xl-5">
              <h2 className="text-5xl text-center my-10 font-light dark:text-gray-100">
                Frequently Asked <strong>Questions</strong>
              </h2>
            </div>
            <div className="accordion accordion-flush px-xl-5" id="faqlist">
              {faqs.map((faq) => {
                const key = `faq_section.faqs.faq_${faq}.question`
                if (t(key) == key) return null; // 未定义的 FAQ 不显示
                return (
                  <div key={faq} className="accordion-item my-5" data-aos-delay={200}>
                    <h3 className="accordion-header font-bold text-gray-500 dark:text-gray-200">
                      <i className="bi bi-question-circle question-icon" />&nbsp;
                      {t(`faq_section.faqs.faq_${faq}.question`, { name: t('metadata.name') })}
                    </h3>
                    <div id={`faq-content-${faq}`} className="font-light dark:text-gray-300" data-bs-parent="#faqlist">
                      <div className="accordion-body">
                        {t(`faq_section.faqs.faq_${faq}.answer`, { name: t('metadata.name') })}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
