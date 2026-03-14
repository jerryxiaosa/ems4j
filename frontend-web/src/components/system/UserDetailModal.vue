<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { fetchUserDetail } from '@/api/adapters/user'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { SystemUserItem } from '@/modules/system/users/types'

interface DetailCell {
  label: string
  value: string
}

const props = defineProps<{
  modelValue: boolean
  user: SystemUserItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const loading = ref(false)
const detail = ref<SystemUserItem | null>(null)
const errorText = ref('')

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const rows = computed(() => {
  const user = detail.value
  if (!user) {
    return []
  }

  const cells: DetailCell[] = [
    { label: '用户账号', value: user.username || '--' },
    { label: '姓名', value: user.realName || '--' },
    { label: '手机号码', value: user.phone || '--' },
    { label: '所属机构', value: user.organizationName || '--' },
    { label: '角色', value: user.roleName || '--' },
    { label: '性别', value: user.genderName || '--' },
    { label: '证件类型', value: user.certificatesTypeText || '--' },
    { label: '证件号码', value: user.certificatesNo || '--' },
    { label: '添加时间', value: user.createTime || '--' },
    { label: '更新时间', value: user.updateTime || '--' }
  ]

  const groups: Array<[DetailCell, DetailCell | null]> = []
  for (let index = 0; index < cells.length; index += 2) {
    groups.push([cells[index], cells[index + 1] ?? null])
  }
  return groups
})

const loadDetail = async () => {
  if (!props.user?.id) {
    detail.value = null
    errorText.value = ''
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    detail.value = await fetchUserDetail(props.user.id)
  } catch (error) {
    detail.value = null
    errorText.value = `用户详情加载失败：${getErrorMessage(error)}`
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void loadDetail()
      return
    }
    loading.value = false
    detail.value = null
    errorText.value = ''
  }
)

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="user-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel user-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">用户详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <UiLoadingState v-if="loading" :size="20" :thickness="2" :min-height="120" />
          <UiErrorState v-else-if="errorText" :text="errorText" :min-height="120" />
          <UiEmptyState v-else-if="!detail" text="暂无详情数据" :min-height="72" />
          <template v-else>
            <div class="summary-grid">
              <div v-for="[leftCell, rightCell] in rows" :key="leftCell.label" class="summary-row">
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
              <div class="summary-row summary-row-full">
                <div class="summary-item summary-item-full">
                  <span class="summary-label es-detail-label">备注</span>
                  <span class="summary-value es-detail-value-box summary-remark">{{
                    detail.remark || '--'
                  }}</span>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 42;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.user-detail-modal {
  display: flex;
  width: min(820px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  flex-direction: column;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
}

.modal-title {
  margin: 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.icon-btn {
  height: 36px;
  min-width: 64px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  flex: 1;
  min-height: 0;
  padding: 20px 20px 32px;
  overflow: auto;
}

.summary-grid {
  display: grid;
  gap: 10px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 56px;
}

.summary-row-full {
  grid-template-columns: minmax(0, 1fr);
}

.summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
  min-width: 0;
}

.summary-item-full {
  grid-template-columns: 96px minmax(0, 1fr);
}

.summary-remark {
  min-height: 68px;
  align-items: flex-start;
  line-height: 1.6;
}

.user-detail-fade-enter-active,
.user-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.user-detail-fade-enter-from,
.user-detail-fade-leave-to {
  opacity: 0;
}

.user-detail-fade-enter-from .user-detail-modal,
.user-detail-fade-leave-to .user-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
