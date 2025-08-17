import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RegistrationData } from './types'
import { checkUsername as apiCheckUsername, signUp as apiSignUp, signOut as apiSignOut } from './api'

export const useAuthStore = defineStore('useAuthStore', () => {
    const user = ref<any>(null)
    const accessToken = ref<string | null>(null)

    async function checkUsernameAvailability(username: string) {
        const r = await apiCheckUsername(username)
        if (!r.success) return { available: false, error: r.error || '확인 실패' }
        return { available: !!r.data?.available, error: r.data?.available ? null : '이미 사용 중인 아이디입니다.' }
    }

    async function signUp(payload: RegistrationData) {
        const r = await apiSignUp(payload)
        if (!r.success) return { success: false, error: r.error }
        return { success: true, requiresEmailVerification: !!r.data?.requiresEmailVerification }
    }

    async function signOut() {
        await apiSignOut()
        user.value = null
        accessToken.value = null
    }

    return {
        user,
        accessToken,
        checkUsernameAvailability,
        signUp,
        signOut
    }

    console.log('AuthStore loaded:', useAuthStore)
})
