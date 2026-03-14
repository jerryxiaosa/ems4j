import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface OrganizationRaw {
  id?: number
  organizationName?: string
  creditCode?: string
  organizationType?: number
  organizationTypeName?: string
  organizationAddress?: string
  entryDate?: string
  remark?: string
  ownAreaId?: number
  managerName?: string
  managerPhone?: string
  createTime?: string
  updateTime?: string
}

export interface OrganizationPageQueryRaw {
  organizationNameLike?: string
  pageNum?: number
  pageSize?: number
}

export interface OrganizationPageResultRaw {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: OrganizationRaw[]
}

export interface OrganizationCreatePayloadRaw {
  organizationName: string
  creditCode: string
  organizationType: number
  organizationAddress?: string
  managerName: string
  managerPhone: string
  entryDate?: string
  remark?: string
}

export interface OrganizationUpdatePayloadRaw {
  organizationName?: string
  creditCode?: string
  organizationType?: number
  organizationAddress?: string
  managerName?: string
  managerPhone?: string
  entryDate?: string
  remark?: string
}

export const getOrganizationPageRaw = (query: OrganizationPageQueryRaw) => {
  return requestV1<ApiEnvelope<OrganizationPageResultRaw>>({
    method: 'GET',
    url: '/organizations/page',
    params: {
      organizationNameLike: query.organizationNameLike,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getOrganizationDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<OrganizationRaw>>({
    method: 'GET',
    url: `/organizations/${id}`
  })
}

export const createOrganizationRaw = (payload: OrganizationCreatePayloadRaw) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/organizations',
    data: payload
  })
}

export const updateOrganizationRaw = (id: number, payload: OrganizationUpdatePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/organizations/${id}`,
    data: payload
  })
}

export const deleteOrganizationRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/organizations/${id}`
  })
}
