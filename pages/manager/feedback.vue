<template>
    <div class="p-6">
        <h2 class="text-xl font-bold mb-4">意見信箱留言管理</h2>

        <table class="w-full border border-gray-300 mb-6">
            <thead class="bg-gray-100">
                <tr>
                    <th class="border px-4 py-2">姓名</th>
                    <th class="border px-4 py-2">主旨</th>
                    <th class="border px-4 py-2">內容</th>
                    <th class="border px-4 py-2">狀態</th>
                    <th class="border px-4 py-2">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in feedbackList" :key="item.id">
                    <td class="border px-4 py-2">{{ item.name }}</td>
                    <td class="border px-4 py-2">{{ item.subject }}</td>
                    <td class="border px-4 py-2">{{ item.content }}</td>
                    <td class="border px-4 py-2">{{ item.status }}</td>
                    <td class="border px-4 py-2">
                        <button class="bg-blue-500 text-white px-3 py-1 rounded" @click="openReply(item)">回覆</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <!-- 回覆對話框 -->
        <div v-if="showReplyModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white p-6 rounded-lg w-96">
                <h3 class="text-lg font-semibold mb-2">回覆留言：{{ selectedItem?.subject }}</h3>
                <textarea v-model="replyContent" class="w-full border rounded p-2 mb-4" rows="5"
                    placeholder="請輸入回覆內容"></textarea>
                <div class="flex justify-end space-x-2">
                    <button class="bg-gray-400 text-white px-3 py-1 rounded" @click="closeReply">取消</button>
                    <button class="bg-green-600 text-white px-3 py-1 rounded" @click="submitReply">送出</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { replyFeedback, getAllFeedback } from '@/composables/useFeedbackApi'

const feedbackList = ref([])
const showReplyModal = ref(false)
const selectedItem = ref(null)
const replyContent = ref('')

const openReply = (item) => {
    selectedItem.value = item
    replyContent.value = item.reply || ''
    showReplyModal.value = true
}

const closeReply = () => {
    showReplyModal.value = false
    selectedItem.value = null
    replyContent.value = ''
}

const submitReply = async () => {
    const result = await replyFeedback({
        id: selectedItem.value.id,
        reply: replyContent.value,
        status: '已回覆'
    })

    if (result.success) {
        alert('✅ 回覆成功')
        showReplyModal.value = false
        await loadFeedback() // 重新載入留言
    } else {
        // 防止 [object Object]
        const errorText = typeof result.message === 'string'
            ? result.message
            : JSON.stringify(result.message)
        alert('❌ 回覆失敗：' + errorText)
    }
}

const loadFeedback = async () => {
    const result = await getAllFeedback()
    console.log('收到的留言資料:', result)
    feedbackList.value = result || []
}

onMounted(loadFeedback)
</script>
