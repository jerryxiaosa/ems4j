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
  },
  {
    id: 112,
    pid: 110,
    menuName: '编辑',
    menuKey: 'system_management_user_management_edit',
    path: '',
    sortNum: 2,
    menuType: 2,
    icon: '',
    hidden: false,
    menuSource: 1
  }
]

const createMaskedUser = (realName) => ({
  id: 1,
  userName: 'tester',
  organizationId: 9,
  organizationName: '测试机构',
  realName,
  userPhone: '138****0000',
  userGender: 1,
  remark: '原始备注',
  certificatesType: 1,
  certificatesTypeText: '身份证',
  certificatesNo: '110**********1234',
  createTime: '2026-03-14 09:00:00',
  updateTime: '2026-03-14 09:30:00',
  roles: [
    {
      id: 3,
      roleName: '企业管理员',
      roleKey: 'enterprise_admin'
    }
  ]
})

const createRawUser = (realName) => ({
  id: 1,
  userName: 'tester',
  organizationId: 9,
  organizationName: '测试机构',
  realName,
  userPhone: '13800000000',
  userGender: 1,
  remark: '原始备注',
  certificatesType: 1,
  certificatesTypeText: '身份证',
  certificatesNo: '110101199001011234',
  createTime: '2026-03-14 09:00:00',
  updateTime: '2026-03-14 09:30:00',
  roles: [
    {
      id: 3,
      roleName: '企业管理员',
      roleKey: 'enterprise_admin'
    }
  ]
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('user management smoke flow supports query detail and edit raw submit', async ({ page }) => {
  let userRealName = '测试用户'
  const updatedRealName = '测试用户-已编辑'
  let lastUserNameLike = ''
  let userPageRequestCount = 0
  let updatePayload = null
  let rawDetailRequestCount = 0

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route('**/api/v1/users/page?**', async (route) => {
    const requestUrl = new URL(route.request().url())
    lastUserNameLike = requestUrl.searchParams.get('userNameLike') || ''
    userPageRequestCount += 1
    await route.fulfill(
      envelope({
        list: [createMaskedUser(userRealName)],
        total: 1,
        pageNum: 1,
        pageSize: 10
      })
    )
  })

  await page.route(/\/api\/v1\/users\/1\/raw$/, async (route) => {
    rawDetailRequestCount += 1
    await route.fulfill(envelope(createRawUser(userRealName)))
  })

  await page.route(/\/api\/v1\/users\/1$/, async (route) => {
    const method = route.request().method()

    if (method === 'GET') {
      await route.fulfill(envelope(createMaskedUser(userRealName)))
      return
    }

    if (method === 'PUT') {
      updatePayload = route.request().postDataJSON()
      userRealName = updatePayload.realName
      await route.fulfill(envelope(null))
      return
    }

    await route.continue()
  })

  await page.route('**/api/v1/organizations/options?**', async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 9,
          organizationName: '测试机构',
          managerName: '张三',
          managerPhone: '13800000000'
        }
      ])
    )
  })

  await page.route('**/api/v1/roles?**', async (route) => {
    await route.fulfill(
      envelope([
        {
          id: 3,
          roleName: '企业管理员',
          roleKey: 'enterprise_admin'
        }
      ])
    )
  })

  await page.route(/\/api\/v1\/system\/enums(?:\?.*)?$/, async (route) => {
    await route.fulfill(
      envelope({
        certificatesType: [
          { value: 1, info: '身份证' },
          { value: 2, info: '护照' }
        ]
      })
    )
  })

  await page.goto(`${baseURL}/system/users`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/system\/users$/)
  await expect(page.locator('tbody tr').first()).toContainText('tester')
  await expect(page.locator('tbody tr').first()).toContainText('138****0000')

  await page.getByPlaceholder('请输入用户名').fill('tester')
  await page.getByRole('button', { name: '查询' }).click()

  await expect.poll(() => lastUserNameLike).toBe('tester')

  const firstRow = page.locator('tbody tr').first()
  await firstRow.getByRole('button', { name: '详情' }).click()

  const detailModal = page.locator('.user-detail-modal')
  await expect(detailModal).toBeVisible()
  await expect(detailModal).toContainText('138****0000')
  await expect(detailModal).toContainText('110**********1234')
  await detailModal.getByRole('button', { name: '关闭' }).click()
  await expect(detailModal).toBeHidden()

  await firstRow.getByRole('button', { name: '编辑' }).click()
  const editModal = page.locator('.user-form-modal')
  await expect(editModal).toBeVisible()
  await expect.poll(() => rawDetailRequestCount).toBe(1)
  await expect(editModal.getByPlaceholder('请输入手机号码')).toHaveValue('13800000000')
  await expect(editModal.getByPlaceholder('请输入证件号码')).toHaveValue('110101199001011234')

  await editModal.getByPlaceholder('请输入姓名').fill(updatedRealName)
  await editModal.getByRole('button', { name: '确定' }).click({ force: true, noWaitAfter: true })

  await expect.poll(() => updatePayload).not.toBeNull()
  expect(updatePayload).toMatchObject({
    realName: updatedRealName,
    userPhone: '13800000000',
    certificatesNo: '110101199001011234',
    organizationId: 9,
    roleIds: [3]
  })

  await expect(page.locator('tbody tr').first()).toContainText(updatedRealName)
  await expect.poll(() => userPageRequestCount >= 3).toBe(true)
})
