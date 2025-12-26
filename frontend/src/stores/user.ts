import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfo } from '../api/auth'
import { sendHeartbeat, getTodayOnlineSeconds } from '../api/stats'

export const useUserStore = defineStore('user', () => {
  const user = ref<any>(null)
  // Use sessionStorage for session isolation
  const token = ref(sessionStorage.getItem('active_token') || '')
  const totalOnlineSeconds = ref(0)
  const todayOnlineSeconds = ref(0)
  
  // Track accumulated seconds locally before syncing
  const pendingSeconds = ref(0)

  function setUser(userData: any) {
    user.value = userData
    if (userData.totalOnlineSeconds) {
        totalOnlineSeconds.value = userData.totalOnlineSeconds
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    sessionStorage.setItem('active_token', newToken)
    // Remove legacy local storage token to avoid confusion
    localStorage.removeItem('token')
  }

  function updateOnlineTime(duration: number) {
    totalOnlineSeconds.value += duration
    todayOnlineSeconds.value += duration
    pendingSeconds.value += duration
  }
  
  async function syncOnlineTime() {
    if (!user.value || pendingSeconds.value === 0) return
    
    try {
        const duration = pendingSeconds.value
        // Reset pending immediately to prevent double sync
        pendingSeconds.value = 0 
        await sendHeartbeat({
            userId: user.value.id,
            duration: duration
        })
    } catch (error) {
        console.error('Failed to sync online time:', error)
        // If failed, add back the seconds to retry later (optional, but good for reliability)
        // pendingSeconds.value += duration 
    }
  }
  
  async function fetchTodayOnlineTime() {
    if (!user.value) return
    try {
        const res: any = await getTodayOnlineSeconds(user.value.id)
        if (res.code === 200) {
            todayOnlineSeconds.value = res.data
        }
    } catch (error) {
        console.error('Failed to fetch today online time:', error)
    }
  }

  function setOnlineStats(total: number, today: number) {
    totalOnlineSeconds.value = total
    todayOnlineSeconds.value = today
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const res: any = await getUserInfo()
      user.value = res.data
      if (res.data.totalOnlineSeconds) {
          totalOnlineSeconds.value = res.data.totalOnlineSeconds
      }
      // Also fetch today's accurate time
      fetchTodayOnlineTime()
    } catch (error) {
      console.error('Failed to fetch user info:', error)
    }
  }

  async function initUser() {
    if (token.value && !user.value) {
      await fetchUser()
    }
  }

  function logout() {
    user.value = null
    token.value = ''
    sessionStorage.removeItem('active_token')
    localStorage.removeItem('token')
    totalOnlineSeconds.value = 0
    todayOnlineSeconds.value = 0
    pendingSeconds.value = 0
  }

  return { 
    user, 
    token, 
    totalOnlineSeconds, 
    todayOnlineSeconds, 
    setUser, 
    setToken, 
    logout, 
    initUser, 
    fetchUser, 
    updateOnlineTime, 
    setOnlineStats,
    syncOnlineTime,
    fetchTodayOnlineTime
  }
})
