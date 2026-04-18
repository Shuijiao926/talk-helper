# 使用官方 Node.js 镜像作为基础
FROM node:20-alpine

# 在容器内设置工作目录
WORKDIR /app

# For canvas
RUN apk add --no-cache build-base cairo-dev jpeg-dev pango-dev giflib-dev

# 启用 corepack 以使用项目指定的 yarn 版本
RUN corepack enable

# 复制与依赖相关的文件
COPY package.json yarn.lock .yarnrc.yml ./
COPY .yarn ./.yarn

# 安装项目依赖
# RUN yarn install --immutable

# 将所有项目文件复制到工作目录
COPY . .

# 暴露 Next.js 开发服务器的默认端口
EXPOSE 3000

# 启动开发服务器的命令
# CMD ["yarn", "start"] 