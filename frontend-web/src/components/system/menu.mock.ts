import type { SystemMenuFormValue, SystemMenuItem } from '@/modules/system/menus/types'

export const systemMenuMockTree: SystemMenuItem[] = [
  {
    id: 1,
    parentId: null,
    name: '后台',
    key: 'backend',
    routePath: '/subapp',
    backendApi: '/users/current/menus',
    permissionCodes: ['/users/current/menus'],
    categoryValue: '1',
    categoryName: '菜单',
    platformValue: '1',
    platformName: '后台',
    hiddenValue: 'false',
    hiddenName: '否',
    icon: 'dashboard',
    sortNum: 1,
    remark: '',
    children: [
      {
        id: 11,
        parentId: 1,
        name: '企业档案',
        key: 'company',
        routePath: '/subapp/company',
        backendApi: '/organizations/page',
        permissionCodes: ['/organizations/page'],
        categoryValue: '3',
        categoryName: '子系统',
        platformValue: '1',
        platformName: '后台',
        hiddenValue: 'false',
        hiddenName: '否',
        icon: 'company',
        sortNum: 21,
        remark: '',
        children: [
          {
            id: 111,
            parentId: 11,
            name: '企业信息',
            key: 'enterprise_info',
            routePath: '/enterprise-info',
            backendApi: '/organizations/{id}',
            permissionCodes: ['/organizations/{id}'],
            categoryValue: '1',
            categoryName: '菜单',
            platformValue: '1',
            platformName: '后台',
            hiddenValue: 'false',
            hiddenName: '否',
            icon: '',
            sortNum: 1,
            remark: ''
          },
          {
            id: 112,
            parentId: 11,
            name: '机构管理',
            key: 'organization_manage',
            routePath: '/organization-manage',
            backendApi: '/organizations/page',
            permissionCodes: ['/organizations/page'],
            categoryValue: '1',
            categoryName: '菜单',
            platformValue: '1',
            platformName: '后台',
            hiddenValue: 'false',
            hiddenName: '否',
            icon: '',
            sortNum: 2,
            remark: ''
          }
        ]
      },
      {
        id: 12,
        parentId: 1,
        name: '空间系统',
        key: 'space_system',
        routePath: '/subapp/space',
        backendApi: '/spaces/tree',
        permissionCodes: ['/spaces/tree'],
        categoryValue: '3',
        categoryName: '子系统',
        platformValue: '1',
        platformName: '后台',
        hiddenValue: 'false',
        hiddenName: '否',
        icon: 'space',
        sortNum: 27,
        remark: ''
      },
      {
        id: 13,
        parentId: 1,
        name: '系统管理',
        key: 'system_manage',
        routePath: '/subapp/settings',
        backendApi: '/users/page',
        permissionCodes: ['/users/page'],
        categoryValue: '3',
        categoryName: '子系统',
        platformValue: '1',
        platformName: '后台',
        hiddenValue: 'false',
        hiddenName: '否',
        icon: 'settings',
        sortNum: 39,
        remark: ''
      }
    ]
  }
]

export const systemMenuCategoryOptions = [
  { value: '1', label: '菜单' },
  { value: '2', label: '按钮' },
  { value: '3', label: '子系统' }
]

export const systemMenuPlatformOptions = [
  { value: '1', label: '后台' },
  { value: '2', label: '移动端' }
]

export const systemMenuHiddenOptions = [
  { value: 'true', label: '是' },
  { value: 'false', label: '否' }
]

export const systemMenuBackendApiOptions = [
  { value: '/users/page', label: '/users/page' },
  { value: '/users/{id}', label: '/users/{id}' },
  { value: '/roles', label: '/roles' },
  { value: '/roles/{id}', label: '/roles/{id}' },
  { value: '/menus', label: '/menus' },
  { value: '/organizations/page', label: '/organizations/page' },
  { value: '/spaces/tree', label: '/spaces/tree' }
]

export const createDefaultSystemMenuForm = (): SystemMenuFormValue => ({
  parentId: '',
  parentName: '顶级菜单',
  name: '',
  key: '',
  routePath: '',
  backendApis: [],
  categoryValue: '1',
  platformValue: '1',
  hiddenValue: 'false',
  icon: '',
  sortNum: '1',
  remark: ''
})
