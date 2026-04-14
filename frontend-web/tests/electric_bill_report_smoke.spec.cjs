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
    menuName: '报表统计',
    menuKey: 'report_statistics',
    path: '',
    sortNum: 1,
    menuType: 1,
    icon: 'report',
    hidden: false,
    menuSource: 1
  },
  {
    id: 310,
    pid: 300,
    menuName: '电费报表',
    menuKey: 'report_statistics_electric_bill',
    path: '/reports/electric-bill',
    sortNum: 1,
    menuType: 1,
    icon: '',
    hidden: false,
    menuSource: 1
  }
]

const buildPageItem = (accountName) => ({
  accountId: 101,
  accountName,
  electricAccountType: 2,
  electricAccountTypeName: '合并计费',
  meterCount: 2,
  periodConsumePowerText: '14023.6',
  periodElectricChargeAmountText: '12454.25',
  periodRechargeAmountText: '0.00',
  periodCorrectionAmountText: '0.00',
  totalDebitAmountText: '12454.25'
})

const buildDetailPayload = (accountName) => ({
  accountInfo: {
    accountId: 101,
    accountName,
    contactName: '邢中奇',
    contactPhone: '13585000114',
    electricAccountType: 2,
    electricAccountTypeName: '合并计费',
    monthlyPayAmountText: '0.00',
    accountBalanceText: '-147908.41',
    meterCount: 2,
    periodConsumePowerText: '14023.6',
    periodElectricChargeAmountText: '12454.25',
    periodRechargeAmountText: '0.00',
    periodCorrectionAmountText: '0.00',
    dateRangeText: '2026-04-01~2026-04-08'
  },
  meterList: [
    {
      meterId: 201,
      deviceNo: 'EM202506030151',
      meterName: '美特富1',
      consumePowerHigherText: '0',
      consumePowerHighText: '9274',
      consumePowerLowText: '0',
      consumePowerLowerText: '0',
      consumePowerDeepLowText: '0',
      displayPriceHigherText: '—',
      displayPriceHighText: '—',
      displayPriceLowText: '—',
      displayPriceLowerText: '—',
      displayPriceDeepLowText: '—',
      electricChargeAmountHigherText: '—',
      electricChargeAmountHighText: '—',
      electricChargeAmountLowText: '—',
      electricChargeAmountLowerText: '—',
      electricChargeAmountDeepLowText: '—',
      totalConsumePowerText: '9274',
      totalElectricChargeAmountText: '—',
      totalRechargeAmountText: '—',
      totalCorrectionAmountText: '—'
    }
  ]
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('electric bill report smoke flow supports query and detail modal', async ({ page }) => {
  let accountName = '美特富精密拉深技术（无锡）有限公司'
  let pageRequestCount = 0
  let detailRequestCount = 0
  let lastAccountNameLike = ''

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route('**/api/v1/report/electric-bill/page?**', async (route) => {
    const requestUrl = new URL(route.request().url())
    pageRequestCount += 1
    lastAccountNameLike = requestUrl.searchParams.get('accountNameLike') || ''
    await route.fulfill(
      envelope({
        list: [buildPageItem(accountName)],
        total: 1,
        pageNum: 1,
        pageSize: 10
      })
    )
  })

  await page.route(/\/api\/v1\/report\/electric-bill\/101\/detail(?:\?.*)?$/, async (route) => {
    detailRequestCount += 1
    await route.fulfill(envelope(buildDetailPayload(accountName)))
  })

  await page.goto(`${baseURL}/reports/electric-bill`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/reports\/electric-bill$/)
  await expect(page.locator('tbody tr').first()).toContainText(accountName)
  await expect(page.locator('tbody tr').first()).toContainText('合并计费')

  await page.getByPlaceholder('请输入账户名称').fill('美特')
  await page.getByRole('button', { name: '查询' }).click()

  await expect.poll(() => lastAccountNameLike).toBe('美特')
  await expect.poll(() => pageRequestCount >= 2).toBe(true)

  await page.locator('tbody tr').first().getByRole('button', { name: '详情' }).click()

  const detailModal = page.locator('.report-detail-modal')
  await expect(detailModal).toBeVisible()
  await expect.poll(() => detailRequestCount).toBe(1)
  await expect(detailModal).toContainText('电费报表详情')
  await expect(detailModal).toContainText(accountName)
  await expect(detailModal).toContainText('邢中奇')
  await expect(detailModal).toContainText('EM202506030151')

  await detailModal.getByRole('button', { name: '关闭' }).click()
  await expect(detailModal).toBeHidden()
})
