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
    id: 120,
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
    id: 130,
    pid: 120,
    menuName: '机构管理',
    menuKey: 'system_management_organization_management',
    path: '/system/organizations',
    sortNum: 2,
    menuType: 1,
    icon: '',
    hidden: false,
    menuSource: 1
  },
  {
    id: 131,
    pid: 130,
    menuName: '详情',
    menuKey: 'system_management_organization_management_detail',
    path: '',
    sortNum: 1,
    menuType: 2,
    icon: '',
    hidden: false,
    menuSource: 1
  },
  {
    id: 132,
    pid: 130,
    menuName: '编辑',
    menuKey: 'system_management_organization_management_edit',
    path: '',
    sortNum: 2,
    menuType: 2,
    icon: '',
    hidden: false,
    menuSource: 1
  }
]

const createOrganization = (organizationName) => ({
  id: 1,
  organizationName,
  creditCode: 'ORG001',
  organizationType: 1,
  organizationTypeName: '企业',
  organizationAddress: '苏州工业园区星湖街 99 号',
  entryDate: '2026-03-01',
  remark: '测试机构备注',
  managerName: '张三',
  managerPhone: '13800000000',
  createTime: '2026-03-14 09:00:00',
  updateTime: '2026-03-14 09:30:00'
})

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('energy_access_token', 'fake-token')
  })
})

test('organization management smoke flow supports query detail and edit save', async ({ page }) => {
  let organizationName = '测试机构'
  const updatedOrganizationName = '测试机构-已编辑'
  let lastOrganizationNameLike = ''
  let organizationPageRequestCount = 0
  let detailRequestCount = 0
  let updatePayload = null

  await page.route(/\/api\/v1\/users\/current\/menus(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(buildMenuPayload()))
  })

  await page.route(/\/api\/v1\/users\/current(?:\?.*)?$/, async (route) => {
    await route.fulfill(envelope(currentUser))
  })

  await page.route(/\/api\/v1\/system\/enums(?:\?.*)?$/, async (route) => {
    await route.fulfill(
      envelope({
        organizationType: [
          { value: 1, info: '企业' },
          { value: 2, info: '机构' }
        ]
      })
    )
  })

  await page.route('**/api/v1/organizations/page?**', async (route) => {
    const requestUrl = new URL(route.request().url())
    lastOrganizationNameLike = requestUrl.searchParams.get('organizationNameLike') || ''
    organizationPageRequestCount += 1
    await route.fulfill(
      envelope({
        list: [createOrganization(organizationName)],
        total: 1,
        pageNum: 1,
        pageSize: 10
      })
    )
  })

  await page.route(/\/api\/v1\/organizations\/1$/, async (route) => {
    const method = route.request().method()

    if (method === 'GET') {
      detailRequestCount += 1
      await route.fulfill(envelope(createOrganization(organizationName)))
      return
    }

    if (method === 'PUT') {
      updatePayload = route.request().postDataJSON()
      organizationName = updatePayload.organizationName || organizationName
      await route.fulfill(envelope(null))
      return
    }

    await route.continue()
  })

  await page.goto(`${baseURL}/system/organizations`)
  await page.waitForLoadState('networkidle')

  await expect(page).toHaveURL(/\/system\/organizations$/)
  await expect(page.locator('tbody tr').first()).toContainText(organizationName)

  await page.getByPlaceholder('请输入机构名称').fill('测试机构')
  await page.getByRole('button', { name: '查询' }).click()

  await expect.poll(() => lastOrganizationNameLike).toBe('测试机构')

  const firstRow = page.locator('tbody tr').first()
  await firstRow.getByRole('button', { name: '详情' }).click()

  const detailModal = page.locator('.organization-detail-modal')
  await expect(detailModal).toBeVisible()
  await expect(detailModal).toContainText('企业')
  await expect(detailModal).toContainText('苏州工业园区星湖街 99 号')
  await detailModal.getByRole('button', { name: '关闭' }).click()
  await expect(detailModal).toBeHidden()

  await firstRow.getByRole('button', { name: '修改' }).click()
  const editModal = page.locator('.organization-form-modal')
  await expect(editModal).toBeVisible()
  await expect.poll(() => detailRequestCount).toBe(2)
  await expect(editModal.getByPlaceholder('请输入机构名称')).toHaveValue('测试机构')
  await expect(editModal.getByPlaceholder('请输入负责人电话')).toHaveValue('13800000000')

  await editModal.getByPlaceholder('请输入机构名称').fill(updatedOrganizationName)
  await editModal.getByRole('button', { name: '确定' }).click({ force: true, noWaitAfter: true })

  await expect.poll(() => updatePayload).not.toBeNull()
  expect(updatePayload).toMatchObject({
    organizationName: updatedOrganizationName,
    creditCode: 'ORG001',
    organizationType: 1,
    organizationAddress: '苏州工业园区星湖街 99 号',
    managerName: '张三',
    managerPhone: '13800000000',
    entryDate: '2026-03-01',
    remark: '测试机构备注'
  })

  await expect(page.locator('tbody tr').first()).toContainText(updatedOrganizationName)
  await expect.poll(() => organizationPageRequestCount >= 3).toBe(true)
})
