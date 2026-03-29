const { test, expect } = require('@playwright/test')

const baseURL = process.env.TEST_BASE_URL || 'http://127.0.0.1:4173'

const envelope = (data) => ({
  status: 200,
  contentType: 'application/json',
  body: JSON.stringify({
    success: true,
    code: 100001,
    message: '',
    data
  })
})

const currentUser = {
  id: 1,
  userName: 'admin',
  realName: '管理员',
  userPhone: '13800000000'
}

const buildMenuPayload = () => {
  return [
    {
      id: 200,
      pid: null,
      menuName: '设备管理',
      menuKey: 'device_management',
      path: '',
      sortNum: 1,
      menuType: 1,
      icon: 'device',
      hidden: false,
      menuSource: 1
    },
    {
      id: 210,
      pid: 200,
      menuName: '智能电表',
      menuKey: 'device_management_electric_meter',
      path: '/devices/electric-meters',
      sortNum: 1,
      menuType: 1,
      icon: '',
      hidden: false,
      menuSource: 1
    },
    {
      id: 211,
      pid: 210,
      menuName: '详情',
      menuKey: 'device_management_electric_meter_detail',
      path: '',
      sortNum: 1,
      menuType: 2,
      icon: '',
      hidden: false,
      menuSource: 1
    },
    {
      id: 212,
      pid: 210,
      menuName: '编辑',
      menuKey: 'device_management_electric_meter_edit',
      path: '',
      sortNum: 2,
      menuType: 2,
      icon: '',
      hidden: false,
      menuSource: 1
    },
    {
      id: 213,
      pid: 210,
      menuName: '删除',
      menuKey: 'device_management_electric_meter_delete',
      path: '',
      sortNum: 3,
      menuType: 2,
      icon: '',
      hidden: false,
      menuSource: 1
    }
  ]
}

const buildMeterPagePayload = (meterName) => ({
  list: [
    {
      id: 1,
      spaceId: 101,
      spaceName: '101 配电间',
      spaceParentNames: ['A区', '1号楼'],
      meterName,
      deviceNo: 'EM-A1101-01',
      modelId: 501,
      modelName: 'DDSY-1000',
      communicateModel: 'tcp',
      gatewayId: 301,
      gatewayName: '1号接入网关',
      portNo: 1,
      meterAddress: 11,
      isOnline: true,
      isCutOff: false,
      isCalculate: true,
      isPrepay: true,
      protectedModel: false,
      pricePlanName: '居民电价方案',
      warnPlanName: '默认预警方案',
      electricWarnTypeName: '一级预警',
      ct: 100,
      accountId: 88,
      offlineDurationText: ''
    }
  ],
  total: 1,
  pageNum: 1,
  pageSize: 10
})

