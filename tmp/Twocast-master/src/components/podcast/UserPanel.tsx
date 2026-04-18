"use client";

import SocialIcon from "../social-icons";
import { ListPanel } from "./ListPanel";
import { UserInput } from "./UserInput";
import { useState } from "react";
import { useTranslation } from "react-i18next";

export function UserPanel() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const { t } = useTranslation('home');

  const handleSubmitSuccess = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  const handleGitHubClick = () => {
    window.open('https://github.com/panyanyany/Twocast', '_blank', 'noopener,noreferrer');
  };

  return (
    <section
      className="py-10 sm:py-20 relative bg-gradient-to-br from-white/70 via-pink-50/50 to-blue-50/70 dark:from-gray-800/70 dark:via-pink-900/30 dark:to-indigo-950/70 flex items-center"
      style={{
        boxShadow: '0 32px 64px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.4)'
      }}
    >
      {/* SVG 背景光晕效果 */}
      <svg
        className="absolute inset-0 w-full h-full pointer-events-none"
        style={{
          zIndex: -1,
        }}
        viewBox="0 0 100 100"
        preserveAspectRatio="none"
      >
        <defs>
          {/* 蓝色光晕渐变 */}
          <radialGradient id="blueGlow" cx="0.8" cy="0.2" r="0.6">
            <stop offset="0%" stopColor="rgb(96 165 250)" stopOpacity="0.3" />
            <stop offset="30%" stopColor="rgb(99 102 241)" stopOpacity="0.2" />
            <stop offset="70%" stopColor="rgb(79 70 229)" stopOpacity="0.1" />
            <stop offset="100%" stopColor="rgb(67 56 202)" stopOpacity="0" />
          </radialGradient>

          {/* 紫色光晕渐变 */}
          <radialGradient id="purpleGlow" cx="0.2" cy="0.8" r="0.6">
            <stop offset="0%" stopColor="rgb(196 181 253)" stopOpacity="0.25" />
            <stop offset="30%" stopColor="rgb(168 85 247)" stopOpacity="0.2" />
            <stop offset="70%" stopColor="rgb(147 51 234)" stopOpacity="0.15" />
            <stop offset="100%" stopColor="rgb(126 34 206)" stopOpacity="0" />
          </radialGradient>

          {/* 粉色光晕渐变 */}
          <radialGradient id="pinkGlow" cx="0.1" cy="0.9" r="0.5">
            <stop offset="0%" stopColor="rgb(244 114 182)" stopOpacity="0.3" />
            <stop offset="50%" stopColor="rgb(236 72 153)" stopOpacity="0.2" />
            <stop offset="100%" stopColor="rgb(219 39 119)" stopOpacity="0" />
          </radialGradient>

          {/* 高斯模糊滤镜 */}
          <filter id="blur" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur in="SourceGraphic" stdDeviation="8" />
          </filter>
        </defs>

        {/* 蓝色光晕圆 */}
        <circle
          cx="85"
          cy="15"
          r="35"
          fill="url(#blueGlow)"
          filter="url(#blur)"
          className="animate-pulse"
          style={{ animationDuration: '4s' }}
        />

        {/* 紫色光晕圆 */}
        <circle
          cx="15"
          cy="85"
          r="40"
          fill="url(#purpleGlow)"
          filter="url(#blur)"
          className="animate-pulse"
          style={{ animationDuration: '5s', animationDelay: '1s' }}
        />

        {/* 粉色光晕圆 */}
        <circle
          cx="10"
          cy="90"
          r="35"
          fill="url(#pinkGlow)"
          filter="url(#blur)"
          className="animate-pulse"
          style={{ animationDuration: '6s', animationDelay: '2s' }}
        />
      </svg>

      {/* 顶部光泽效果 */}
      <div className="absolute inset-0 bg-gradient-to-br from-white/30 via-transparent to-transparent pointer-events-none"></div>

      <div className="mx-auto max-w-5xl px-4 sm:px-6 lg:px-8 w-full relative z-10">
        <div className="flex justify-center pb-8">
          <button
            onClick={handleGitHubClick}
            className="flex items-center space-x-2.5 rounded-2xl bg-white/70 px-4 py-2 text-gray-800 shadow-lg backdrop-blur-sm transition-all duration-300 hover:scale-105 hover:shadow-xl dark:bg-gray-800/50 dark:text-gray-200"
          >
            <SocialIcon kind="github" size={6} />
            <span className="font-semibold">Star on GitHub</span>
          </button>
        </div>
        {/* 标题 */}
        <div className="text-center mb-6 sm:mb-8">
          <h2 className="text-2xl sm:text-3xl lg:text-4xl font-bold bg-gradient-to-r from-gray-900 via-pink-800 to-indigo-700 dark:from-gray-100 dark:via-pink-200 dark:to-indigo-300 bg-clip-text text-transparent mb-2 sm:mb-3">
            {t('hero_section.headline')}
          </h2>
          <p className="text-base sm:text-lg text-gray-600 dark:text-gray-300 max-w-2xl mx-auto px-4">
            {t('hero_section.sub_headline')}
          </p>
        </div>

        {/* 输入组件 */}
        <UserInput onSubmitSuccess={handleSubmitSuccess} />
        <br />
        <ListPanel refreshTrigger={refreshTrigger} apiUrl="/api/protected/get-list" />
      </div>
    </section>
  );
} 