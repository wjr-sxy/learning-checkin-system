import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { useUserStore } from './user'
import { ElMessage } from 'element-plus'

const BASE_URL = '/api/friend'

export const useFriendStore = defineStore('friend', () => {
    const friends = ref<any[]>([])
    const requests = ref<any[]>([])
    const loading = ref(false)
    const userStore = useUserStore()

    const loadFriends = async () => {
        if (!userStore.user) return
        try {
            loading.value = true
            const res = await axios.get(`${BASE_URL}/list`, {
                params: { userId: userStore.user.id }
            })
            friends.value = res.data.data
        } catch (error) {
            console.error('Load friends error', error)
        } finally {
            loading.value = false
        }
    }

    const loadRequests = async () => {
        if (!userStore.user) return
        try {
            const res = await axios.get(`${BASE_URL}/requests`, {
                params: { userId: userStore.user.id }
            })
            requests.value = res.data.data
        } catch (error) {
            console.error('Load requests error', error)
        }
    }

    const sendRequest = async (targetUserId: number) => {
        if (!userStore.user) return
        try {
            await axios.post(`${BASE_URL}/request`, {
                userId: userStore.user.id,
                targetUserId
            })
            ElMessage.success('发送好友请求成功')
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '发送请求失败')
        }
    }

    const handleRequest = async (requestId: number, status: number) => {
        if (!userStore.user) return
        try {
            await axios.post(`${BASE_URL}/handle`, {
                userId: userStore.user.id,
                requestId,
                status
            })
            ElMessage.success(status === 1 ? '已接受好友请求' : '已拒绝好友请求')
            loadRequests()
            if (status === 1) loadFriends()
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '操作失败')
        }
    }

    const deleteFriend = async (friendId: number) => {
        if (!userStore.user) return
        try {
            await axios.delete(`${BASE_URL}/delete`, {
                data: {
                    userId: userStore.user.id,
                    friendId
                }
            })
            ElMessage.success('已删除好友')
            loadFriends()
        } catch (error: any) {
            ElMessage.error(error.response?.data?.message || '删除失败')
        }
    }

    const searchUsers = async (keyword: string) => {
        if (!userStore.user) return []
        try {
            const res = await axios.get(`${BASE_URL}/search`, {
                params: {
                    keyword,
                    currentUserId: userStore.user.id
                }
            })
            return res.data.data
        } catch (error) {
            console.error('Search error', error)
            return []
        }
    }

    return {
        friends,
        requests,
        loading,
        loadFriends,
        loadRequests,
        sendRequest,
        handleRequest,
        deleteFriend,
        searchUsers
    }
})
