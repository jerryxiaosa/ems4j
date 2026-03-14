import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from '@/stores'
import menuPermissionDirective from '@/directives/menuPermission'
import './styles/base.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.directive('menu-permission', menuPermissionDirective)
app.mount('#app')
