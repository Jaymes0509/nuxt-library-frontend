<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const member = ref(null)
const loading = ref(true)
const error = ref(null)

// 可編輯欄位
const editFields = ref({
    addressCounty: '',
    addressTown: '',
    addressZip: '',
    addressDetail: '',
    phone: '',
    email: '',
    nationality: '',
    education: '',
    occupation: '',
    password: '',
})

onMounted(async () => {
    try {
        const res = await axios.get(`http://localhost:8080/api/manager/accounts/${route.params.id}`)
        member.value = res.data
        console.log('--------------------------------')
        console.log(res.data)
        console.log(res.data.addressCounty)
        console.log(res.data.addressTown)
        console.log(res.data.addressZip)
        console.log(res.data.addressDetail)
        console.log(res.data.phone)
        console.log(res.data.email)
        console.log(res.data.nationality)
        console.log(res.data.education)
        console.log(res.data.occupation)
        console.log(res.data.password)
        console.log('--------------------------------')
        editFields.value = {
            addressCounty: res.data.addressCounty,
            addressTown: res.data.addressTown,
            addressZip: res.data.addressZip,
            addressDetail: res.data.addressDetail,
            phone: res.data.phone,
            email: res.data.email,
            nationality: res.data.nationality,
            education: res.data.education,
            occupation: res.data.occupation,
            // password: res.data.password,
        }
    } catch (e) {
        error.value = '載入會員資料失敗'
    } finally {
        loading.value = false
    }
})

async function saveMemberDetail() {
    try {
        await axios.patch(`http://localhost:8080/api/manager/accounts/${route.params.id}`, {
            ...editFields.value
        }, {
            headers: { 'Content-Type': 'application/json' }
        })
        alert('更新成功')
    } catch (e) {
        console.log('e:', e)
        console.log('e.response:', e.response)
        console.log('e.response?.data:', e.response?.data)
        console.log('e.message:', e.message)
        alert('更新失敗')
    }
}
</script>

<template>
    <div class="p-8 flex flex-col items-center min-h-[60vh]">
        <div class="bg-white p-6 rounded shadow-lg min-w-[350px] mt-4 max-w-xl w-full">
            <h3 class="text-xl font-bold mb-4">會員詳細資料</h3>
            <div v-if="loading">載入中...</div>
            <div v-else-if="error">{{ error }}</div>
            <div v-else-if="member">
                <div class="mb-2"><span class="font-bold">會員編號：</span>{{ member.user_id }}</div>
                <div class="mb-2"><span class="font-bold">姓名：</span>{{ member.name }}</div>
                <div class="mb-2"><span class="font-bold">性別：</span>{{ member.gender }}</div>
                <div class="mb-2"><span class="font-bold">身分證：</span>{{ member.idNumber }}</div>
                <div class="mb-2"><span class="font-bold">生日：</span>{{ member.birthDate }}</div>
                <div class="mb-2"><span class="font-bold">國籍：</span>
                    <input v-model="editFields.nationality" class="border px-1 w-32" placeholder="國籍" />
                </div>
                <div class="mb-2"><span class="font-bold">教育程度：</span>
                    <input v-model="editFields.education" class="border px-1 w-32" placeholder="教育程度" />
                </div>
                <div class="mb-2"><span class="font-bold">職業：</span>
                    <input v-model="editFields.occupation" class="border px-1 w-32" placeholder="職業" />
                </div>
                <div class="mb-2"><span class="font-bold">居住地：</span>
                    <input v-model="editFields.addressCounty" class="border px-1 w-20" placeholder="縣市" />
                    <input v-model="editFields.addressTown" class="border px-1 w-20" placeholder="鄉鎮" />
                    <input v-model="editFields.addressZip" class="border px-1 w-16" placeholder="郵遞區號" />
                    <input v-model="editFields.addressDetail" class="border px-1 w-32" placeholder="詳細地址" />
                </div>
                <div class="mb-2"><span class="font-bold">信箱：</span>
                    <input v-model="editFields.email" class="border px-1 w-48" placeholder="信箱" />
                </div>
                <div class="mb-2"><span class="font-bold">電話：</span>
                    <input v-model="editFields.phone" class="border px-1 w-32" placeholder="電話" />
                </div>
                <div class="mb-2"><span class="font-bold">密碼：</span>
                    <input v-model="editFields.password" class="border px-1 w-32" placeholder="密碼" />
                </div>
                <div class="mb-2"><span class="font-bold">建立時間：</span>{{ member.createdAt.replace('T', ' ') }}</div>
                <div class="mb-2"><span class="font-bold">更新時間：</span>{{ member.updatedAt.replace('T', ' ') }}</div>
                <div class="flex justify-end gap-2 mt-4">
                    <button class="px-3 py-1 border rounded bg-blue-500 text-white"
                        @click="saveMemberDetail">儲存</button>
                    <button class="px-3 py-1 border rounded" @click="router.back()">返回</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>