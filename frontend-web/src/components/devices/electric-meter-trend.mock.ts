import type {
  ElectricMeterPowerConsumeTrendPoint,
  ElectricMeterPowerTrendQuery,
  LatestPowerRecord
} from '@/types/device'

const formatDateTime = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const parseDateTime = (value: string) => {
  const normalized = value.trim().replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export const buildMockElectricMeterPowerTrend = (
  id: number,
  query: ElectricMeterPowerTrendQuery
): LatestPowerRecord[] => {
  const startTime = parseDateTime(query.beginTime)
  const endTime = parseDateTime(query.endTime)
  if (!startTime || !endTime || startTime.getTime() >= endTime.getTime()) {
    return []
  }

  const pointCount = 12
  const totalDuration = endTime.getTime() - startTime.getTime()
  const interval = totalDuration / (pointCount - 1)
  const basePower = 120 + id * 4.5

  return Array.from({ length: pointCount }, (_, index) => {
    const currentTime = new Date(startTime.getTime() + interval * index)
    const trendOffset = index * 1.12
    const waveOffset = Math.sin(index / 1.6) * 1.85
    const power = basePower + trendOffset + waveOffset

    return {
      recordTime: formatDateTime(currentTime),
      power: power.toFixed(2),
      powerHigher: (power * 0.18).toFixed(2),
      powerHigh: (power * 0.24).toFixed(2),
      powerLow: (power * 0.31).toFixed(2),
      powerLower: (power * 0.19).toFixed(2),
      powerDeepLow: (power * 0.08).toFixed(2)
    }
  })
}

export const buildMockElectricMeterPowerConsumeTrend = (
  id: number,
  query: ElectricMeterPowerTrendQuery
): ElectricMeterPowerConsumeTrendPoint[] => {
  const startTime = parseDateTime(query.beginTime)
  const endTime = parseDateTime(query.endTime)
  if (!startTime || !endTime || startTime.getTime() >= endTime.getTime()) {
    return []
  }

  const pointCount = 12
  const totalDuration = endTime.getTime() - startTime.getTime()
  const interval = totalDuration / pointCount
  const baseConsumePower = 1.6 + id * 0.08

  return Array.from({ length: pointCount }, (_unused, index) => {
    const beginTime = new Date(startTime.getTime() + interval * index)
    const endTime = new Date(beginTime.getTime() + interval * 0.82)
    const consumePower =
      baseConsumePower + Math.sin(index / 1.7) * 0.35 + (index % 3 === 0 ? 0.28 : -0.12)

    return {
      beginRecordTime: formatDateTime(beginTime),
      endRecordTime: formatDateTime(endTime),
      meterConsumeTime: formatDateTime(endTime),
      consumePower: Math.max(consumePower, 0.2).toFixed(2),
      consumePowerHigher: Math.max(consumePower * 0.16, 0.01).toFixed(2),
      consumePowerHigh: Math.max(consumePower * 0.24, 0.01).toFixed(2),
      consumePowerLow: Math.max(consumePower * 0.32, 0.01).toFixed(2),
      consumePowerLower: Math.max(consumePower * 0.18, 0.01).toFixed(2),
      consumePowerDeepLow: Math.max(consumePower * 0.1, 0.01).toFixed(2)
    }
  })
}
