const fs = require('node:fs')
const path = require('node:path')

const PROJECT_ROOT = path.resolve(__dirname, '..')
const TEMPLATE_PATH = path.join(PROJECT_ROOT, 'src', 'manifest.template.json')
const OUTPUT_PATH = path.join(PROJECT_ROOT, 'src', 'manifest.json')
const PLACEHOLDER = '__WECHAT_MINI_APPID__'

const stripQuotes = (value) => {
  if (value.length < 2) {
    return value
  }

  const first = value[0]
  const last = value[value.length - 1]
  if ((first === '"' && last === '"') || (first === "'" && last === "'")) {
    return value.slice(1, -1)
  }

  return value
}

const parseEnvironmentContent = (content) => {
  return content.split(/\r?\n/).reduce((environment, line) => {
    const trimmedLine = line.trim()
    if (!trimmedLine || trimmedLine.startsWith('#')) {
      return environment
    }

    const separatorIndex = trimmedLine.indexOf('=')
    if (separatorIndex === -1) {
      return environment
    }

    const key = trimmedLine.slice(0, separatorIndex).trim()
    const value = trimmedLine.slice(separatorIndex + 1).trim()
    if (key) {
      environment[key] = stripQuotes(value)
    }

    return environment
  }, {})
}

const loadEnvironmentFile = (filePath) => {
  if (!fs.existsSync(filePath)) {
    return {}
  }

  return parseEnvironmentContent(fs.readFileSync(filePath, 'utf8'))
}

const loadEnvironment = () => {
  return {
    ...loadEnvironmentFile(path.join(PROJECT_ROOT, '.env')),
    ...loadEnvironmentFile(path.join(PROJECT_ROOT, '.env.local')),
    ...process.env
  }
}

const generateManifestContent = (templateContent, environment) => {
  const appId = environment.WECHAT_MINI_APPID
  if (!appId) {
    throw new Error('缺少 WECHAT_MINI_APPID，请在 frontend-mini/.env.local 中配置，或通过环境变量传入。')
  }

  const manifestContent = templateContent.replaceAll(PLACEHOLDER, appId)
  JSON.parse(manifestContent)
  return manifestContent
}

const generateManifest = () => {
  const templateContent = fs.readFileSync(TEMPLATE_PATH, 'utf8')
  const manifestContent = generateManifestContent(templateContent, loadEnvironment())
  fs.writeFileSync(OUTPUT_PATH, manifestContent)
}

if (require.main === module) {
  generateManifest()
}

module.exports = {
  generateManifest,
  generateManifestContent,
  loadEnvironmentFile,
  parseEnvironmentContent
}
