'use client'
import { useParams } from "next/navigation";
import type { LocaleTypes } from "@/i18n/settings";
import { useTranslation } from "@/i18n/client";
import { Zap, Layers, Cpu, Sparkles } from 'lucide-react'; // 导入图标
import { getLocalePath } from "@/utils/locale-util";

export default function Main() {
    const locale = useParams()?.locale as LocaleTypes;
    const { t } = useTranslation(locale, "home");

    // 定义图标映射
    const featureIcons = [Zap, Layers, Cpu, Sparkles];

    const loraCards = [
        {
            title: "",
            description: "",
            assetUrl: "/assets/site/explore/01.mp4",
            assetType: "video"
        },
        {
            title: "",
            description: "",
            assetUrl: "/assets/site/explore/02.mp4",
            assetType: "video"
        },
        {
            title: "",
            description: "",
            assetUrl: "/assets/site/explore/03.mp4",
            assetType: "video"
        }
    ];
    return (
        <main className="min-h-screen">
            <nav className="to-primary/5 border-b bg-gradient-to-b from-transparent pb-20 pt-8">
                <div className="container text-center">
                    <h1 className="mx-auto max-w-3xl text-5xl font-bold lg:text-7xl">{t('metadata.name')}</h1>
                    <p className="mt-4 text-lg opacity-75">{t("hero_section.sub_headline", {name: t('metadata.name')})}</p>
                    <div className="mt-6 flex flex-col items-center justify-center gap-3 md:flex-row">
                        <a className="inline-flex items-center justify-center font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 rounded-xl px-8 text-base" href={getLocalePath(locale, '/playground')}>
                            {t("hero_section.cta_button")}
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-arrow-right ml-2 size-4">
                                <path d="M5 12h14"></path>
                                <path d="m12 5 7 7-7 7"></path>
                            </svg>
                        </a>
                    </div>
                </div>
            </nav>

            {/* Awesome FLUX LoRA section */}
            <section className="py-16">
                <div className="container">
                    <div className="text-center mb-20">
                        <h2 className="text-4xl font-bold lg:text-5xl">{t("feature_section.headline", { name: t("metadata.name") })}</h2>
                        <p className="mt-3 text-lg opacity-70">{t("feature_section.sub_headline", { name: t("metadata.name") })}</p>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {loraCards.map((card, index) => (
                            <div key={index} className="bg-card/90 text-card-foreground rounded-lg shadow-sm backdrop-blur-md overflow-hidden flex flex-col h-full">
                                <div className="relative aspect-w-16 aspect-h-9">
                                    {card.assetType === "image" ? (
                                        <img src={card.assetUrl} alt={card.title} className="object-cover w-full h-full" />
                                    ) : card.assetType === "video" ? (
                                        <video src={card.assetUrl} className="object-cover w-full h-full" autoPlay loop muted />
                                    ) : null}
                                </div>
                                <div className="flex flex-col flex-grow">
                                    <div className="flex flex-col space-y-1.5 p-6 pb-4">
                                        <h3 className="text-xl font-semibold leading-none">{card.title}</h3>
                                    </div>
                                    <div className="p-6 pt-0 flex-grow">
                                        <p className="text-muted-foreground text-sm">{card.description}</p>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Features section */}
            <section className="bg-card text-card-foreground py-24">
                <div className="container">
                    <div className="text-center">
                        <h2 className="text-4xl font-bold lg:text-5xl">{t("feature_section.headline", { name: t("metadata.name") })}</h2>
                        <p className="mt-3 text-lg opacity-70">{t("feature_section.sub_headline", { name: t("metadata.name") })}</p>
                    </div>
                    <div className="mt-20 grid grid-cols-1 gap-16">
                        {Object.entries(t("feature_section.features", { returnObjects: true })).map(([key, feature], index) => {
                            const IconComponent = featureIcons[index] || Zap;
                            return (
                                <div key={key} className="grid items-center gap-8 lg:grid-cols-2 lg:gap-16">
                                    <div className={`bg-primary/10 rounded-2xl p-12 flex items-center justify-center ${index % 2 === 0 ? '' : 'lg:order-1'}`}>
                                        <IconComponent className="w-24 h-24 text-primary" />
                                    </div>
                                    <div>
                                        <h3 className="text-3xl font-bold">{feature.title}</h3>
                                        <p className="mt-2 leading-normal opacity-70">{feature.description}</p>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </section>

            {/* FAQ section */}
            <section className="bg-card text-card-foreground py-24">
                <div className="container max-w-4xl">
                    <h2 className="text-4xl font-bold text-center mb-12">FAQ</h2>
                    <dl className="mt-10 space-y-8 divide-y divide-gray-900/10">
                        {Array.from({ length: 9 }, (_, i) => i + 1).map((faq) => (
                            <div key={faq} className="pt-8 lg:grid lg:grid-cols-12 lg:gap-8">
                                <dt className="text-base font-semibold leading-7 text-gray-900 lg:col-span-5">
                                    {t(`faq_section.faqs.faq_${faq}.question`, { name: t('metadata.name') })}
                                </dt>
                                <dd className="mt-4 lg:col-span-7 lg:mt-0">
                                    <p className="text-base leading-7 text-gray-600">
                                        {t(`faq_section.faqs.faq_${faq}.answer`, { name: t('metadata.name') })}
                                    </p>
                                </dd>
                            </div>
                        ))}
                    </dl>
                </div>
            </section>
        </main>
    );
}

