<template>
    <div class="scroll-wrapper">
        <div class="library-card">
            <div class="title-row">
                <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                <h1>自習座位預約</h1>
            </div>

            <h1 class="section-title">
                {{
                    step === 1 ? '選擇日期 & 預計就座時段' :
                        step === 2 ? '選擇座位' :
                            '預約結果'
                }}
            </h1>

            <div v-if="step === 1">
                <SeatDatePicker v-model="selectedDate" />
                <br>
                <SeatTimeSlot v-model="selectedSlot" :selected-date="selectedDate" />
                <ButtonsNextButton :disabled="!selectedDate || !selectedSlot?.start" @next="handleNextStep" />
            </div>

            <div v-if="step === 2">
                <ButtonsBackButton :step="step" @update:step="step = $event" />
                <SeatMap :selectedDate="selectedDate" :selectedSlot="selectedSlot" @confirm="handleConfirmSeat" />
            </div>

            <SeatReservationSummary v-if="step === 3" :selectedDate="selectedDate"
                :selectedSlot="`${selectedSlot.start} - ${selectedSlot.end}`" :selectedSeat="selectedSeat" />

            <div v-if="step === 3" class="cancel-button-wrapper" style="margin-top: 20px; text-align: center;">
                <button @click="cancelReservation" class="cancel-btn">❌ 取消預約</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useFetch } from '#app'

const selectedDate = ref('')
const selectedSeat = ref(null)
const selectedSlot = ref(null)
const step = ref(1)
const userId = ref(125) // 實際整合時請改為動態取得登入者 ID

const handleNextStep = async () => {
    const slotLabel = `${selectedSlot.value.start} - ${selectedSlot.value.end}`
    const { data, error } = await useFetch('http://localhost:8080/api/seats/reservations/check', {
        method: 'GET',
        query: { userId: userId.value, date: selectedDate.value, timeSlot: slotLabel }
    })

    if (error.value) return alert('❌ 檢查預約時發生錯誤')
    if (data.value === true) return alert('⚠️ 您已預約同一時段的座位')

    step.value = 2
}

const handleConfirmSeat = async (seatLabel) => {
    selectedSeat.value = seatLabel
    const res = await useFetch('http://localhost:8080/api/seats/reservations/book', {
        method: 'POST',
        body: {
            userId: userId.value,
            seatLabel,
            reservationDate: selectedDate.value,
            timeSlot: `${selectedSlot.value.start} - ${selectedSlot.value.end}`
        }
    })

    if (res.error.value) {
        const msg = res.error.value?.data || '❌ 發生錯誤';
        if (res.status.value === 409) {
            alert(msg.includes('同一時段') ? '⚠️ 您已預約同一時段的座位' : '⚠️ 該座位已被預約')
        } else {
            alert('❌ 發生錯誤，請稍後再試')
            console.error(res.error.value)
        }
    } else {
        step.value = 3
    }
}

const cancelReservation = async () => {
    const res = await useFetch('http://localhost:8080/api/seats/reservations/cancel', {
        method: 'PUT',
        query: {
            userId: userId.value,
            seatLabel: selectedSeat.value,
            date: selectedDate.value,
            timeSlot: selectedSlot.value.enum
        },
    });

    if (res.error.value) {
        alert('❌ 取消預約失敗：' + res.error.value.message);
    } else {
        alert('✅ 預約已取消');
        step.value = 1;
        selectedSeat.value = null;
        selectedSlot.value = '';
        selectedDate.value = '';
    }
};
</script>

<style scoped>
.cancel-btn {
    background-color: #e74c3c;
    color: white;
    padding: 10px 16px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 16px;
}

.cancel-btn:hover {
    background-color: #c0392b;
}

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
