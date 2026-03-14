import { resolve } from 'node:path'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  define: {
    __APP_TITLE__: JSON.stringify('能耗预付费管理系统')
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.test.ts'],
    exclude: ['tests/**/*', 'node_modules/**/*'],
    setupFiles: ['./tests/unit/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: [
        'src/api/raw/types.ts',
        'src/stores/auth.ts',
        'src/stores/permission.ts',
        'src/api/adapters/user.ts',
        'src/modules/system/users/composables/useUserNotice.ts',
        'src/modules/system/users/composables/useUserQuery.ts',
        'src/modules/system/users/composables/useUserCrud.ts',
        'src/modules/devices/electric-meters/composables/electricMeterShared.ts',
        'src/modules/devices/electric-meters/composables/useElectricMeterNotice.ts',
        'src/modules/devices/electric-meters/composables/useElectricMeterQuery.ts',
        'src/modules/devices/electric-meters/composables/useElectricMeterActions.ts',
        'src/router/guard.ts'
      ],
      thresholds: {
        lines: 95,
        functions: 90,
        branches: 90,
        statements: 95
      }
    }
  }
})
