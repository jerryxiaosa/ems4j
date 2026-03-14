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

const buildMenuPayload = ({ includeCreatePermission }) => {
  const menus = [
    {
      id: 100,
      pid: null,
      menuName: '系统管理',
      menuKey: 'system_management',
      path: '',
      sortNum: 1,
      menuType: 1,
      icon: 'system',
      hidden: false,
      menuSource: 1
    },
    {
      id: 110,
      pid: 100,
      menuName: '用户管理',
      menuKey: 'system_management_user_management',
      path: '/system/users',
      sortNum: 1,
      menuType: 1,
      icon: '',
      hidden: false,
      menuSource: 1
    },
    {
      id: 111,
      pid: 110,
      menuName: '详情',
      menuKey: 'system_management_user_management_detail',
      path: '',
      sortNum: 1,
      menuType: 2,
      icon: '',
      hidden: false,
      menuSource: 1
    }
  ]

  if (includeCreatePermission) {
    menus.push({
      id: 112,
      pid: 110,
      menuName: '新增',
      menuKey: 'system_management_user_management_create',
      path: '',
      sortNum: 2,
      menuType: 2,
      icon: '',
      hidden: false,
      menuSource: 1
    })
  }

  return menus
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('menu endpoint failure redirects authenticated navigation to login', async ({ page }) => {
  await page.route('**/api/v1/users/current/menus?**', async (route) => {
    await route.abort('failed')
  })

  await page.goto(`${baseURL}/accounts/info`)

  await expect(page).toHaveURL(/\/login(?:\?|$)/)
})

test('permission refresh hides directive-guarded buttons on current page', async ({ page }) => {
  let menuRequestCount = 0

  await page.route('**/api/v1/users/current', async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route('**/api/v1/users/current/menus?**', async (route) => {
    menuRequestCount += 1
    const payload =
      menuRequestCount === 1
        ? buildMenuPayload({ includeCreatePermission: true })
        : buildMenuPayload({ includeCreatePermission: false })
    await route.fulfill(envelope(payload))
  })

  await page.route('**/api/v1/users/page?**', async (route) => {
    await route.fulfill(
      envelope({
        list: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      })
    )
  })

  await page.route('**/api/v1/organizations/options?**', async (route) => {
    await route.fulfill(envelope([]))
  })

  await page.route('**/api/v1/roles?**', async (route) => {
    await route.fulfill(envelope([]))
  })

  await page.goto(`${baseURL}/system/users`)
  await page.waitForLoadState('networkidle')

  const addButton = page.locator('button:has-text("添加")').first()

  await expect(page).toHaveURL(/\/system\/users$/)
  await expect(addButton).toBeVisible()
  await expect.poll(() => menuRequestCount).toBe(1)

  await page.evaluate(() => {
    window.dispatchEvent(new CustomEvent('ems4j:permission-denied'))
  })

  await expect.poll(() => menuRequestCount).toBe(2)
  await expect(page).toHaveURL(/\/system\/users$/)
  await expect(addButton).toBeHidden()
})
