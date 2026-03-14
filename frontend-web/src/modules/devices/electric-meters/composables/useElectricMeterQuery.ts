import { computed, reactive, ref } from 'vue'
import { fetchElectricMeterPage } from '@/api/adapters/device'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import type { ElectricMeterNoticeType } from '@/modules/devices/electric-meters/composables/useElectricMeterNotice'
import {
  DEFAULT_ELECTRIC_METER_PAGE_SIZE,
  getElectricMeterErrorMessage,
  normalizeElectricMeterRow
} from '@/modules/devices/electric-meters/composables/electricMeterShared'

type ElectricMeterNoticeSetter = (type: ElectricMeterNoticeType, text: string) => void

export interface ElectricMeterQueryFormState {
  searchKey: string
  onlineStatus: string
  status: string
  payType: string
  pageNum: number
  pageSize: number
}

interface UseElectricMeterQueryOptions {
  setNotice: ElectricMeterNoticeSetter
}

export const useElectricMeterQuery = ({ setNotice }: UseElectricMeterQueryOptions) => {
  const rows = ref<ElectricMeterItem[]>([])
  const loading = ref(false)
  const total = ref(0)
  const selectedIds = ref<number[]>([])

  const queryForm = reactive<ElectricMeterQueryFormState>({
    searchKey: '',
    onlineStatus: '',
    status: '',
    payType: '',
    pageNum: 1,
    pageSize: DEFAULT_ELECTRIC_METER_PAGE_SIZE
  })

  const pagedRows = computed(() => rows.value)
  const selectableRows = computed(() => pagedRows.value)
  const selectedRows = computed(() => {
    const selectedSet = new Set(selectedIds.value)
    return rows.value.filter((item) => selectedSet.has(item.id))
  })
  const isAllChecked = computed(() => {
    return (
      selectableRows.value.length > 0 &&
      selectableRows.value.every((row) => selectedIds.value.includes(row.id))
    )
  })

  const syncSelection = () => {
    const existingIds = new Set(rows.value.map((item) => item.id))
    selectedIds.value = selectedIds.value.filter((id) => existingIds.has(id))
  }

  const loadMeterPage = async () => {
    loading.value = true

    try {
      const page = await fetchElectricMeterPage({
        searchKey: queryForm.searchKey.trim() || undefined,
        isOnline: queryForm.onlineStatus ? queryForm.onlineStatus === 'true' : undefined,
        isCutOff: queryForm.status ? queryForm.status === 'true' : undefined,
        isPrepay: queryForm.payType ? queryForm.payType === 'true' : undefined,
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize
      })

      rows.value = page.list.map(normalizeElectricMeterRow)
      total.value = page.total
      syncSelection()
    } catch (error) {
      rows.value = []
      total.value = 0
      selectedIds.value = []
      setNotice('error', getElectricMeterErrorMessage(error))
    } finally {
      loading.value = false
    }
  }

  const toggleSelectAllOnPage = (checked: boolean) => {
    const selectedSet = new Set(selectedIds.value)
    selectableRows.value.forEach((row) => {
      if (checked) {
        selectedSet.add(row.id)
      } else {
        selectedSet.delete(row.id)
      }
    })
    selectedIds.value = Array.from(selectedSet)
  }

  const toggleRowSelection = (row: ElectricMeterItem, checked: boolean) => {
    const selectedSet = new Set(selectedIds.value)
    if (checked) {
      selectedSet.add(row.id)
    } else {
      selectedSet.delete(row.id)
    }
    selectedIds.value = Array.from(selectedSet)
  }

  const handleSearch = async () => {
    queryForm.pageNum = 1
    await loadMeterPage()
  }

  const handleReset = async () => {
    queryForm.searchKey = ''
    queryForm.onlineStatus = ''
    queryForm.status = ''
    queryForm.payType = ''
    queryForm.pageNum = 1
    queryForm.pageSize = DEFAULT_ELECTRIC_METER_PAGE_SIZE
    await loadMeterPage()
  }

  const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
    if (loading.value) {
      return
    }

    queryForm.pageNum = payload.pageNum
    queryForm.pageSize = payload.pageSize
    await loadMeterPage()
  }

  const getSerialNumber = (index: number) => {
    return (queryForm.pageNum - 1) * queryForm.pageSize + index + 1
  }

  const initialize = async () => {
    await loadMeterPage()
  }

  return {
    rows,
    loading,
    total,
    queryForm,
    selectedIds,
    pagedRows,
    selectableRows,
    selectedRows,
    isAllChecked,
    syncSelection,
    loadMeterPage,
    toggleSelectAllOnPage,
    toggleRowSelection,
    handleSearch,
    handleReset,
    handlePageChange,
    getSerialNumber,
    initialize
  }
}
