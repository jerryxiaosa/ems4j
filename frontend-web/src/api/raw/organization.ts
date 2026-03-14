import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface OrganizationOptionRaw {
  id?: number | string
  organizationId?: number | string
  name?: string
  organizationName?: string
  managerName?: string
  managerPhone?: string
  contactName?: string
  contactPhone?: string
}

export const getOrganizationOptionsRaw = (organizationNameLike?: string, limit = 10) => {
  return requestV1<ApiEnvelope<OrganizationOptionRaw[]>>({
    method: 'GET',
    url: '/organizations/options',
    params: {
      organizationNameLike,
      limit
    }
  })
}
