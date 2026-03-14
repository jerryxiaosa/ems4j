import { reactive, ref, type Ref } from 'vue'
import {
  createUser,
  deleteUser,
  fetchUserDetailRaw,
  resetUserPassword,
  updateUser
} from '@/api/adapters/user'
import type { UserNoticeType } from '@/modules/system/users/composables/useUserNotice'
import type { SystemUserFormValue, SystemUserItem } from '@/modules/system/users/types'
import { getUserErrorMessage } from '@/modules/system/users/composables/userShared'

type UserNoticeSetter = (type: UserNoticeType, text: string) => void

type UserFormMode = 'create' | 'edit'

interface UserQueryFormLike {
  pageNum: number
  pageSize: number
}

interface UseUserCrudOptions {
  setNotice: UserNoticeSetter
  loadUsers: () => Promise<void>
  syncAppliedFilters: () => void
  queryForm: UserQueryFormLike
  total: Ref<number>
}

export const useUserCrud = ({
  setNotice,
  loadUsers,
  syncAppliedFilters,
  queryForm,
  total
}: UseUserCrudOptions) => {
  const formModalVisible = ref(false)
  const formMode = ref<UserFormMode>('create')
  const currentUser = ref<SystemUserItem | null>(null)
  const detailVisible = ref(false)
  const passwordVisible = ref(false)
  const confirmState = reactive({
    visible: false,
    target: null as SystemUserItem | null
  })

  const openCreateModal = () => {
    formMode.value = 'create'
    currentUser.value = null
    formModalVisible.value = true
  }

  const openEditModal = async (row: SystemUserItem) => {
    formMode.value = 'edit'

    try {
      currentUser.value = await fetchUserDetailRaw(row.id)
      formModalVisible.value = true
    } catch (error) {
      setNotice('error', `用户详情加载失败：${getUserErrorMessage(error)}`)
    }
  }

  const openDetailModal = (row: SystemUserItem) => {
    currentUser.value = row
    detailVisible.value = true
  }

  const openPasswordModal = (row: SystemUserItem) => {
    currentUser.value = row
    passwordVisible.value = true
  }

  const openDeleteConfirm = (row: SystemUserItem) => {
    confirmState.target = row
    confirmState.visible = true
  }

  const closeDeleteConfirm = () => {
    confirmState.visible = false
    confirmState.target = null
  }

  const handleFormSubmit = async (payload: SystemUserFormValue) => {
    if (formMode.value === 'create') {
      try {
        await createUser({
          userName: payload.username.trim(),
          password: payload.password.trim(),
          realName: payload.realName.trim(),
          userPhone: payload.phone.trim(),
          userGender: Number(payload.userGender),
          certificatesType: payload.certificatesType ? Number(payload.certificatesType) : undefined,
          certificatesNo: payload.certificatesNo.trim() || undefined,
          organizationId: Number(payload.organizationId),
          roleIds: payload.roleIds.map((item) => Number(item)),
          remark: payload.remark.trim() || undefined
        })
        formModalVisible.value = false
        setNotice('success', '用户新增成功')
        queryForm.pageNum = 1
        syncAppliedFilters()
        await loadUsers()
      } catch (error) {
        setNotice('error', `用户新增失败：${getUserErrorMessage(error)}`)
      }
      return
    }

    if (payload.id != null) {
      try {
        await updateUser(payload.id, {
          realName: payload.realName.trim(),
          userPhone: payload.phone.trim(),
          userGender: Number(payload.userGender),
          certificatesType: payload.certificatesType ? Number(payload.certificatesType) : undefined,
          certificatesNo: payload.certificatesNo.trim() || undefined,
          organizationId: Number(payload.organizationId),
          roleIds: payload.roleIds.map((item) => Number(item)),
          remark: payload.remark.trim() || undefined
        })
        formModalVisible.value = false
        setNotice('success', '用户编辑成功')
        await loadUsers()
      } catch (error) {
        setNotice('error', `用户编辑失败：${getUserErrorMessage(error)}`)
      }
      return
    }

    formModalVisible.value = false
  }

  const handlePasswordSubmit = async (payload: { password: string; confirmPassword: string }) => {
    if (!currentUser.value?.id) {
      setNotice('error', '缺少用户ID，无法重置密码')
      return
    }

    try {
      await resetUserPassword(currentUser.value.id, {
        newPassword: payload.password.trim()
      })
      passwordVisible.value = false
      setNotice('success', '密码重置成功')
    } catch (error) {
      setNotice('error', `密码重置失败：${getUserErrorMessage(error)}`)
    }
  }

  const handleConfirmDelete = async () => {
    if (!confirmState.target) {
      return
    }

    const targetId = confirmState.target.id
    closeDeleteConfirm()

    try {
      await deleteUser(targetId)
      setNotice('success', '用户删除成功')

      const nextTotal = Math.max(0, total.value - 1)
      const maxPageNum = Math.max(1, Math.ceil(nextTotal / queryForm.pageSize))
      if (queryForm.pageNum > maxPageNum) {
        queryForm.pageNum = maxPageNum
      }

      await loadUsers()
    } catch (error) {
      setNotice('error', `用户删除失败：${getUserErrorMessage(error)}`)
    }
  }

  return {
    formModalVisible,
    formMode,
    currentUser,
    detailVisible,
    passwordVisible,
    confirmState,
    openCreateModal,
    openEditModal,
    openDetailModal,
    openPasswordModal,
    openDeleteConfirm,
    closeDeleteConfirm,
    handleFormSubmit,
    handlePasswordSubmit,
    handleConfirmDelete
  }
}
