import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, test } from 'vitest'

const readViewSource = (relativePath: string) => {
  return readFileSync(resolve(__dirname, relativePath), 'utf-8')
}

describe('page design consistency', () => {
  test('testListCards_ShouldUseFivePixelRadiusInCancelAndOperationViews', () => {
    const accountCancelSource = readViewSource('./accounts/AccountCancelRecordView.vue')
    const deviceOperationSource = readViewSource('./device/DeviceOperationsView.vue')

    expect(accountCancelSource).toMatch(/\.search-card,\s*\.table-card\s*\{[\s\S]*border-radius:\s*5px;/)
    expect(deviceOperationSource).toMatch(/\.search-card,\s*\.table-card\s*\{[\s\S]*border-radius:\s*5px;/)
    expect(deviceOperationSource).toMatch(/\.table-wrap\s*\{[\s\S]*border-radius:\s*5px;/)
  })

  test('testListPages_ShouldSeparateSearchCardAndTableCard', () => {
    const pageSources = [
      readViewSource('./devices/DeviceGatewayView.vue'),
      readViewSource('./devices/DeviceElectricMeterView.vue'),
      readViewSource('./plans/ElectricPricePlanView.vue'),
      readViewSource('./plans/WarnPlanView.vue')
    ]

    pageSources.forEach((source) => {
      expect(source).toContain('<section class="search-card">')
      expect(source).toContain('<section class="table-card">')
      expect(source).not.toContain('<section class="workspace-card">')
    })
  })

  test('testDetailModalTitles_ShouldUseSameTypographyInConsumeAndMeterDetail', () => {
    const meterConsumeSource = readViewSource('../components/trades/MeterConsumeDetailModal.vue')
    const electricMeterDetailSource = readViewSource('../components/devices/DeviceElectricMeterDetailModal.vue')

    expect(meterConsumeSource).toMatch(/\.modal-title\s*\{[\s\S]*font-size:\s*var\(--es-font-size-md\);[\s\S]*font-weight:\s*600;/)
    expect(electricMeterDetailSource).toMatch(/\.modal-title\s*\{[\s\S]*font-size:\s*var\(--es-font-size-md\);[\s\S]*font-weight:\s*600;/)

    expect(meterConsumeSource).toMatch(/\.section-title\s*\{[\s\S]*font-size:\s*14px;[\s\S]*font-weight:\s*600;/)
    expect(electricMeterDetailSource).toMatch(/\.section-title\s*\{[\s\S]*font-size:\s*14px;[\s\S]*font-weight:\s*600;/)
  })

  test('testDetailFieldLabels_ShouldNotOverrideEsDetailLabelWeightInConsumeDetail', () => {
    const meterConsumeSource = readViewSource('../components/trades/MeterConsumeDetailModal.vue')

    expect(meterConsumeSource).not.toMatch(/\.summary-label\s*\{[^}]*font-weight:\s*600;/)
    expect(meterConsumeSource).not.toMatch(/\.summary-label\s*\{[^}]*color:\s*var\(--es-color-text-primary\);/)
  })

  test('testLongCommandBlocks_ShouldUseLocalScrollInOperationDetail', () => {
    const operationDetailSource = readViewSource('../components/devices/DeviceOperationDetailModal.vue')

    expect(operationDetailSource).toContain('class="command-box-wrap"')
    expect(operationDetailSource).toMatch(/\.command-box-wrap\s*\{[^}]*max-height:\s*260px;/)
    expect(operationDetailSource).toMatch(/\.command-box-wrap\s*\{[^}]*overflow:\s*auto;/)
    expect(operationDetailSource).toMatch(/\.operation-detail-modal\s*\{[\s\S]*border-radius:\s*5px;/)
    expect(operationDetailSource).toMatch(/\.nested-modal-panel\s*\{[\s\S]*border-radius:\s*5px;/)
  })

  test('testUserManagementActions_ShouldUseAccountInfoLinkStyle', () => {
    const userManagementSource = readViewSource('./system/UserManagementView.vue')

    expect(userManagementSource).toMatch(/\.btn-link,\s*\.btn-link-danger\s*\{[\s\S]*font-weight:\s*500;/)
    expect(userManagementSource).toMatch(/\.btn-link,\s*\.btn-link-danger\s*\{[\s\S]*line-height:\s*1\.2;/)
    expect(userManagementSource).toMatch(/\.btn-link-danger:hover\s*\{[\s\S]*opacity:\s*0\.85;/)
  })
})
