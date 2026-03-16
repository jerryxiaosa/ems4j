import { describe, expect, test, vi } from 'vitest'
import { loadDeviceOperationFilterOptions, resolveDeviceTypeQueryValue } from './device-operation-filter'

describe('device-operation filter', () => {
  test('loadDeviceOperationFilterOptions should read commandType and meterType enums', async () => {
    const fetchEnumOptionsByKey = vi.fn(async (key: string) => {
      if (key === 'commandType') {
        return [{ label: '设置CT变比', value: '5' }]
      }
      if (key === 'meterType') {
        return [{ label: '电表', value: '1' }]
      }
      return []
    })

    const result = await loadDeviceOperationFilterOptions(fetchEnumOptionsByKey)

    expect(fetchEnumOptionsByKey).toHaveBeenCalledTimes(2)
    expect(fetchEnumOptionsByKey).toHaveBeenNthCalledWith(1, 'commandType')
    expect(fetchEnumOptionsByKey).toHaveBeenNthCalledWith(2, 'meterType')
    expect(result.commandTypeOptions).toEqual([{ label: '设置CT变比', value: '5' }])
    expect(result.deviceTypeOptions).toEqual([{ label: '电表', value: '1' }])
  })

  test('resolveDeviceTypeQueryValue should map meterType code to device type key', () => {
    expect(resolveDeviceTypeQueryValue('1')).toBe('electricMeter')
    expect(resolveDeviceTypeQueryValue('2')).toBe('waterMeter')
    expect(resolveDeviceTypeQueryValue('')).toBeUndefined()
    expect(resolveDeviceTypeQueryValue('3')).toBeUndefined()
  })
})
