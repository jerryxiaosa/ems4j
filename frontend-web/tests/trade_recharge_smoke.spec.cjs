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
    id: 220,
    pid: null,
    menuName: '收费管理',
    menuKey: 'trade_management',
    path: '',
    sortNum: 1,
    menuType: 1,
    icon: 'trade',
    hidden: false,
    menuSource: 1
  },
  {
    id: 221,
    pid: 220,
    menuName: '电费充值',
    menuKey: 'trade_management_electric_recharge',
    path: '/trade/recharge',
    sortNum: 1,
    menuType: 1,
    icon: '',
    hidden: false,
    menuSource: 1
  },
  {
    id: 222,
    pid: 220,
    menuName: '订单流水',
    menuKey: 'trade_management_order_flow',
    path: '/trade/order-flows',
    sortNum: 2,
    menuType: 1,
    icon: '',
    hidden: false,
    menuSource: 1
  },
  {
    id: 223,
    pid: 221,
    menuName: '确认充值',
    menuKey: 'trade_management_electric_recharge_confirm_recharge',
    path: '',
    sortNum: 1,
    menuType: 2,
    icon: '',
    hidden: false,
    menuSource: 1
  },
  {
    id: 224,
    pid: 221,
    menuName: '电费服务费设置',
    menuKey: 'trade_management_electric_recharge_service_fee_setting',
    path: '',
    sortNum: 2,
    menuType: 2,
    icon: '',
    hidden: false,
    menuSource: 1
  }
]

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('trade recharge smoke flow supports selecting account and submitting merged recharge order', async ({
  page
}) => {
  let accountOptionRequestCount = 0
  let createOrderPayload = null

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route(/\/api\/v1\/system\/enums(?:\?.*)?$/, async (route) => {
    await route.fulfill(
      envelope({
        paymentChannel: [{ value: 'OFFLINE', info: '现金支付' }],
        balanceType: [
          { value: 0, info: '账户余额' },
          { value: 1, info: '电表余额' }
        ],
        meterType: [{ value: 2, info: '单相表' }]
      })
    )
  })

  await page.route(/\/api\/v1\/orders\/service-rate\/default(?:\?.*)?$/, async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill(
        envelope({
          defaultServiceRate: 0.1
        })
      )
      return
    }

    await route.continue()
  })

  await page.route('**/api/v1/accounts/options?**', async (route) => {
    accountOptionRequestCount += 1
    await route.fulfill(
      envelope([
        {
          id: 88,
          name: '测试企业A',
          managerName: '张三',
          managerPhone: '13800000000'
        }
      ])
    )
  })

  await page.route('**/api/v1/accounts/88', async (route) => {
    await route.fulfill(
      envelope({
        id: 88,
        ownerId: 1001,
        ownerType: 0,
        ownerTypeName: '企业',
        ownerName: '测试企业A',
        contactName: '张三',
        contactPhone: '13800000000',
        electricAccountType: 2,
        electricAccountTypeName: '合并计费',
        electricBalanceAmountText: '800.00',
        meterList: []
      })
    )
  })

  await page.route(/\/api\/v1\/orders\/energy-top-up(?:\?.*)?$/, async (route) => {
    createOrderPayload = route.request().postDataJSON()
    await route.fulfill(
      envelope({
        orderSn: 'ORDER-001',
        paymentChannel: 'OFFLINE'
      })
    )
  })

  await page.route('**/api/v1/orders?**', async (route) => {
    await route.fulfill(
      envelope({
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      })
    )
  })

  await page.goto(`${baseURL}/trade/recharge`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/trade\/recharge$/)

  const accountInput = page.getByPlaceholder('请选择账户')
  await accountInput.click()
  await expect.poll(() => accountOptionRequestCount).toBeGreaterThan(0)
  await page.getByRole('button', { name: /测试企业A/ }).click()

  await expect(page.locator('.summary-grid')).toContainText('合并计费')
  await expect(page.locator('body')).not.toContainText('更换电表')

  await page.getByPlaceholder('请输入付款金额').fill('200')
  await page.getByRole('button', { name: '确认充值' }).click()

  await expect.poll(() => createOrderPayload).not.toBeNull()
  expect(createOrderPayload).toMatchObject({
    userId: 1,
    userPhone: '13800000000',
    userRealName: '管理员',
    orderAmount: 200,
    paymentChannel: 'OFFLINE',
    energyTopUp: {
      accountId: 88,
      ownerId: 1001,
      ownerType: 0,
      ownerName: '测试企业A',
      electricAccountType: 2,
      balanceType: 0,
      serviceRate: 0.1
    }
  })

  await expect(page.locator('.page-notice-success')).toContainText('充值订单创建成功')
  await page.waitForURL(/\/trade\/order-flows$/)
})
