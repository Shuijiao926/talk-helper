"use client";

import SocialIcon from "@/components/social-icons";
import siteMetadata from "@/data/siteMetadata";
import Link from "./mdxcomponents/Link";

import { useTranslation } from "@/i18n/client";
import { localeOptions, LocaleTypes } from "@/i18n/settings";
import { useParams } from "next/navigation";

import { getLocalePath } from "@/utils/locale-util";

export default function Footer() {
  const locale = useParams()?.locale as LocaleTypes;
  const { t } = useTranslation(locale, "home");

  return (
    <>
      <footer>
        <div className="mt-16 flex flex-col items-center">
          <div className="mb-3 flex space-x-4">
            <div className="flex items-center">
              {siteMetadata.formspree === false ? (
                <SocialIcon kind="mail" href={`mailto:${siteMetadata.email}`} size={6} />
              ) : (
                <button className="flex items-center focus:outline-none">
                  <SocialIcon kind="mail" size={6} />
                </button>
              )}
            </div>
            <div className="flex items-center">
              <SocialIcon kind="github" href={siteMetadata.github} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="facebook" href={siteMetadata.facebook} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="youtube" href={siteMetadata.youtube} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="linkedin" href={siteMetadata.linkedin} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="x" href={siteMetadata.x} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="instagram" href={siteMetadata.instagram} size={6} />
            </div>
            <div className="flex items-center">
              <SocialIcon kind="threads" href={siteMetadata.threads} size={6} />
            </div>
          </div>


          <div className="flex items-center space-x-2 flex-wrap justify-center text-gray-500 dark:text-gray-400">
            {localeOptions.map(opt => {
              return <span key={opt.lang} className="mx-1"><a href={`/${opt.lang}`}>{opt.label}</a>&nbsp;</span>;
            })}
          </div>

          <div className="flex items-center space-x-2 flex-wrap text-gray-500 dark:text-gray-400 justify-center text-sm bg-gray-100 dark:bg-gray-800 p-2">
            {/* 友链放这里 */}
          </div>

          <div className="my-2 flex space-x-2 text-sm text-gray-500 dark:text-gray-400 flex-col md:flex-row justify-center items-center">
            {/*<div>{siteMetadata.author}</div>*/}
            {/*<div>{` • `}</div>*/}
            <div>{`© ${new Date().getFullYear()}`}</div>
            <div>{` • `}</div>
            <Link href="/">{t("metadata.title", { name: t("metadata.name") })}</Link>
            <div>{` • `}</div>
            <div>Contact: {process.env.NEXT_PUBLIC_CONTACT_EMAIL}</div>
          </div>
        </div>
      </footer>
    </>
  );
}
