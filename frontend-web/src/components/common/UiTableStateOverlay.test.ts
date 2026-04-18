import { describe, expect, test } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('UiTableStateOverlay', () => {
  test('testLoadingOverlay_ShouldBlockPointerEvents', () => {
    const source = readFileSync(
      resolve(__dirname, './UiTableStateOverlay.vue'),
      'utf-8'
    )

    expect(source).toMatch(/\.ui-table-state-overlay\.is-loading\s*\{[\s\S]*pointer-events:\s*auto;/)
  })
})
