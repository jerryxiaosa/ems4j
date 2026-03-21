import type { SpaceTreeItem } from '@/types/space'

export interface GatewayModelOption {
  id: number
  modelName: string
  communicateModel: string
  isNb: boolean
}

export interface GatewayItem {
  id: number
  gatewayName: string
  deviceNo: string
  modelId: number
  modelName: string
  spaceId: string
  spaceName: string
  spacePath: string
  inBuildingSort?: string
  communicateModel: string
  sn: string
  imei?: string
  configInfo: string
  onlineStatus: 0 | 1 | null
  onlineStatusName: string
  deviceAmount: number
}

export interface GatewayFormValue {
  id?: number
  gatewayName: string
  modelId: string
  modelName?: string
  deviceNo: string
  spaceId: string
  spaceName?: string
  spacePath?: string
  communicateModel: string
  sn: string
  imei: string
  deviceSecret: string
  configInfo: string
}

export interface GatewayDetailDeviceItem {
  id: number
  deviceName: string
  deviceType: string
  deviceNo: string
  portNo?: string
  meterAddress?: string
  isOnline?: boolean | null
}

export interface GatewayOption {
  value: string
  label: string
}

export const gatewayModelOptions: GatewayModelOption[] = [
  { id: 201, modelName: '边缘网关 EG-100', communicateModel: 'TCP', isNb: false },
  { id: 202, modelName: '采集网关 CG-NB20', communicateModel: 'NB', isNb: true },
  { id: 203, modelName: '采集网关 CG-TCP50', communicateModel: 'TCP', isNb: false }
]

export const gatewayOnlineStatusOptions: GatewayOption[] = [
  { value: 'true', label: '在线' },
  { value: 'false', label: '离线' }
]

export const gatewayCommunicateModelOptions: GatewayOption[] = [
  { value: 'tcp', label: 'TCP' },
  { value: 'nb', label: 'NB' }
]

export const gatewaySpaceTree: SpaceTreeItem[] = [
  {
    id: 101,
    pid: 0,
    name: 'A区',
    pathLabel: 'A区',
    children: [
      {
        id: 111,
        pid: 101,
        name: '1号楼',
        pathLabel: 'A区 / 1号楼',
        children: [
          {
            id: 1111,
            pid: 111,
            name: '弱电间',
            pathLabel: 'A区 / 1号楼 / 弱电间'
          },
          {
            id: 1112,
            pid: 111,
            name: '总控机房',
            pathLabel: 'A区 / 1号楼 / 总控机房'
          }
        ]
      }
    ]
  },
  {
    id: 102,
    pid: 0,
    name: 'B区',
    pathLabel: 'B区',
    children: [
      {
        id: 121,
        pid: 102,
        name: '3号楼',
        pathLabel: 'B区 / 3号楼',
        children: [
          {
            id: 1211,
            pid: 121,
            name: '设备层',
            pathLabel: 'B区 / 3号楼 / 设备层'
          }
        ]
      }
    ]
  }
]

export const gatewayRows: GatewayItem[] = [
  {
    id: 1,
    gatewayName: 'A区1号楼边缘网关',
    deviceNo: 'GW-A1-0001',
    modelId: 201,
    modelName: '边缘网关 EG-100',
    spaceId: '1112',
    spaceName: '总控机房',
    spacePath: 'A区 / 1号楼 / 总控机房',
    inBuildingSort: '1',
    communicateModel: 'TCP',
    sn: 'SN-A10001',
    configInfo: '{"collectInterval":60}',
    onlineStatus: 1,
    onlineStatusName: '在线',
    deviceAmount: 12
  },
  {
    id: 2,
    gatewayName: 'A区1号楼采集网关',
    deviceNo: 'GW-A1-0002',
    modelId: 202,
    modelName: '采集网关 CG-NB20',
    spaceId: '1111',
    spaceName: '弱电间',
    spacePath: 'A区 / 1号楼 / 弱电间',
    inBuildingSort: '2',
    communicateModel: 'NB',
    sn: 'SN-A10002',
    imei: '867530900001202',
    configInfo: '{"heartbeat":120}',
    onlineStatus: 0,
    onlineStatusName: '离线',
    deviceAmount: 7
  },
  {
    id: 3,
    gatewayName: 'B区3号楼边缘网关',
    deviceNo: 'GW-B3-0001',
    modelId: 203,
    modelName: '采集网关 CG-TCP50',
    spaceId: '1211',
    spaceName: '设备层',
    spacePath: 'B区 / 3号楼 / 设备层',
    inBuildingSort: '1',
    communicateModel: 'TCP',
    sn: 'SN-B30001',
    configInfo: '{"retry":3}',
    onlineStatus: 1,
    onlineStatusName: '在线',
    deviceAmount: 18
  }
]

