<template>
    <div class="scroll-wrapper">
        <div class="hours">
            <div class="title-row">
                <img src="public\images\clock.png" alt="時鐘" />
                <h1>開放時間</h1>
            </div>
            <ul>
                <li v-for="item in hours" :key="item.day">
                    {{ item.day }}：{{ item.open }} - {{ item.close }}
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup>
const hours = [
    { day: '星期一', open: '休館', close: '' },
    { day: '星期二', open: '09:00', close: '21:00' },
    { day: '星期三', open: '09:00', close: '21:00' },
    { day: '星期四', open: '09:00', close: '21:00' },
    { day: '星期五', open: '09:00', close: '21:00' },
    { day: '星期六', open: '09:00', close: '21:00' },
    { day: '星期日', open: '09:00', close: '17:00' }
]
</script>

<style scoped>
.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.hours {
    flex: 1;
    max-width: 1000px;
    margin: 0 auto;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100%;
}

.hours li {
    font-family: "Hachi Maru Pop";
    font-weight: bold;
    margin-bottom: 1rem;
}

/* 滾動條預設為透明 */
.hours::-webkit-scrollbar {
    width: 8px;
}

.hours::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .hours::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .hours {
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

.description {
    font-size: 1.1rem;
    color: #333;
    /* padding: 12px 16px; */
    line-height: 2;

    /* display: block; */
}
</style>
