const { chromium } = require('playwright')

const baseURL = process.env.TEST_BASE_URL || 'http://127.0.0.1:4173'
const permissionDeniedEvent = 'ems4j:permission-denied'

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

const assert = (condition, message) => {
  if (!condition) {
    throw new Error(message)
  }
}

async function createContext(browser) {
  const context = await browser.newContext()
  await context.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
  return context
}

async function testMenuLoadFailureRedirects() {
  const browser = await chromium.launch({ headless: true })
  const context = await createContext(browser)
  const page = await context.newPage()

  await page.route('**/api/v1/users/current/menus?**', async (route) => {
    await route.abort('failed')
  })

  await page.goto(`${baseURL}/accounts/info`)
  await page.waitForURL(/\/login(?:\?|$)/, { timeout: 10000 })

  assert(/\/login(?:\?|$)/.test(page.url()), '菜单接口失败后未跳转到登录页')

  await browser.close()
  return '菜单接口失败后受保护路由会跳转到登录页'
}

async function testDirectiveReactsToPermissionRefresh() {
  const browser = await chromium.launch({ headless: true })
  const context = await createContext(browser)
  const page = await context.newPage()
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

  await addButton.waitFor({ state: 'visible', timeout: 10000 })
  assert(menuRequestCount === 1, `初始菜单请求次数异常: ${menuRequestCount}`)

  await page.evaluate((eventName) => {
    window.dispatchEvent(new CustomEvent(eventName))
  }, permissionDeniedEvent)

  await page.waitForFunction(() => {
    const button = Array.from(document.querySelectorAll('button')).find((item) =>
      item.textContent?.includes('添加')
    )
    return !button || window.getComputedStyle(button).display === 'none'
  }, undefined, { timeout: 10000 })

  assert(menuRequestCount === 2, `权限刷新后菜单未重新请求，当前次数: ${menuRequestCount}`)
  assert(/\/system\/users$/.test(page.url()), '权限刷新后页面不应离开用户管理')

  await browser.close()
  return '权限刷新后当前页受指令控制的按钮会即时隐藏'
}

async function main() {
  const results = []
  results.push(await testMenuLoadFailureRedirects())
  results.push(await testDirectiveReactsToPermissionRefresh())
  console.log(JSON.stringify({ ok: true, results }, null, 2))
}

main().catch((error) => {
  console.error(JSON.stringify({ ok: false, error: error.message }, null, 2))
  process.exit(1)
})
