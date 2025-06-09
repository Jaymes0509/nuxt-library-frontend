// composables/useNavigation.js
import { useRouter } from 'vue-router'

export function useNavigation() {
    const router = useRouter()
    const goHome = () => router.push('/')
    const goToForm = () => router.push('/card-application')

    return { goHome, goToForm }
}
