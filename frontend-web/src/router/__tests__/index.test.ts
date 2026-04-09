import { describe, expect, test } from 'vitest'
import router from '@/router'

describe('router index', () => {
  test('testRoutes_WhenElectricBillPreviewRemoved_ShouldNotRegisterPublicPreviewRoute', () => {
    const routePathList = router.getRoutes().map((route) => route.path)

    expect(routePathList).not.toContain('/preview/electric-bill')
  })
})