export const gatewayDeviceMap: Record<number, GatewayDetailDeviceItem[]> = {
  1: [
    {
      id: 1,
      deviceName: 'A1-101总表',
      deviceType: '智能电表',
      deviceNo: 'EM-A1101-01',
      portNo: '1',
      meterAddress: '11',
      isOnline: true
    },
    {
      id: 2,
      deviceName: 'A1-102分表',
      deviceType: '智能电表',
      deviceNo: 'EM-A1102-02',
      portNo: '1',
      meterAddress: '12',
      isOnline: true
    },
    {
      id: 3,
      deviceName: 'A区冷机房总表',
      deviceType: '智能电表',
      deviceNo: 'EM-A-CJ-01',
      portNo: '2',
      meterAddress: '3',
      isOnline: false
    },
    {
      id: 4,
      deviceName: 'A区水表采集器',
      deviceType: '智能水表',
      deviceNo: 'WM-A-01',
      portNo: '3',
      meterAddress: '--',
      isOnline: null
    },
    {
      id: 5,
      deviceName: 'A区空调网关从站',
      deviceType: '智能网关',
      deviceNo: 'GW-A1-S01',
      portNo: '--',
      meterAddress: '--',
      isOnline: true
    },
    {
      id: 6,
      deviceName: 'A区照明回路表',
      deviceType: '智能电表',
      deviceNo: 'EM-A-LT-01',
      portNo: '4',
      meterAddress: '7',
      isOnline: true
    }
  ],
  2: [
    {
      id: 7,
      deviceName: 'A区宿舍一层总表',
      deviceType: '智能电表',
      deviceNo: 'EM-A-D1-01',
      portNo: '1',
      meterAddress: '21',
      isOnline: true
    },
    {
      id: 8,
      deviceName: 'A区宿舍二层总表',
      deviceType: '智能电表',
      deviceNo: 'EM-A-D2-01',
      portNo: '1',
      meterAddress: '22',
      isOnline: false
    }
  ],
  3: [
    {
      id: 9,
      deviceName: 'B区3号楼总表',
      deviceType: '智能电表',
      deviceNo: 'EM-B3-01',
      portNo: '1',
      meterAddress: '31',
      isOnline: true
    },
    {
      id: 10,
      deviceName: 'B区冷站电表',
      deviceType: '智能电表',
      deviceNo: 'EM-B-COLD-01',
      portNo: '2',
      meterAddress: '9',
      isOnline: true
    },
    {
      id: 11,
      deviceName: 'B区补水泵水表',
      deviceType: '智能水表',
      deviceNo: 'WM-B-01',
      portNo: '3',
      meterAddress: '--',
      isOnline: null
    }
  ]
}

export const createDefaultGatewayForm = (): GatewayFormValue => ({
  gatewayName: '',
  modelId: '',
  modelName: '',
  deviceNo: '',
  spaceId: '',
  spaceName: '',
  spacePath: '',
  communicateModel: '',
  sn: '',
  imei: '',
  deviceSecret: '',
  configInfo: ''
})

export const findGatewayModelOption = (
  modelId: string | number | undefined
): GatewayModelOption | undefined => {
  return gatewayModelOptions.find((item) => String(item.id) === String(modelId))
}
