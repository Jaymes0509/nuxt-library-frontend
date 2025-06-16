<script setup lang="ts">
// import type { ComponentFieldBindingObject } from 'vee-validate'
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'

const props = defineProps<{
  class?: HTMLAttributes['class']
  disabled?: boolean
  // componentField?: ComponentFieldBindingObject<any>
  autocomplete?: string
  modelValue?: string
  placeholder?: string
}>()

const showModal = useModel(props, 'modelValue')

const showPassword = ref(false)
</script>

<template>
  <div class="relative">
    <input v-model="showModal" :type="showPassword ? 'text' : 'password'" :class="cn('pr-10', props?.class)"
      :placeholder="props?.placeholder ? props.placeholder : 'Enter your password'" :disabled="props?.disabled"
      :autocomplete="props?.autocomplete" />
    <button type="button" class="absolute right-0 top-0 h-full px-2 py-2 bg-transparent border-0 cursor-pointer"
      :disabled="props?.disabled" @click="showPassword = !showPassword" tabindex="-1">
      <Icon :icon="showPassword ? 'mdi:eye' : 'mdi:eye-off'" class="w-5 h-5" aria-hidden="true" />
      <span class="sr-only">
        {{ showPassword ? "隱藏密碼" : "顯示密碼" }}
      </span>
    </button>
  </div>
</template>
