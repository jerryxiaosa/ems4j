import { reactive, ref, type ComputedRef, type Ref } from 'vue'
import {
  createElectricMeter,
  fetchDeviceModelList,
  fetchElectricMeterDetail,
  removeElectricMeter,
  updateElectricMeterCt,
  updateElectricMeterProtect,
  updateElectricMeterSwitch,
  updateElectricMeter
} from '@/api/adapters/device'
import { findModelOption, type ElectricMeterFormValue, type ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import type { ElectricMeterNoticeType } from '@/modules/devices/electric-meters/composables/useElectricMeterNotice'
import {
  formatCommunicateModel,
  getElectricMeterErrorMessage,
  normalizeElectricMeterDetailRow,
  showNumberForCreate,
  toOptionalNumber
} from '@/modules/devices/electric-meters/composables/electricMeterShared'

type ElectricMeterNoticeSetter = (type: ElectricMeterNoticeType, text: string) => void

type ElectricMeterFormMode = 'create' | 'edit'

type MeterConfirmAction =
  | 'delete'
  | 'cut'
  | 'merge'
  | 'protect'
  | 'unprotect'
  | 'batch-cut'
  | 'batch-merge'
  | 'batch-protect'
  | 'batch-unprotect'
  | ''

export interface ElectricMeterMoreActionMenu {
  row: ElectricMeterItem
  top: number
  left: number
}

interface ConfirmState {
  visible: boolean
  title: string
  content: string
  subContent: string
  type: ElectricMeterNoticeType
  confirmText: string
  action: MeterConfirmAction
  targetId: number
}

interface UseElectricMeterActionsOptions {
  setNotice: ElectricMeterNoticeSetter
  loadMeterPage: () => Promise<void>
  syncSelection: () => void
  rows: Ref<ElectricMeterItem[]>
  selectedRows: ComputedRef<ElectricMeterItem[]>
  queryForm: {
    pageNum: number
    pageSize: number
  }
}

export const useElectricMeterActions = ({
  setNotice,
  loadMeterPage,
  syncSelection,
  rows,
  selectedRows,
  queryForm
}: UseElectricMeterActionsOptions) => {
  const editModalVisible = ref(false)
  const editMode = ref<ElectricMeterFormMode>('create')
  const editingMeter = ref<ElectricMeterItem | null>(null)

  const detailModalVisible = ref(false)
  const detailMeter = ref<ElectricMeterItem | null>(null)
  const detailLoading = ref(false)

  const moreActionMenu = ref<ElectricMeterMoreActionMenu | null>(null)

  const ctModalVisible = ref(false)
  const ctMeter = ref<ElectricMeterItem | null>(null)

  const confirmState = reactive<ConfirmState>({
    visible: false,
    title: '',
    content: '',
    subContent: '',
    type: 'info',
    confirmText: '确定',
    action: '',
    targetId: 0
  })

  const closeConfirm = () => {
    confirmState.visible = false
    confirmState.title = ''
    confirmState.content = ''
    confirmState.subContent = ''
    confirmState.type = 'info'
    confirmState.confirmText = '确定'
    confirmState.action = ''
    confirmState.targetId = 0
  }

  const closeMoreActionMenu = () => {
    moreActionMenu.value = null
  }

  const handleDocumentClick = (event: Event) => {
    const target = event.target as HTMLElement | null
    if (target?.closest('.meter-row-actions') || target?.closest('.more-action-floating-menu')) {
      return
    }
    closeMoreActionMenu()
  }

  const openConfirm = (payload: {
    title: string
    content: string
    subContent?: string
    type?: ElectricMeterNoticeType
    confirmText?: string
    action: Exclude<MeterConfirmAction, ''>
    targetId?: number
  }) => {
    confirmState.visible = true
    confirmState.title = payload.title
    confirmState.content = payload.content
    confirmState.subContent = payload.subContent || ''
    confirmState.type = payload.type || 'info'
    confirmState.confirmText = payload.confirmText || '确定'
    confirmState.action = payload.action
    confirmState.targetId = payload.targetId || 0
  }

  const openBatchProtectConfirm = (protect: boolean) => {
    if (selectedRows.value.length === 0) {
      setNotice('error', '请先勾选要操作的数据')
      return
    }

    openConfirm({
      title: protect ? '批量保电确认' : '批量取消保电确认',
      content: `电表数量：${selectedRows.value.length}`,
      subContent: `是否批量执行${protect ? '保电' : '取消保电'}操作？`,
      action: protect ? 'batch-protect' : 'batch-unprotect',
      type: 'info'
    })
  }

  const openCreateModal = () => {
    closeMoreActionMenu()
    editMode.value = 'create'
    editingMeter.value = null
    editModalVisible.value = true
  }

  const openEditModal = (row: ElectricMeterItem) => {
    closeMoreActionMenu()
    editMode.value = 'edit'
    editingMeter.value = { ...row }
    editModalVisible.value = true
  }

  const openDetailModal = async (row: ElectricMeterItem) => {
    closeMoreActionMenu()
    detailMeter.value = { ...row }
    detailModalVisible.value = true
    detailLoading.value = true

    try {
      const detail = await fetchElectricMeterDetail(row.id)

      if (!detail) {
        throw new Error('电表详情不存在')
      }

      detailMeter.value = normalizeElectricMeterDetailRow(detail, row)
    } catch (error) {
      detailModalVisible.value = false
      detailMeter.value = null
      setNotice('error', getElectricMeterErrorMessage(error))
    } finally {
      detailLoading.value = false
    }
  }

  const toggleMoreActionMenu = (row: ElectricMeterItem, event: MouseEvent) => {
    if (moreActionMenu.value?.row.id === row.id) {
      closeMoreActionMenu()
      return
    }

    const trigger = event.currentTarget as HTMLElement | null
    if (!trigger) {
      return
    }

    const rect = trigger.getBoundingClientRect()
    const menuWidth = 104
    moreActionMenu.value = {
      row: { ...row },
      top: rect.top - 2,
      left: Math.min(rect.right + 8, window.innerWidth - menuWidth - 12)
    }
  }

  const handleSubmitMeter = async (payload: ElectricMeterFormValue) => {
    if (editMode.value === 'create') {
      const fallbackModel = findModelOption(payload.modelId)
      const selectedModel = {
        id: Number(payload.modelId),
        modelName: payload.modelName || fallbackModel?.modelName || '',
        communicateModel: formatCommunicateModel(
          payload.communicateModel || fallbackModel?.communicateModel
        ),
        isNb: payload.isNb ?? fallbackModel?.isNb ?? false,
        isCt: payload.isCt ?? fallbackModel?.isCt ?? false,
        isPrepay: payload.isPrepay ?? fallbackModel?.isPrepay ?? true
      }

      if (!selectedModel.id || !selectedModel.modelName) {
        return
      }

      try {
        await createElectricMeter({
          spaceId: Number(payload.spaceId),
          meterName: payload.meterName.trim(),
          deviceNo: payload.deviceNo.trim() || undefined,
          isCalculate: payload.isCalculate === 'true',
          calculateType: undefined,
          isPrepay: payload.payType === '1',
          modelId: selectedModel.id,
          gatewayId: showNumberForCreate(selectedModel.isNb, payload.gatewayId),
          portNo: showNumberForCreate(selectedModel.isNb, payload.portNo),
          meterAddress: toOptionalNumber(payload.meterAddress),
          imei: selectedModel.isNb ? payload.imei.trim() || undefined : undefined,
          ct: selectedModel.isCt ? toOptionalNumber(payload.ct) : undefined
        })
        editModalVisible.value = false
        queryForm.pageNum = 1
        await loadMeterPage()
        setNotice('success', '电表添加成功')
      } catch (error) {
        setNotice('error', getElectricMeterErrorMessage(error))
      }
      return
    }

    if (editingMeter.value) {
      try {
        await updateElectricMeter(editingMeter.value.id, {
          spaceId: Number(payload.spaceId),
          meterName: payload.meterName.trim(),
          isCalculate: payload.isCalculate === 'true',
          isPrepay: payload.payType === '1'
        })
        editModalVisible.value = false
        await loadMeterPage()
        setNotice('success', '电表信息更新成功')
      } catch (error) {
        setNotice('error', getElectricMeterErrorMessage(error))
      }
      return
    }

    editModalVisible.value = false
    syncSelection()
  }

  const openDeleteConfirm = (row: ElectricMeterItem) => {
    closeMoreActionMenu()
    openConfirm({
      title: '删除确认',
      content: `是否删除电表“${row.meterName}”？`,
      action: 'delete',
      targetId: row.id,
      type: 'error'
    })
  }

  const openSingleCommandConfirm = (row: ElectricMeterItem, action: 'cut' | 'merge') => {
    closeMoreActionMenu()
    if (row.onlineStatus === 0) {
      setNotice('error', '无法对离线设备进行操作')
      return
    }
    openConfirm({
      title: action === 'cut' ? '断闸确认' : '合闸确认',
      content: `是否对电表“${row.meterName}”执行${action === 'cut' ? '断闸' : '合闸'}操作？`,
      action,
      targetId: row.id,
      type: 'info'
    })
  }

  const openSingleProtectConfirm = (row: ElectricMeterItem, protect: boolean) => {
    closeMoreActionMenu()
    openConfirm({
      title: protect ? '保电确认' : '取消保电确认',
      content: `是否对电表“${row.meterName}”执行${protect ? '保电' : '取消保电'}操作？`,
      action: protect ? 'protect' : 'unprotect',
      targetId: row.id,
      type: 'info'
    })
  }

  const openBatchCommandConfirm = (action: 'batch-cut' | 'batch-merge') => {
    if (selectedRows.value.length === 0) {
      setNotice('error', '请先勾选要操作的数据')
      return
    }
    if (selectedRows.value.some((row) => row.onlineStatus === 0)) {
      setNotice('error', '无法对离线设备进行操作')
      return
    }

    openConfirm({
      title: action === 'batch-cut' ? '批量断闸确认' : '批量合闸确认',
      content: `电表数量：${selectedRows.value.length}`,
      subContent: `是否批量执行${action === 'batch-cut' ? '断闸' : '合闸'}操作？`,
      action,
      type: 'info'
    })
  }

  const submitSwitchCommands = async (targetIds: number[], switchStatus: 0 | 1) => {
    const results = await Promise.allSettled(
      targetIds.map((id) =>
        updateElectricMeterSwitch({
          id,
          switchStatus
        })
      )
    )

    const successCount = results.filter((item) => item.status === 'fulfilled').length
    const failedCount = results.length - successCount

    return {
      successCount,
      failedCount
    }
  }

  const handleConfirm = async () => {
    switch (confirmState.action) {
      case 'delete':
        try {
          await removeElectricMeter(confirmState.targetId)
          const shouldFallbackPage = rows.value.length === 1 && queryForm.pageNum > 1
          if (shouldFallbackPage) {
            queryForm.pageNum -= 1
          }
          await loadMeterPage()
          syncSelection()
          setNotice('success', '电表删除成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'cut':
        try {
          await updateElectricMeterSwitch({
            id: confirmState.targetId,
            switchStatus: 1
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '断闸指令下发成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'merge':
        try {
          await updateElectricMeterSwitch({
            id: confirmState.targetId,
            switchStatus: 0
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '合闸指令下发成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'protect':
        try {
          await updateElectricMeterProtect({
            meterIds: [confirmState.targetId],
            protect: true
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '保电设置成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'unprotect':
        try {
          await updateElectricMeterProtect({
            meterIds: [confirmState.targetId],
            protect: false
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '取消保电成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'batch-cut': {
        const targetIds = selectedRows.value.map((item) => item.id)
        try {
          const result = await submitSwitchCommands(targetIds, 1)
          await loadMeterPage()
          syncSelection()
          if (result.failedCount === 0) {
            setNotice('success', '批量断闸指令下发成功')
          } else {
            setNotice(
              'error',
              `批量断闸部分失败：成功 ${result.successCount} 条，失败 ${result.failedCount} 条`
            )
          }
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      }
      case 'batch-merge': {
        const targetIds = selectedRows.value.map((item) => item.id)
        try {
          const result = await submitSwitchCommands(targetIds, 0)
          await loadMeterPage()
          syncSelection()
          if (result.failedCount === 0) {
            setNotice('success', '批量合闸指令下发成功')
          } else {
            setNotice(
              'error',
              `批量合闸部分失败：成功 ${result.successCount} 条，失败 ${result.failedCount} 条`
            )
          }
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      }
      case 'batch-protect':
        try {
          await updateElectricMeterProtect({
            meterIds: selectedRows.value.map((item) => item.id),
            protect: true
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '批量保电设置成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      case 'batch-unprotect':
        try {
          await updateElectricMeterProtect({
            meterIds: selectedRows.value.map((item) => item.id),
            protect: false
          })
          await loadMeterPage()
          syncSelection()
          setNotice('success', '批量取消保电成功')
        } catch (error) {
          setNotice('error', getElectricMeterErrorMessage(error))
        }
        break
      default:
        break
    }
    closeConfirm()
  }

  const openCtModal = async (row: ElectricMeterItem) => {
    closeMoreActionMenu()
    try {
      const modelList = await fetchDeviceModelList({
        typeKey: 'electricMeter'
      })
      const matchedModel = modelList.find((item) => item.id === row.modelId)

      if (matchedModel?.isCt !== true) {
        setNotice('error', '当前电表型号不支持修改 CT 变比')
        return
      }

      ctMeter.value = { ...row }
      ctModalVisible.value = true
    } catch (error) {
      setNotice('error', getElectricMeterErrorMessage(error))
    }
  }

  const handleSubmitCt = async (payload: { id: number; ct: string }) => {
    try {
      await updateElectricMeterCt({
        meterId: payload.id,
        ct: Number(payload.ct)
      })
      rows.value = rows.value.map((item) => {
        if (item.id !== payload.id) {
          return item
        }
        return {
          ...item,
          ct: payload.ct
        }
      })
      ctModalVisible.value = false
      setNotice('success', 'CT 变比设置成功')
    } catch (error) {
      setNotice('error', getElectricMeterErrorMessage(error))
    }
  }

  return {
    editModalVisible,
    editMode,
    editingMeter,
    detailModalVisible,
    detailMeter,
    detailLoading,
    moreActionMenu,
    ctModalVisible,
    ctMeter,
    confirmState,
    closeConfirm,
    closeMoreActionMenu,
    handleDocumentClick,
    openBatchProtectConfirm,
    openCreateModal,
    openEditModal,
    openDetailModal,
    toggleMoreActionMenu,
    handleSubmitMeter,
    openDeleteConfirm,
    openSingleCommandConfirm,
    openSingleProtectConfirm,
    openBatchCommandConfirm,
    handleConfirm,
    openCtModal,
    handleSubmitCt
  }
}
