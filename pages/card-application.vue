    <template>
    <div class="scroll-wrapper">
        <div class="library-card">
            <div class="title-row">
                <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                <h1>借閱證申請</h1>
            </div>
            <h1 class="section-title"> {{
                step === 1 ? '申辦說明' :
                    step === 2 ? '個人資料填寫' :
                        step === 3 ? '申請成功' :
                            '重複申請警告'
            }}</h1>

            <!-- 步驟一：申請說明 + 同意聲明 -->
            <div v-if="step === 1" class="instructions">
                <ol>
                    <li>申辦對象：未曾申辦過本館個人借閱證之中華民國國民及持有居留證之非本國籍讀者</li>
                    <li>使用範圍：借閱證可借用本館內館藏資源(圖書、視聽資料、館內設備等)，並能使用本館所有線上資源。</li>
                    <li>領取：網路申請辦證後，請於本館開放時間內攜帶身分證明文件至櫃台領取。
                        <ul>
                            <li>中華民國國民應持個人身分證、駕照或戶口名簿(未滿十二歲之兒童由家長或法定監護人檢具身分證正本及戶口名簿正本代為辦理)。</li>
                            <li>非本國籍人士持護照或居留證。</li>
                        </ul>
                    </li>
                    <li>借閱說明：借閱證每人限辦乙張，個人借閱證可借閱圖書30冊、期刊5冊、視聽資料5件，圖書、期刊借期30天，可續借兩次。漫畫及視聽資料借期14天，不得續借。</li>
                    <li>遺失補發：
                        <ul>
                            <li>掛失登記：借閱證如遺失時，應立即向本館辦理掛失登記，若因未掛失或轉借而發生冒用情事，應自行負責相關賠償之責。</li>
                            <li>遺失補發: 檢具相關身分證明文件，須繳交行政規費新臺幣參拾元整(繳付工本費30元整)，申請補發</li>
                            <li>讀者資料如有變更，應即向本館辦理異動手續。</li>
                        </ul>
                    </li>
                </ol>

                <label class="consent">
                    <input type="checkbox" v-model="agreed" />
                    我已閱讀並同意以上聲明
                </label>

                <button :disabled="!agreed" @click="step = 2" class="start-button">
                    開始申請網路辦證
                </button>
            </div>

            <!-- ✅ 步驟二：申請表單 -->
            <form v-if="step === 2" @submit.prevent="submitForm" class="form">
                <div class="form-group">
                    <label class="form-label">姓名：</label>
                    <input v-model="form.name" required />
                </div>

                <div class="form-group">
                    <label class="form-label">性別：</label>
                    <div class="gender-radio">
                        <label><input type="radio" value="男" v-model="form.gender" required />男 Male</label>
                        <label><input type="radio" value="女" v-model="form.gender" />女 Female</label>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">身分證字號：</label>
                    <input v-model="form.idNumber" required />
                </div>

                <div class="form-group">
                    <label class="form-label">出生日期：</label>
                    <input v-model="form.birthDate" type="date" required />
                </div>

                <div class="form-group">
                    <label class="form-label">國別：</label>
                    <select v-model="form.nationality" required>
                        <option disabled value="">請選擇國別</option>
                        <option v-for="country in countries" :key="country" :value="country">{{ country }}</option>
                    </select>
                </div>

                <div class="form-group education-row">
                    <label class="form-label">學歷：</label>
                    <div class="education-options">
                        <label v-for="edu in educationOptions" :key="edu.value">
                            <input type="radio" :value="edu.value" v-model="form.education" name="education" required />
                            {{ edu.label }}
                        </label>
                    </div>
                </div>


                <div class="form-group">
                    <label class="form-label">職業：</label>
                    <select v-model="form.occupation" required>
                        <option disabled value="">請選擇職業</option>
                        <option v-for="job in occupations" :key="job" :value="job">{{ job }}</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">通訊地址：</label>
                    <div class="address-row">

                        <select v-model="form.addressCounty" required>
                            <option disabled value="">請選擇縣市</option>
                            <option v-for="county in counties" :key="county">{{ county }}</option>
                        </select>

                        <select v-model="form.addressTown" required>
                            <option disabled value="">請選擇鄉鎮</option>
                            <option v-for="town in towns" :key="town">{{ town }}</option>
                        </select>
                        <input type="text" v-model="form.addressZip" placeholder="郵遞區號 Zip" required />
                    </div>
                    <div class="address-detail">
                        <input type="text" v-model="form.addressDetail" placeholder="地址 Address" required />
                    </div>
                </div>


                <div class="form-group">
                    <label class="form-label">電子郵件：</label>
                    <input v-model="form.email" type="email" required />
                </div>

                <div class="form-group">
                    <label class="form-label">聯絡電話：</label>
                    <input v-model="form.phone" type="tel" required />
                </div>

                <div class="form-group">
                    <button type="button" @click="step = 1" class="back-button">← 回上一頁</button>
                    <button type="submit">送出申請</button>
                </div>
            </form>

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


const submitted = ref(false)

const submitForm = async () => {
    // 模擬已有申請過的身分證號
    if (form.idNumber === 'A123456789') {
        alreadyApplied.value = true
        step.value = 4 // 顯示「已申請過」畫面
        return
    }

    // ✅ 送出表單流程
    console.log('送出資料：', form)
    submitted.value = true
    step.value = 3
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
