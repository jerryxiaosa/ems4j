# EMS4J Frontend Mini

[English](README_EN.md)

`frontend-mini` 是 EMS4J 用户端微信小程序验证工程。

当前阶段目标是快速验证：

- `uni-app + Vue 3 + TypeScript` 工程能正常编译到微信小程序。
- Stitch 首页设计能转成真实小程序页面。
- 微信开发者工具能打开 `dist/dev/mp-weixin` 并看到首页样式。

## 本地运行

首次运行前，先创建本地环境变量文件：

```bash
cd frontend-mini
cp .env.example .env.local
```

然后在 `.env.local` 中填写微信小程序 AppID：

```bash
WECHAT_MINI_APPID=wx-your-mini-program-appid
```

然后安装依赖并启动微信小程序开发构建：

```bash
pnpm install
pnpm dev:mp-weixin
```

然后用微信开发者工具打开：

```text
dist/dev/mp-weixin
```

## 构建验证

```bash
pnpm test:manifest
pnpm typecheck
pnpm build:mp-weixin
```

发布构建产物在：

```text
dist/build/mp-weixin
```

## 说明

`src/manifest.json` 由 `src/manifest.template.json` 和本地 `.env.local` 生成，不提交到版本库。真实微信小程序 AppID 放在 `WECHAT_MINI_APPID` 中，避免把具体小程序配置写入仓库。后续业务页面应继续通过 `api/*`、`mock/*` 和 `platform/*` 适配层扩展。
