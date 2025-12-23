import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import StudyPlan from '../views/StudyPlan.vue'
import Shop from '../views/Shop.vue'
import Leaderboard from '../views/Leaderboard.vue'
import PointsCenter from '../views/PointsCenter.vue'
import StudentDashboard from '../views/student/StudentDashboard.vue'
import TeacherDashboard from '../views/teacher/TeacherDashboard.vue'
import TaskManagement from '../views/teacher/TaskManagement.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import ProfileLayout from '../views/profile/ProfileLayout.vue'
import UserInfo from '../views/profile/UserInfo.vue'
import Security from '../views/profile/Security.vue'
import Points from '../views/profile/Points.vue'
import Decorations from '../views/profile/Decorations.vue'
import Logs from '../views/profile/Logs.vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: (_to) => {
          // Redirect based on role if already logged in will be handled in guard or component
          return '/dashboard' 
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      component: ProfileLayout,
      meta: { requiresAuth: true },
      children: [
        {
            path: '',
            redirect: '/profile/info'
        },
        {
            path: 'info',
            name: 'profile-info',
            component: UserInfo
        },
        {
            path: 'security',
            name: 'profile-security',
            component: Security
        },
        {
            path: 'points',
            name: 'profile-points',
            component: Points
        },
        {
            path: 'decorations',
            name: 'profile-decorations',
            component: Decorations,
            meta: { role: 'USER' } // Only students
        },
        {
            path: 'logs',
            name: 'profile-logs',
            component: Logs
        }
      ]
    },
    {
      path: '/checkin',
      name: 'checkin',
      component: Dashboard,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/student-dashboard',
      name: 'student-dashboard',
      component: StudentDashboard,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/teacher-dashboard',
      name: 'teacher-dashboard',
      component: TeacherDashboard,
      meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
      path: '/teacher/tasks',
      name: 'teacher-tasks',
      component: TaskManagement,
      meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
      path: '/teacher/grading/:taskId',
      name: 'teacher-grading',
      component: () => import('../views/teacher/GradingWorkbench.vue'),
      meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
      path: '/admin-dashboard',
      name: 'admin-dashboard',
      component: AdminDashboard,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/content',
      name: 'ContentSecurity',
      component: () => import('../views/admin/ContentSecurity.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/shop',
      name: 'AdminShop',
      component: () => import('../views/admin/MallOperations.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/study-plan',
      name: 'study-plan',
      component: StudyPlan,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/shop',
      name: 'shop',
      component: Shop,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/leaderboard',
      name: 'leaderboard',
      component: Leaderboard,
      meta: { requiresAuth: true }
    },
    {
      path: '/points',
      name: 'points',
      component: PointsCenter,
      meta: { requiresAuth: true }
    },
    {
      path: '/friends',
      name: 'friends',
      component: () => import('../views/Friends.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  
  // 1. Check Auth
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }

  // 2. Init User if needed
  if (userStore.token && !userStore.user) {
    await userStore.initUser()
  }

  // 3. Role Based Access Control
  if (to.meta.role && userStore.user) {
      const userRole = userStore.user.role
      if (to.meta.role !== userRole && userRole !== 'ADMIN') { // Let Admin access everything? Or strict? Let's be strict for now or allow Admin to access all.
          // Actually user requirement says "ensure each role home URL cannot be mutually accessed"
          // So strict check
          if (to.meta.role !== userRole) {
              ElMessage.error('无权访问该页面')
              
              // Redirect to correct dashboard
              if (userRole === 'USER') next('/student-dashboard')
              else if (userRole === 'TEACHER') next('/teacher-dashboard')
              else if (userRole === 'ADMIN') next('/admin-dashboard')
              else next('/login')
              return
          }
      }
  }
  
  // 4. Root path redirection or Login page redirection if logged in
  if (to.path === '/login' && userStore.token) {
       const userRole = userStore.user?.role
       if (userRole === 'USER') next('/student-dashboard')
       else if (userRole === 'TEACHER') next('/teacher-dashboard')
       else if (userRole === 'ADMIN') next('/admin-dashboard')
       else next()
       return
  }
  
  // Handle /dashboard generic path
  if (to.path === '/dashboard' || to.path === '/') {
      if (userStore.user) {
           const userRole = userStore.user.role
           if (userRole === 'USER') next('/student-dashboard')
           else if (userRole === 'TEACHER') next('/teacher-dashboard')
           else if (userRole === 'ADMIN') next('/admin-dashboard')
           else next()
           return
      }
  }

  next()
})

export default router
