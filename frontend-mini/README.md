# EMS4J Frontend Mini

`frontend-mini` 是 EMS4J 用户端微信小程序验证工程。

当前阶段目标是快速验证：

- `uni-app + Vue 3 + TypeScript` 工程能正常编译到微信小程序。
- Stitch 首页设计能转成真实小程序页面。
- 微信开发者工具能打开 `dist/dev/mp-weixin` 并看到首页样式。

## 本地运行

```bash
cd frontend-mini
pnpm install
pnpm dev:mp-weixin
```

然后用微信开发者工具打开：

```text
dist/dev/mp-weixin
```

## 构建验证

```bash
pnpm typecheck
pnpm build:mp-weixin
```

发布构建产物在：

```text
dist/build/mp-weixin
```

## 说明

当前首页是静态验证页，没有接入登录、接口和支付。`src/manifest.json` 里的微信小程序 `appid` 仍为空，使用真实 AppID 调试时需要手工填写。后续业务页面应继续通过 `api/*`、`mock/*` 和 `platform/*` 适配层扩展。