const buildMeterDetailPayload = (meterName) => ({
  id: 1,
  spaceId: 101,
  spaceName: '101 配电间',
  spaceParentNames: ['A区', '1号楼'],
  meterName,
  deviceNo: 'EM-A1101-01',
  modelId: 501,
  modelName: 'DDSY-1000',
  communicateModel: 'tcp',
  gatewayId: 301,
  gatewayName: '1号接入网关',
  portNo: 1,
  meterAddress: 11,
  isOnline: true,
  isCutOff: false,
  isCalculate: true,
  isPrepay: true,
  protectedModel: false,
  pricePlanName: '居民电价方案',
  warnPlanName: '默认预警方案',
  electricWarnTypeName: '一级预警',
  ct: 100,
  accountId: 88,
  latestPowerRecord: {
    recordTime: '2026-03-14 08:00:00',
    power: 168.4,
    powerHigher: 23.1,
    powerHigh: 52.6,
    powerLow: 61.7,
    powerLower: 25.8,
    powerDeepLow: 5.2
  }
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('electric meter page smoke flow supports query detail trend edit save', async ({ page }) => {
  let meterName = 'A1-101总表'
  const updatedMeterName = 'A1-101总表-已编辑'
  let lastSearchKey = ''
  let meterPageRequestCount = 0
  let updateRequestCount = 0
  let updatePayload = null
  let trendRequestCount = 0

  page.on('request', (request) => {
    if (request.url().includes('/api/v1/device/electric-meters/1/power-consume-trend')) {
      trendRequestCount += 1
    }
  })

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route('**/api/v1/device/electric-meters/page?**', async (route) => {
    const requestUrl = new URL(route.request().url())
    meterPageRequestCount += 1
    lastSearchKey = requestUrl.searchParams.get('searchKey') || ''
    await route.fulfill(envelope(buildMeterPagePayload(meterName)))
  })

  await page.route('**/api/v1/device/electric-meters/1', async (route) => {
    const request = route.request()
    const method = request.method()

    if (method === 'GET') {
      await route.fulfill(envelope(buildMeterDetailPayload(meterName)))
      return
    }

    if (method === 'PUT') {
      updatePayload = request.postDataJSON()
      updateRequestCount += 1
      meterName = updatePayload?.meterName || meterName
      await route.fulfill(envelope(true))
      return
    }

    await route.continue()
  })

  await page.route('**/api/v1/device/electric-meters/1/power-consume-trend?**', async (route) => {
    await route.fulfill(
      envelope([
        {
          beginRecordTime: '2026-03-27 06:00:00',
          endRecordTime: '2026-03-27 08:00:00',
          meterConsumeTime: '2026-03-27 08:00:00',
          consumePower: 2.6
        },
        {
          beginRecordTime: '2026-03-28 06:00:00',
          endRecordTime: '2026-03-28 08:00:00',
          meterConsumeTime: '2026-03-28 08:00:00',
          consumePower: 3.4
        }
      ])
    )
  })

  await page.route('**/api/v1/device/device-models?**', async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 501,
          typeKey: 'electricMeter',
          modelName: 'DDSY-1000',
          communicateModel: 'tcp',
          isNb: false,
          isCt: true,
          isPrepay: true
        }
      ])
    )
  })

  await page.route(/\/api\/v1\/device\/gateways(?:\?.*)?$/, async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 301,
          gatewayName: '1号接入网关',
          deviceNo: 'GW-2025-001',
          sn: 'GW-2025-001'
        }
      ])
    )
  })

  await page.route('**/api/v1/spaces/tree', async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 1,
          pid: 0,
          name: 'A区',
          fullPath: 'A区',
          children: [
            {
              id: 11,
              pid: 1,
              name: '1号楼',
              fullPath: 'A区,1号楼',
              children: [
                {
                  id: 101,
                  pid: 11,
                  name: '101 配电间',
                  fullPath: 'A区,1号楼,101 配电间'
                }
              ]
            }
          ]
        }
      ])
    )
  })

  await page.goto(`${baseURL}/devices/electric-meters`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/devices\/electric-meters$/)
  await expect(page.locator('tbody tr').first()).toContainText(meterName)

  await page.getByPlaceholder('请输入电表名称/编号').fill('A1')
  await page.getByRole('button', { name: '查询' }).click()

  await expect.poll(() => lastSearchKey).toBe('A1')

  const firstRow = page.locator('tbody tr').first()

  if (process.env.SAVE_TREND_ENTRY_SCREENSHOT === 'true') {
    await firstRow.screenshot({
      path: process.env.TREND_ENTRY_SCREENSHOT_PATH || '.tmp/electric-meter-trend-entry-smoke.png',
      animations: 'disabled'
    })
  }

  await firstRow.getByRole('button', { name: '详情' }).click()
  const detailModal = page.locator('.meter-detail-modal')
  await expect(detailModal).toBeVisible()
  await expect(detailModal).toContainText('电表详情')
  await expect(detailModal).toContainText('168.40')
  await detailModal.getByRole('button', { name: '关闭' }).click()
  await expect(detailModal).toBeHidden()

  await firstRow.getByRole('button', { name: '用电趋势' }).click()
  const trendModal = page.locator('.meter-trend-modal')
  await expect(trendModal).toBeVisible()
  await expect(trendModal).toContainText('用电趋势')
  await expect(trendModal).toContainText(meterName)
  await expect(trendModal).toContainText('EM-A1101-01')
  await expect.poll(() => trendRequestCount).toBe(1)
  await expect(trendModal.locator('[data-test="trend-chart"]')).toBeVisible()
  if (process.env.SAVE_TREND_SCREENSHOT === 'true') {
    await trendModal.screenshot({
      path: process.env.TREND_SCREENSHOT_PATH || '.tmp/electric-meter-trend-modal-smoke.png',
      animations: 'disabled'
    })
  }
  await trendModal.getByRole('button', { name: '关闭' }).click()
  await expect(trendModal).toBeHidden()

  await firstRow.getByRole('button', { name: '...' }).click()
  const moreActionMenu = page.locator('.more-action-floating-menu')
  await expect(moreActionMenu).toBeVisible()
  await expect(moreActionMenu.getByRole('button', { name: '删除' })).toBeVisible()

  await firstRow.getByRole('button', { name: '编辑' }).click()
  const editModal = page.locator('.meter-edit-modal')
  await expect(editModal).toBeVisible()

  await editModal.getByPlaceholder('请输入电表名称').fill(updatedMeterName)
  await editModal.getByRole('button', { name: '确定' }).click({ force: true, noWaitAfter: true })

  await expect.poll(() => updateRequestCount).toBe(1)
  expect(updatePayload).toMatchObject({
    spaceId: 101,
    meterName: updatedMeterName,
    isCalculate: true,
    isPrepay: true
  })
  await expect(page.locator('tbody tr').first()).toContainText(updatedMeterName)
  await expect.poll(() => meterPageRequestCount >= 3).toBe(true)
})
