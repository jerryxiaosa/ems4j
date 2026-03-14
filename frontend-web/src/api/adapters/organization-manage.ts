import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import {
  createOrganizationRaw,
  deleteOrganizationRaw,
  getOrganizationDetailRaw,
  getOrganizationPageRaw,
  updateOrganizationRaw,
  type OrganizationCreatePayloadRaw,
  type OrganizationPageResultRaw,
  type OrganizationRaw,
  type OrganizationUpdatePayloadRaw
} from '@/api/raw/organization-manage'
import type { PageResult } from '@/types/http'
import type { SystemOrganizationItem } from '@/modules/system/organizations/types'

export interface OrganizationPageQuery {
  organizationNameLike?: string
  pageNum: number
  pageSize: number
}

export interface OrganizationSavePayload {
  organizationName?: string
  creditCode?: string
  organizationType?: number
  organizationAddress?: string
  managerName?: string
  managerPhone?: string
  entryDate?: string
  remark?: string
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value).trim()
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return undefined
}

const padNumber = (value: number) => String(value).padStart(2, '0')

const formatDateTime = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) {
    return ''
  }

  const text = value.trim()
  if (!text.includes('T')) {
    return text.length >= 19 ? text.slice(0, 19) : text
  }

  const date = new Date(text)
  if (Number.isNaN(date.getTime())) {
    const fallback = text.replace('T', ' ')
    return fallback.length >= 19 ? fallback.slice(0, 19) : fallback
  }

  return `${date.getFullYear()}-${padNumber(date.getMonth() + 1)}-${padNumber(
    date.getDate()
  )} ${padNumber(date.getHours())}:${padNumber(date.getMinutes())}:${padNumber(date.getSeconds())}`
}

const normalizeOrganization = (raw: OrganizationRaw): SystemOrganizationItem => {
  return {
    id: toNumber(raw.id) ?? 0,
    name: normalizeText(raw.organizationName),
    code: normalizeText(raw.creditCode),
    typeValue: (() => {
      const value = toNumber(raw.organizationType)
      return value === undefined ? '' : String(value)
    })(),
    typeName: normalizeText(raw.organizationTypeName),
    managerName: normalizeText(raw.managerName),
    managerPhone: normalizeText(raw.managerPhone),
    address: normalizeText(raw.organizationAddress),
    settledAt: normalizeText(raw.entryDate),
    createTime: formatDateTime(raw.createTime),
    updateTime: formatDateTime(raw.updateTime),
    remark: normalizeText(raw.remark)
  }
}

export const fetchOrganizationPage = async (
  query: OrganizationPageQuery
): Promise<PageResult<SystemOrganizationItem>> => {
  const payload = unwrapEnvelope<OrganizationPageResultRaw>(await getOrganizationPageRaw(query))
  const page = normalizePageResult(payload)
  return {
    ...page,
    list: page.list.map(normalizeOrganization)
  }
}

export const fetchOrganizationDetail = async (id: number): Promise<SystemOrganizationItem> => {
  return normalizeOrganization(unwrapEnvelope<OrganizationRaw>(await getOrganizationDetailRaw(id)))
}

export const createOrganization = async (payload: OrganizationSavePayload): Promise<number> => {
  const requestPayload: OrganizationCreatePayloadRaw = {
    organizationName: payload.organizationName || '',
    creditCode: payload.creditCode || '',
    organizationType: payload.organizationType || 0,
    organizationAddress: payload.organizationAddress,
    managerName: payload.managerName || '',
    managerPhone: payload.managerPhone || '',
    entryDate: payload.entryDate,
    remark: payload.remark
  }
  return unwrapEnvelope<number>(await createOrganizationRaw(requestPayload))
}

export const updateOrganization = async (id: number, payload: OrganizationSavePayload) => {
  const requestPayload: OrganizationUpdatePayloadRaw = {
    organizationName: payload.organizationName,
    creditCode: payload.creditCode,
    organizationType: payload.organizationType,
    organizationAddress: payload.organizationAddress,
    managerName: payload.managerName,
    managerPhone: payload.managerPhone,
    entryDate: payload.entryDate,
    remark: payload.remark
  }
  await unwrapEnvelope<void>(await updateOrganizationRaw(id, requestPayload))
}

export const deleteOrganization = async (id: number) => {
  await unwrapEnvelope<void>(await deleteOrganizationRaw(id))
}
