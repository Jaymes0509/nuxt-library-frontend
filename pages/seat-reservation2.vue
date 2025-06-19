    <template>
        <div class="scroll-wrapper">
            <div class="library-card">
                <div class="title-row">
                    <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                    <h1>自習座位預約</h1>
                </div>
                <h1 class="section-title"> {{
                    step === 1 ? '申辦說明' :
                        step === 2 ? '個人資料填寫' :
                            step === 3 ? '申請成功' :
                                '重複申請警告'
                }}</h1>

                <!-- 步驟一：申請說明 + 同意聲明 -->
                <SeatDatePicker v-model="selectedDate" @nextStep="step = 2" />
                <SeatTimeSlot v-model="selectedSlot" />

                <SeatMap />

                <!-- <SeatMap :seatList="['A1', 'A2', 'A3', 'A4', 'A5', 'A6']" :availableSeats="['A2', 'A3', 'A5']"
                    @seat-selected="selectedSeat = $event" /> -->


                <!-- 步驟三：成功畫面 -->
                <div v-if="step === 3" class="success-step">
                    <h2>✅ 申請成功！</h2>
                    <p>我們已收到您的申請，將盡快與您聯絡!</p>
                    <div v-if="loading" class="loading-spinner"></div>
                    <p v-if="loading">即將返回首頁...</p>

                    <button v-if="!loading" @click="delayedGoHome">回首頁</button>
                </div>

                <div v-if="step === 4" class="already-applied-step">
                    <h2>⚠️ 您已申請過借閱證</h2>
                    <p>系統判定您已辦理借閱證，請勿重複申請。如有疑問請洽客服。</p>
                    <button @click="delayedGoHome">回首頁</button>
                </div>

            </div>

        </div>
    </template>

<script setup>
const selectedDate = ref('')

const selectedSeat = ref(null)
const selectedSlot = ref('')


import { ref, reactive, watch } from 'vue'
import { useNavigation } from '@/composables/useNavigation'

const { goHome } = useNavigation()

const loading = ref(false)

const delayedGoHome = () => {
    loading.value = true
    setTimeout(() => {
        goHome()
    }, 3000)
}

const step = ref(1)
const agreed = ref(false)
const alreadyApplied = ref(false)

const form = reactive({
    name: '',
    gender: '',
    idNumber: '',
    birthDate: '',
    nationality: '',
    education: '',
    occupation: '',
    addressCounty: '',
    addressTown: '',
    addressZip: '',
    addressDetail: '',
    email: '',
    phone: '',
    password: '',
})

const countries = [
    "中華民國 Republic of China",
    "美國 United States of America",
    "加拿大 Canada",
    "英國 United Kingdom",
    "法國 France",
    "德國 Germany",
    "日本 Japan",
    "南韓 South Korea",
    "香港 Hong Kong",
    "印度 Republic of India",
    "新加坡 Singapore",
    "馬來西亞 Malaysia",
    "越南 Vietnam",
    "泰國 Thailand",
    "菲律賓 The Philippines",
    "印尼 Republic of Indonesia",
    "非洲各國 African countries",
    "中美洲各國 Central American countries",
    "南美洲各國 South American countries",
    "俄羅斯 Russia",
    "中國 China",
    "歐洲各國 European countries",
    "中東各國 Middle Eastern countries",
    "其他 Other"
]

const educationOptions = [
    { value: '學齡前', label: '學齡前 Preschool' },
    { value: '國小', label: '國小 Elementary' },
    { value: '國中 (初中)', label: '國中 (初中) Junior High School' },
    { value: '高中 (高職)', label: '高中 (高職) Senior High School' },
    { value: '專科', label: '專科 Junior College' },
    { value: '大學', label: '大學 University/College' },
    { value: '碩士', label: '碩士 Master' },
    { value: '博士', label: '博士 Doctor' },
    { value: '其他', label: '其他 Other' }
]

