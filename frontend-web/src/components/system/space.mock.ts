import type { SystemSpaceFormValue, SystemSpaceItem } from '@/modules/system/spaces/types'

export const systemSpaceMockTree: SystemSpaceItem[] = [
  {
    id: 1,
    parentId: null,
    name: 'Base园区',
    typeValue: '1',
    typeName: '园区',
    area: 0,
    sortNum: 1,
    children: [
      {
        id: 2,
        parentId: 1,
        name: '第二级园区1',
        typeValue: '1',
        typeName: '园区',
        area: 0,
        sortNum: 1,
        children: [
          {
            id: 3,
            parentId: 2,
            name: '第二级园区1',
            typeValue: '1',
            typeName: '园区',
            area: 0,
            sortNum: 1
          },
          {
            id: 4,
            parentId: 2,
            name: 'A幢',
            typeValue: '2',
            typeName: '园内区域',
            area: 0,
            sortNum: 2,
            children: [
              { id: 5, parentId: 4, name: 'A101', typeValue: '3', typeName: '房间', area: 150, sortNum: 1 },
              { id: 6, parentId: 4, name: 'A102', typeValue: '3', typeName: '房间', area: 555, sortNum: 2 },
              { id: 7, parentId: 4, name: 'A103', typeValue: '3', typeName: '房间', area: 0, sortNum: 3 }
            ]
          }
        ]
      },
      {
        id: 8,
        parentId: 1,
        name: '第二级园区2',
        typeValue: '1',
        typeName: '园区',
        area: 0,
        sortNum: 2,
        children: [
          { id: 9, parentId: 8, name: '12', typeValue: '2', typeName: '园内区域', area: 123, sortNum: 1 },
          { id: 10, parentId: 8, name: 'AS101', typeValue: '3', typeName: '房间', area: 80, sortNum: 2 }
        ]
      },
      {
        id: 11,
        parentId: 1,
        name: 'A园区',
        typeValue: '2',
        typeName: '园内区域',
        area: 200,
        sortNum: 3,
        children: [
          {
            id: 12,
            parentId: 11,
            name: '1幢',
            typeValue: '2',
            typeName: '园内区域',
            area: 0,
            sortNum: 1,
            children: [
              { id: 13, parentId: 12, name: '101', typeValue: '3', typeName: '房间', area: 50, sortNum: 1 },
              { id: 14, parentId: 12, name: '102', typeValue: '3', typeName: '房间', area: 80, sortNum: 2 }
            ]
          },
          {
            id: 15,
            parentId: 11,
            name: '2幢',
            typeValue: '2',
            typeName: '园内区域',
            area: 0,
            sortNum: 2,
            children: [
              { id: 16, parentId: 15, name: '201', typeValue: '3', typeName: '房间', area: 100, sortNum: 1 }
            ]
          }
        ]
      }
    ]
  }
]

export const createDefaultSystemSpaceForm = (): SystemSpaceFormValue => ({
  parentId: '',
  parentName: '',
  name: '',
  typeValue: '',
  typeName: '',
  area: '',
  sortNum: ''
})
