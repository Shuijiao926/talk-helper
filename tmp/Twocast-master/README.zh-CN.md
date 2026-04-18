# 🎙️ Twocast - AI播客生成器、双人播客生成器

🌐 官网：[Twocast.app](https://twocast.app/)

![Screenshot](./public/assets/img/image.png)

## 交流群
- [Discord](https://discord.gg/VJREVF9Ja3)
- [Telegram](https://t.me/+FYXV9IAVqcY5MTYx)

---

## ✨ 主要特性
- 👥 双人播客
- ⏱️ 一键生成 3~5 分钟播客
- 🧠 支持多种生成方式：**主题**、**链接**、**文档**（doc/pdf/txt）、**列表页**（5~9 分钟）
- 🌍 多语言支持
- ⬇️ 可下载音频
- 📋 播客内容包含：**音频、大纲、脚本**
- 🔌 支持三大平台：**Fish Audio**、**Minimax**、**Google Gemini**

---

## 🎧 示例播客

- 🇺🇸 English: [Hacker News Hot Articles](https://twocast.app/podcast/vs962a7f-9461-4875-b7c7-2f5aca66126e)
- 🇨🇳 中文: [Hacker News 热榜](https://twocast.app/podcast/vs789e71-b192-4374-93a2-8177f457ba5c)
- 🇨🇳 中文: [V2EX 热榜](https://twocast.app/podcast/vsbed589-6493-4ac2-8217-64d82b1ecafa)

---

## 🚀 快速开始

### 方法一：本地启动

1. **启动依赖服务**
   ```bash
   docker run -t -d --restart always -p 8080:8080 -e PORT=8080 --name textract bespaloff/textract-rest-api:v4.0.2
   docker run -d --restart always --name ffmpeg-api -p 8081:3000 kazhar/ffmpeg-api
   ```
2. **配置环境变量**
   ```bash
   cp .env.example .env
   ```
   详细内容见[环境变量配置](#环境变量配置)
3. **启动 Postgres 数据库**
   - 创建数据库 `twocast`
   - 修改 `.env` 文件中的 `DATABASE_URL`
   - 初始化数据库：
     ```bash
     npx drizzle-kit push
     ```
4. **启动项目**
   ```bash
   yarn && yarn start
   ```

---

### 方法二：Docker 一键启动

> ⚠️ 如遇问题请优先使用"本地启动"方式。

1. **配置环境变量**
   ```bash
   cp .env.docker .env
   ```
   详细内容见[环境变量配置](#环境变量配置)
2. **启动**
   ```bash
   docker compose up
   ```

---

## 环境变量配置

### 🔊 TTS API 配置

- 🎏 **Fish Audio**  
  注册并获取 API Key：[Fish Audio](https://bit.ly/4k7AXHt)，填入 `FISH_AUDIO_TOKEN=`
- 🦾 **Minimax**（可选）  
  [Profile](https://www.minimax.io/platform/user-center/basic-information) 获取 GroupID，填入 `MINIMAX_GROUP_ID=`  
  [API keys](https://www.minimax.io/platform/user-center/basic-information/interface-key) 获取 API Key，填入 `MINIMAX_TOKEN=`  
  启用：`MINIMAX_ENABLED=1`
- 🌈 **Google Gemini**（可选，费用较高）  
  [Google AI Studio](https://aistudio.google.com/gen-media) 获取 API Key，填入 `GEMINI_TOKEN=`  
  启用：`GEMINI_ENABLED=1`

### 🤖 LLM API 配置

- 💬 **Chat**：[OpenRouter](https://openrouter.ai) 获取 API Key，填入 `LLM_API_KEY=`
- 🔍 **Search**：[x.ai](https://console.x.ai/) 获取 API Key，填入 `LLM_SEARCH_API_KEY=`

---

## 🙏 鸣谢

- [tailwind-nextjs-starter-blog-i18n](https://github.com/PxlSyl/tailwind-nextjs-starter-blog-i18n)