const occupations = [
    '本國學生;僑生 Domestic Student / Overseas Chinese student',
    '外籍學生 Foreign student',
    '中小學教師 Primary or secondary school teacher',
    '大專教師',
    '公務員 Civil servant',
    '軍警人員 Military Personnel / Police',
    '自由業 Freelancer',
    '新聞業 Journalist',
    '文化業 Cultural worker',
    '農林漁牧業 Agricultural, forestry, fishing or animal husbandry worker',
    '工業 Industrial worker',
    '商 Business worker',
    '醫藥業 Medical worker',
    '社會團體 Social group member',
    '社會工作者 Social Worker',
    '宗教事業 Religious Worker',
    '交通事業 Transport workers',
    '家庭管理 Domestic worker',
    '退休人員 Retired',
    '職業(無) Not Employed'
]

const counties = ref([])

onMounted(async () => {
    try {
        const data = await $fetch('/api/zipcodes/counties')
        counties.value = data || []
        console.log('縣市資料：', counties.value)
    } catch (error) {
        console.error('載入縣市失敗：', error)
    }
})

const towns = ref([])

watch(() => form.addressCounty, async (newCounty) => {
    if (!newCounty) return

    try {
        const data = await $fetch('/api/zipcodes/towns', {
            query: { county: newCounty }
        })
        towns.value = data || []
        form.addressTown = ''
        form.addressZip = ''
    } catch (error) {
        console.error('載入鄉鎮失敗：', error)
    }
})

watch(() => form.addressTown, async (newTown) => {
    if (!form.addressCounty || !newTown) return

    try {
        const data = await $fetch('/api/zipcodes/zip', {
            query: {
                county: form.addressCounty,
                town: newTown
            }
        })
        form.addressZip = data || ''
    } catch (error) {
        console.error('取得郵遞區號失敗：', error)
    }
})

const showPassword = ref(false)

const submitted = ref(false)

const submitForm = async () => {
    try {
        // 呼叫後端 API
        await $fetch('http://localhost:8080/api/members/register', {
            method: 'POST',
            body: { ...form },
            credentials: 'include' // 若後端有跨域需要 session 可加
        })

        submitted.value = true
        step.value = 3 // 顯示申請成功畫面
    } catch (error) {
        // 偵測是否為重複身分證或 email 錯誤
        const message = error?.data || error?.message || '申請失敗'
        alert(`❌ 錯誤：${message}`)

        // 顯示已申請過畫面（可依照後端訊息判斷）
        if (message.includes('身分證') || message.includes('已被註冊')) {
            step.value = 4
        }
    }
}

</script>

<style scoped>
.loading-spinner {
    border: 6px solid #f3f3f3;
    border-top: 6px solid #003366;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 1rem auto;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.library-card {
    flex: 1;
    max-width: 1000px;
    /* max-height: 1000px; */
    margin: 0 auto;
    padding: 0 10px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    /* width: 100%; */
    height: 100%;
    scrollbar-width: thin;
    /* for Firefox */
    scrollbar-color: transparent transparent;
}


/* 滾動條預設為透明 */
.library-card::-webkit-scrollbar {
    width: 8px;
}

.library-card::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .library-card::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .library-card {
    scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
}




.title-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    /* 圖片與文字間距 */
}

.title-row img {
    width: 50px;
    height: auto;
}

.title-row h1 {
    margin: 0;
    font-size: 2rem;
}

.section-title {
    position: relative;
    padding-left: 1rem;
    font-size: 1.5rem;
    font-weight: bold;
    padding: 0 1.5rem;
}

.section-title::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    background-color: skyblue;
    /* 橘色 */
    border-radius: 2px;
}

.section-title::after {
    content: "";
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    background-color: skyblue;
    border-radius: 2px;
}

.instructions li {
    margin-bottom: 0.5rem;
    /* 行與行之間的間距 */
    line-height: 2;
    /* 文字行高 */
    /* text-align: center; */
}

.instructions ul {
    padding-left: 1.5rem;
}

