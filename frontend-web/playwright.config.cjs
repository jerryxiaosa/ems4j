const { defineConfig } = require('@playwright/test')

module.exports = defineConfig({
  testDir: './tests',
  testMatch: /.*\.spec\.cjs$/,
  use: {
    baseURL: process.env.TEST_BASE_URL || 'http://127.0.0.1:4173'
  }
})
