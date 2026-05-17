# EMS4J Frontend Mini

[中文文档](README.md)

`frontend-mini` is the EMS4J user-facing WeChat Mini Program validation project.

The current goal is to quickly verify:

- The `uni-app + Vue 3 + TypeScript` project can compile to a WeChat Mini Program.
- The Stitch home page design can be converted into a real mini-program page.
- WeChat DevTools can open `dist/dev/mp-weixin` and display the home page styles.

## Local Development

Before the first run, create the local environment file:

```bash
cd frontend-mini
cp .env.example .env.local
```

Then fill in the WeChat Mini Program AppID in `.env.local`:

```bash
WECHAT_MINI_APPID=wx-your-mini-program-appid
```

Install dependencies and start the WeChat Mini Program dev build:

```bash
pnpm install
pnpm dev:mp-weixin
```

Then open the following path in WeChat DevTools:

```text
dist/dev/mp-weixin
```

## Build Verification

```bash
pnpm test:manifest
pnpm typecheck
pnpm build:mp-weixin
```

The production build output is:

```text
dist/build/mp-weixin
```

## Notes

`src/manifest.json` is generated from `src/manifest.template.json` and the local `.env.local` file. It is not committed to the repository. The real WeChat Mini Program AppID is stored in `WECHAT_MINI_APPID` to avoid committing environment-specific mini-program configuration. Future business pages should continue to extend through the `api/*`, `mock/*`, and `platform/*` adaptation layers.
