# 🎙️ Twocast - AI Podcast Generator - Two-person Podcast
[中文手册](./README.zh-CN.md) | 🌐 Website: [Twocast.app](https://twocast.app/)

![Screenshot](./public/assets/img/image.png)

## Community
- [Discord](https://discord.gg/VJREVF9Ja3)
- [Telegram](https://t.me/+FYXV9IAVqcY5MTYx)

---

## ✨ Key Features

- 👥 Two-person Podcast
- ⏱️ Generate 3-5 minute podcasts with one click
- 🧠 Supports multiple generation methods: **Topic**, **Link**, **Document** (doc/pdf/txt), **List Page** (5-9 minutes)
- 🌍 Multi-language support
- ⬇️ Downloadable audio
- 📋 Podcast content includes: **Audio, Outline, Script**
- 🔌 Supports three major platforms: **Fish Audio**, **Minimax**, **Google Gemini**

---

## 🎧 Sample Podcasts

- 🇺🇸 English: [Hacker News Hot Articles](https://twocast.app/podcast/vs962a7f-9461-4875-b7c7-2f5aca66126e)
- 🇨🇳 中文: [Hacker News 热榜](https://twocast.app/podcast/vs789e71-b192-4374-93a2-8177f457ba5c)
- 🇨🇳 中文: [V2EX 热榜](https://twocast.app/podcast/vsbed589-6493-4ac2-8217-64d82b1ecafa)

---

## 🚀 Quick Start

### Method 1: Local Setup

1.  **Start dependency services**
    ```bash
    docker run -t -d --restart always -p 8080:8080 -e PORT=8080 --name textract bespaloff/textract-rest-api:v4.0.2
    docker run -d --restart always --name ffmpeg-api -p 8081:3000 kazhar/ffmpeg-api
    ```
2.  **Configure environment variables**
    ```bash
    cp .env.example .env
    ```
    See [Environment Variable Configuration](#environment-variable-configuration) for details.
3.  **Start Postgres database**
    -   Create a database `twocast`
    -   Modify `DATABASE_URL` in the `.env` file
    -   Initialize the database:
        ```bash
        npx drizzle-kit push
        ```
4.  **Start the project**
    ```bash
    yarn && yarn start
    ```

---

### Method 2: Docker One-Click Start

> ⚠️ If you encounter any issues, please prioritize using the "Local Setup" method.

1.  **Configure environment variables**
    ```bash
    cp .env.docker .env
    ```
    See [Environment Variable Configuration](#environment-variable-configuration) for details.
2.  **Start**
    ```bash
    docker compose up
    ```

---

## Environment Variable Configuration

### 🔊 TTS API Configuration

-   🎏 **Fish Audio**  
    Register and get an API Key: [Fish Audio](https://bit.ly/4k7AXHt), and enter it in `FISH_AUDIO_TOKEN=`
-   🦾 **Minimax** (Optional)  
    Get GroupID from [Profile](https://www.minimax.io/platform/user-center/basic-information), and enter it in `MINIMAX_GROUP_ID=`  
    Get API Key from [API keys](https://www.minimax.io/platform/user-center/basic-information/interface-key), and enter it in `MINIMAX_TOKEN=`  
    Enable: `MINIMAX_ENABLED=1`
-   🌈 **Google Gemini** (Optional, more expensive)  
    Get API Key from [Google AI Studio](https://aistudio.google.com/gen-media), and enter it in `GEMINI_TOKEN=`  
    Enable: `GEMINI_ENABLED=1`

### 🤖 LLM API Configuration

-   💬 **Chat**: Get API Key from [OpenRouter](https://openrouter.ai), and enter it in `LLM_API_KEY=`
-   🔍 **Search**: Get API Key from [x.ai](https://console.x.ai/), and enter it in `LLM_SEARCH_API_KEY=`

---

## 🙏 Acknowledgements

-   [tailwind-nextjs-starter-blog-i18n](https://github.com/PxlSyl/tailwind-nextjs-starter-blog-i18n)