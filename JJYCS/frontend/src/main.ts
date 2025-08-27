import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// Install Pinia store
app.use(createPinia())

// Install Vue Router
app.use(router)

// Mount the app
app.mount('#app')
