import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Account {
    userId: number
    username: string
    avatar: string
    role: string
    token: string
    lastActiveTime: number
}

export const useAccountStore = defineStore('account', () => {
    // Persistent storage of all logged-in accounts (Vault)
    const accounts = ref<Account[]>([])
    
    // Initialize from localStorage
    const stored = localStorage.getItem('accounts_vault')
    if (stored) {
        try {
            accounts.value = JSON.parse(stored)
        } catch (e) {
            accounts.value = []
        }
    }

    function saveToStorage() {
        localStorage.setItem('accounts_vault', JSON.stringify(accounts.value))
    }

    function addAccount(user: any, token: string) {
        const existingIndex = accounts.value.findIndex(a => a.userId === user.id)
        const account: Account = {
            userId: user.id,
            username: user.username,
            avatar: user.avatar,
            role: user.role,
            token: token,
            lastActiveTime: Date.now()
        }
        
        if (existingIndex > -1) {
            accounts.value[existingIndex] = account
        } else {
            accounts.value.push(account)
        }
        saveToStorage()
    }

    function removeAccount(userId: number) {
        accounts.value = accounts.value.filter(a => a.userId !== userId)
        saveToStorage()
    }
    
    function getAccount(userId: number) {
        return accounts.value.find(a => a.userId === userId)
    }

    return { accounts, addAccount, removeAccount, getAccount }
})
