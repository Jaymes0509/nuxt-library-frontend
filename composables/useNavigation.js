// composables/useNavigation.js
import { useRouter } from 'vue-router'

// composables/useNavigation.js
export function generateLink(href) {
  const pagesNeedReset = [
    '/application/card-application',
    '/application/seat-reservation',
    '/application/book-recommendation',
    '/feedback'
  ]

  if (typeof href === 'object' && href.path && pagesNeedReset.includes(href.path)) {
    return {
      ...href,
      query: {
        ...(href.query || {}),
        reset: 'true'
      }
    }
  }

  if (typeof href === 'string' && pagesNeedReset.includes(href)) {
    return {
      path: href,
      query: { reset: 'true' }
    }
  }

  return href
}



export function useNavigation() {
    const router = useRouter()
    const goHome = () => router.push('/')
    const goToForm = () => router.push('/card-application')

    return { goHome, goToForm }
}
