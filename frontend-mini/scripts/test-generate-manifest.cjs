const assert = require('node:assert/strict')
const fs = require('node:fs')
const os = require('node:os')
const path = require('node:path')

const {
  generateManifestContent,
  loadEnvironmentFile,
  parseEnvironmentContent
} = require('./generate-manifest.cjs')

assert.deepEqual(parseEnvironmentContent('WECHAT_MINI_APPID=wx-test\n# comment\nEMPTY=\n'), {
  WECHAT_MINI_APPID: 'wx-test',
  EMPTY: ''
})

assert.equal(
  generateManifestContent('{"mp-weixin":{"appid":"__WECHAT_MINI_APPID__"}}', {
    WECHAT_MINI_APPID: 'wx-test'
  }),
  '{"mp-weixin":{"appid":"wx-test"}}'
)

assert.throws(
  () => generateManifestContent('{"mp-weixin":{"appid":"__WECHAT_MINI_APPID__"}}', {}),
  /WECHAT_MINI_APPID/
)

const temporaryDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'manifest-env-'))
const environmentPath = path.join(temporaryDirectory, '.env.local')
fs.writeFileSync(environmentPath, 'WECHAT_MINI_APPID="wx-from-file"\n')
assert.deepEqual(loadEnvironmentFile(environmentPath), {
  WECHAT_MINI_APPID: 'wx-from-file'
})

fs.rmSync(temporaryDirectory, { recursive: true, force: true })
