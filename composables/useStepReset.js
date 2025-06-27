// /composables/useStepReset.js
import { watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/**
 * 當偵測到 ?reset=true query 時，自動將 step 重設為 1 並呼叫 resetFn。
 * @param {import('vue').Ref<number>} stepRef - 綁定的步驟 ref
 * @param {Function} [resetFn] - 可選的重設表單方法
 */
export function useStepReset(stepRef, resetFn) {
  const route = useRoute()
  const router = useRouter()

  const resetStep = () => {
    stepRef.value = 1
    if (resetFn) {
      resetFn()
    }
    router.replace({ path: route.path }) // 移除 ?reset=true
  }

  onMounted(() => {
    if (route.query.reset === 'true') {
      resetStep()
    }
  })

  watch(() => route.query.reset, (val) => {
    if (val === 'true') {
      resetStep()
    }
  })
}
