import type { EnumOption } from '@/api/adapters/system'

type EnumFetcher = (key: string) => Promise<EnumOption[]>

export interface DeviceOperationFilterOptions {
  commandTypeOptions: EnumOption[]
  deviceTypeOptions: EnumOption[]
}

export const loadDeviceOperationFilterOptions = async (
  fetchEnumOptionsByKey: EnumFetcher
): Promise<DeviceOperationFilterOptions> => {
  const [commandTypeOptions, deviceTypeOptions] = await Promise.all([
    fetchEnumOptionsByKey('commandType'),
    fetchEnumOptionsByKey('meterType')
  ])

  return {
    commandTypeOptions,
    deviceTypeOptions
  }
}

export const resolveDeviceTypeQueryValue = (meterTypeValue?: string): string | undefined => {
  if (!meterTypeValue) {
    return undefined
  }
  if (meterTypeValue === '1') {
    return 'electricMeter'
  }
  if (meterTypeValue === '2') {
    return 'waterMeter'
  }
  return undefined
}
