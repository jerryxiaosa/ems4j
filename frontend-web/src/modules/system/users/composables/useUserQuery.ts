import { reactive, ref, watch } from 'vue'
import { fetchUserPage } from '@/api/adapters/user'
import { searchOrganizationOptions, type OrganizationOption } from '@/api/adapters/organization'
import { fetchRoleOptions } from '@/api/adapters/role'
import { systemOrganizationOptions, systemRoleOptions } from '@/components/system/user.mock'
import type { UserNoticeType } from '@/modules/system/users/composables/useUserNotice'
import type { SystemUserItem } from '@/modules/system/users/types'
import { DEFAULT_USER_PAGE_SIZE, getUserErrorMessage } from '@/modules/system/users/composables/userShared'
import type { SystemOption } from '@/types/system'

type UserNoticeSetter = (type: UserNoticeType, text: string) => void

interface UserQueryFormState {
  username: string
  realName: string
  phone: string
  organizationKeyword: string
  organizationId: string
  roleId: string
  pageNum: number
  pageSize: number
}

interface UserAppliedFiltersState {
  username: string
  realName: string
  phone: string
  organizationId: string
  roleId: string
}

interface UseUserQueryOptions {
  setNotice: UserNoticeSetter
}

export const useUserQuery = ({ setNotice }: UseUserQueryOptions) => {
  const userRows = ref<SystemUserItem[]>([])
  const total = ref(0)
  const loading = ref(false)
  const organizationOptions = ref<SystemOption[]>(systemOrganizationOptions)
  const roleOptions = ref<SystemOption[]>(systemRoleOptions)

  const queryForm = reactive<UserQueryFormState>({
    username: '',
    realName: '',
    phone: '',
    organizationKeyword: '',
    organizationId: '',
    roleId: '',
    pageNum: 1,
    pageSize: DEFAULT_USER_PAGE_SIZE
  })

  const appliedFilters = reactive<UserAppliedFiltersState>({
    username: '',
    realName: '',
    phone: '',
    organizationId: '',
    roleId: ''
  })

  const syncAppliedFilters = () => {
    appliedFilters.username = queryForm.username
    appliedFilters.realName = queryForm.realName
    appliedFilters.phone = queryForm.phone
    appliedFilters.organizationId = queryForm.organizationId
    appliedFilters.roleId = queryForm.roleId
  }

  const handleOrganizationSelect = (option: OrganizationOption) => {
    queryForm.organizationKeyword = option.name
    queryForm.organizationId = String(option.id)
  }

  watch(
    () => queryForm.organizationKeyword,
    (value) => {
      const normalized = value.trim()
      const matched = organizationOptions.value.find((item) => item.label === normalized)
      queryForm.organizationId = matched ? matched.value : ''
    }
  )

  const loadOrganizationOptions = async () => {
    try {
      const list = await searchOrganizationOptions('', 200)
      if (list.length > 0) {
        organizationOptions.value = list.map((item) => ({
          value: String(item.id),
          label: item.name
        }))
      }
    } catch (error) {
      organizationOptions.value = systemOrganizationOptions
      setNotice('error', `机构列表加载失败：${getUserErrorMessage(error)}`)
    }
  }

  const loadRoleOptions = async () => {
    try {
      const list = await fetchRoleOptions()
      if (list.length > 0) {
        roleOptions.value = list
      }
    } catch (error) {
      roleOptions.value = systemRoleOptions
      setNotice('error', `角色列表加载失败：${getUserErrorMessage(error)}`)
    }
  }

  const loadUsers = async () => {
    loading.value = true
    userRows.value = []

    try {
      const result = await fetchUserPage({
        userNameLike: appliedFilters.username.trim() || undefined,
        realNameLike: appliedFilters.realName.trim() || undefined,
        userPhoneLike: appliedFilters.phone.trim() || undefined,
        organizationId: appliedFilters.organizationId ? Number(appliedFilters.organizationId) : undefined,
        roleId: appliedFilters.roleId ? Number(appliedFilters.roleId) : undefined,
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize
      })

      userRows.value = result.list
      total.value = result.total
      queryForm.pageNum = result.pageNum || queryForm.pageNum
      queryForm.pageSize = result.pageSize || queryForm.pageSize
    } catch (error) {
      userRows.value = []
      total.value = 0
      setNotice('error', `用户列表加载失败：${getUserErrorMessage(error)}`)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = async () => {
    syncAppliedFilters()
    queryForm.pageNum = 1
    await loadUsers()
  }

  const handleReset = async () => {
    queryForm.username = ''
    queryForm.realName = ''
    queryForm.phone = ''
    queryForm.organizationKeyword = ''
    queryForm.organizationId = ''
    queryForm.roleId = ''
    queryForm.pageNum = 1
    queryForm.pageSize = DEFAULT_USER_PAGE_SIZE

    appliedFilters.username = ''
    appliedFilters.realName = ''
    appliedFilters.phone = ''
    appliedFilters.organizationId = ''
    appliedFilters.roleId = ''

    await loadUsers()
  }

  const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
    if (loading.value) {
      return
    }

    queryForm.pageNum = payload.pageNum
    queryForm.pageSize = payload.pageSize
    await loadUsers()
  }

  const getSerialNumber = (index: number) => {
    return (queryForm.pageNum - 1) * queryForm.pageSize + index + 1
  }

  const initialize = async () => {
    await Promise.all([loadOrganizationOptions(), loadRoleOptions(), loadUsers()])
  }

  return {
    userRows,
    total,
    loading,
    organizationOptions,
    roleOptions,
    queryForm,
    appliedFilters,
    syncAppliedFilters,
    handleOrganizationSelect,
    loadOrganizationOptions,
    loadRoleOptions,
    loadUsers,
    handleSearch,
    handleReset,
    handlePageChange,
    getSerialNumber,
    initialize
  }
}
