/// <reference types="@dcloudio/types" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@qiun/ucharts' {
  export default class UCharts {
    constructor(options: Record<string, unknown>)
    updateData(options?: Record<string, unknown>): void
    showToolTip(event: unknown, options?: Record<string, unknown>): void
  }
}
