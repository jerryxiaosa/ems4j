import { readFileSync } from 'node:fs'

const systemListViews = [
  'UserManagementView.vue',
  'RoleManagementView.vue',
  'MenuManagementView.vue',
  'SpaceManagementView.vue',
  'OrganizationManagementView.vue'
]

const readComponentStyle = (fileName: string) => {
  const source = readFileSync(`${process.cwd()}/src/views/system/${fileName}`, 'utf8')
  return source.match(/<style scoped>([\s\S]*?)<\/style>/)?.[1] ?? ''
}

describe('SystemManagementListStyle', () => {
  test.each(systemListViews)(
    'testActionButtonStyle_WhenRenderedInSystemList_ShouldAlignWithAccountInfoList %s',
    (fileName) => {
      const style = readComponentStyle(fileName)

      expect(style).toMatch(
        /\.btn-link,\s*\.btn-link-danger\s*\{[\s\S]*?height:\s*auto;[\s\S]*?font-weight:\s*500;[\s\S]*?line-height:\s*1\.2;/
      )
      expect(style).toMatch(
        /\.btn-link:hover\s*\{[\s\S]*?color:\s*var\(--es-color-primary-hover\);/
      )
      expect(style).toMatch(/\.btn-link-danger\s*\{[\s\S]*?color:\s*var\(--es-color-danger\);/)
      expect(style).toMatch(/\.btn-link-danger:hover\s*\{[\s\S]*?opacity:\s*0\.85;/)
    }
  )
})
