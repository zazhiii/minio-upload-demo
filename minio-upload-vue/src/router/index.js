import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'upload',
    component: () => import( '../views/UploadView.vue') 
  }
]

const router = new VueRouter({
  routes
})

export default router
