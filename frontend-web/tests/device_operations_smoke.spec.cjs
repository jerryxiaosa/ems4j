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

const buildMenuPayload = () => [
  {
    id: 300,
    pid: null,
    menuName: '设备操作',
    menuKey: 'device_operation',
    path: '/device-operations',
    sortNum: 1,
    menuType: 1,
    icon: 'operation',
    hidden: false,
    menuSource: 1
  }
]

const buildOperationPagePayload = () => ({
  list: [
    {
      id: 13,
      deviceNo: 'EM-1001',
      deviceName: '一号电表',
      deviceType: 'electricMeter',
      deviceTypeName: '电表',
      spaceName: 'A区 / 101',
      commandType: 5,
      commandTypeName: '设置CT变比',
      success: false,
      ensureSuccess: true,
      isRunning: false,
      executeTimes: 1,
      maxExecuteTimes: 3,
      successName: '失败',
      operateUserName: '张三',
      createTime: '2026-04-14 10:00:00'
    },
    {
      id: 14,
      deviceNo: 'GW-2001',
      deviceName: '一号网关',
      deviceType: 'gateway',
      deviceTypeName: '网关',
      spaceName: 'A区 / 机房',
      commandType: 1,
      commandTypeName: '电表充值自动合闸',
      success: true,
      ensureSuccess: false,
      isRunning: false,
      executeTimes: 1,
      maxExecuteTimes: 1,
      successName: '成功',
      operateUserName: '李四',
      createTime: '2026-04-14 11:00:00'
    }
  ],
  total: 2,
  pageNum: 1,
  pageSize: 10
})

const buildOperationDetailPayload = () => ({
  id: 13,
  deviceId: 301,
  deviceIotId: 'iot-301',
  deviceNo: 'EM-1001',
  deviceName: '一号电表',
  deviceType: 'electricMeter',
  deviceTypeName: '电表',
  spaceName: 'A区 / 101',
  commandType: 5,
  commandTypeName: '设置CT变比',
  commandSource: 1,
  commandSourceName: '用户命令',
  commandData: '{"ct":150}',
  success: false,
  isRunning: false,
  successName: '失败',
  successTime: '--',
  lastExecuteTime: '2026-04-14 10:10:00',
  ensureSuccess: true,
  executeTimes: 1,
  maxExecuteTimes: 3,
  operateUserName: '张三',
  createTime: '2026-04-14 10:00:00',
  remark: '重试后应恢复'
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('device operations smoke flow supports list detail and retry', async ({ page }) => {
  let lastOperateUserName = ''
  let retryRequestCount = 0

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route(/\/api\/v1\/system\/enums(?:\?.*)?$/, async (route) => {
    await route.fulfill(
      envelope({
        commandType: [
          { value: 1, info: '电表充值自动合闸' },
          { value: 5, info: '设置CT变比' }
        ],
        deviceType: [
          { value: 'electricMeter', info: '电表' },
          { value: 'gateway', info: '网关' }
        ]
      })
    )
  })

  await page.route('**/api/v1/device/operations/page?**', async (route) => {
    const requestUrl = new URL(route.request().url())
    lastOperateUserName = requestUrl.searchParams.get('operateUserName') || ''
    await route.fulfill(envelope(buildOperationPagePayload()))
  })

  await page.route('**/api/v1/device/operations/13', async (route) => {
    await route.fulfill(envelope(buildOperationDetailPayload()))
  })

  await page.route('**/api/v1/device/operations/13/execute-records', async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 1,
          commandId: 13,
          commandSource: 1,
          commandSourceName: '用户命令',
          success: false,
          reason: '网关超时',
          runTime: '2026-04-14 10:05:00'
        }
      ])
    )
  })

  await page.route('**/api/v1/device/operations/13/retry', async (route) => {
    retryRequestCount += 1
    await route.fulfill(envelope(null))
  })

  await page.goto(`${baseURL}/device-operations`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/device-operations$/)
  await expect(page.locator('tbody tr').first()).toContainText('一号电表')
  await expect(page.getByRole('button', { name: '重试' })).toHaveCount(1)

  await page.getByPlaceholder('请输入操作人员').fill('张三')
  await page.getByRole('button', { name: '查询' }).click()
  await expect.poll(() => lastOperateUserName).toBe('张三')

  const firstRow = page.locator('tbody tr').first()
  await firstRow.getByRole('button', { name: '详情' }).click()

  const detailModal = page.locator('.operation-detail-modal')
  await expect(detailModal).toBeVisible()
  await expect(detailModal).toContainText('设置CT变比')
  await expect(detailModal).toContainText('用户命令')
  await expect(detailModal.getByRole('button', { name: '重试' })).toBeVisible()
  await detailModal.getByRole('button', { name: '关闭' }).click()
  await expect(detailModal).toBeHidden()

  await firstRow.getByRole('button', { name: '重试' }).click()
  await expect.poll(() => retryRequestCount).toBe(1)
  await expect(page.locator('.notice-success')).toContainText('设备操作重试已提交')
})
