import { getOrganizationOptionsRaw, type OrganizationOptionRaw } from '@/api/raw/organization'
import { unwrapEnvelope } from '@/api/raw/types'

export interface OrganizationOption {
  id: number
  name: string
  managerName: string
  managerPhone: string
}

const toNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return null
}

const normalizeOrganizationOption = (raw: OrganizationOptionRaw): OrganizationOption | null => {
  const id = toNumber(raw.id ?? raw.organizationId)
  const name = (raw.organizationName || raw.name || '').trim()
  if (id === null || !name) {
    return null
  }

  return {
    id,
    name,
    managerName: (raw.managerName || raw.contactName || '').trim(),
    managerPhone: (raw.managerPhone || raw.contactPhone || '').trim()
  }
}

export const searchOrganizationOptions = async (
  keyword: string,
  limit = 10
): Promise<OrganizationOption[]> => {
  const list =
    unwrapEnvelope<OrganizationOptionRaw[]>(
      await getOrganizationOptionsRaw(keyword.trim() || undefined, limit)
    ) || []
  return list
    .map(normalizeOrganizationOption)
    .filter((item): item is OrganizationOption => item !== null)
}