a {
    color: #007bff;
    text-decoration: underline;
}

a:hover {
    text-decoration: none;
}

.consent {
    display: block;
    margin: 1rem auto;
    font-weight: bold;
    text-align: center;
    width: fit-content;
}

.start-button {
    display: block;
    margin: 0 auto 2rem;
    background-color: orange;
    color: black;
    padding: 12px 16px;
    border: 1px dashed #333;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
}

.start-button:disabled {
    background-color: #ccc;
    color: #666;
    cursor: not-allowed;
}

.form {
    display: block;
    flex-direction: column;
    /* gap: 50px; */
    /* background-color: #0056b3; */
}


.form-group {
    display: flex;
    align-items: center;
    margin-bottom: 1.5rem;
    /* 控制每列之間的間距 */
    flex-wrap: wrap;
    /* 小螢幕時可換行 */
}

.form-group label {
    min-width: 120px;
    /* 統一 label 寬度，可依需求調整 */
    font-weight: bold;
    margin-right: 12px;
    text-align: right;
}

.form-group input,
.form-group select {
    flex: 1;
    padding: 8px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
    min-width: 200px;
}

.education-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 1.5rem;
}

.form-label {
    width: 80px;
    font-weight: bold;
    margin-top: 0.3rem;
}

.gender-radio {
    display: flex;
    /* gap: 2rem; */
    min-width: 100px;
    font-weight: bold
}

.education-options {
    display: grid;
    grid-template-columns: repeat(3, minmax(200px, 1fr));
    /* 三欄排版 */
    gap: 1rem 2rem;
}

.education-options label {
    display: flex;
    align-items: center;
    text-align: center;
    gap: 6px;
}

.address-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8PX;
    margin-bottom: 0.5rem;
}

.address-row select,
.address-row input {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1rem;
    min-width: 140px;
}

.address-detail {
    display: flex;
    /* width: fit-content; */
    width: 100%;
    /* margin-left: 0; */
    padding-left: 130px;
    /* 避免被頂住，408 = 200 + 200 + 8 */
}

.address-detail input {
    width: 100%;
    max-width: calc(3 * 200px + 16px);
    /* 假設前面三個欄位每個200px，中間gap為8px*2 */
    padding: 8px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
}

.password-wrapper {
    display: flex;
    align-items: center;
}

.password-wrapper input {
    flex: 1;
}

.password-wrapper button {
    margin-left: 0.5rem;
    background: none;
    font-size: xx-large;
    border: none;
    cursor: pointer;
}


label {
    display: block;
    font-weight: bold;
    margin-bottom: 1rem;
    /* 控制垂直間距 */
}

input {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1rem;
}

button[type='submit'] {
    background-color: #007bff;
    color: white;
    padding: 10px;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    cursor: pointer;
}

button[type='submit']:hover {
    background-color: #0056b3;
}

.back-button {
    margin: 1rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
}

.success-step {
    text-align: center;
    padding: 40px 20px;
}

.success-step h2 {
    color: green;
    font-size: 2rem;
    margin-bottom: 1rem;
}

.success-step p {
    font-size: 1.2rem;
    margin-bottom: 2rem;
}

.success-step button {
    padding: 10px 20px;
    background-color: #2563eb;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1rem;
}


.success {
    color: green;
    font-weight: bold;
}

.already-applied-step {
    background-color: #fff8e1;
    border: 1px solid #ffcc80;
    padding: 2rem;
    border-radius: 1rem;
    text-align: center;
    margin-top: 2rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.already-applied-step h2 {
    color: #e65100;
    font-size: 1.8rem;
    margin-bottom: 1rem;
}

.already-applied-step p {
    font-size: 1rem;
    color: #4e342e;
    margin-bottom: 1.5rem;
}

.already-applied-step button {
    background-color: #ff9800;
    color: white;
    border: none;
    padding: 0.6rem 1.2rem;
    font-size: 1rem;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.already-applied-step button:hover {
    background-color: #fb8c00;
}
</style>
