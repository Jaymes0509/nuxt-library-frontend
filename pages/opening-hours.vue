<template>
    <div class="scroll-wrapper">
        <div class="hours">
            <div class="title-row">
                <img src="public\images\clock.png" alt="時鐘" />
                <h1>開放時間</h1>
            </div>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>

        </div>
    </div>
</template>

<script setup>

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
    /* max-height: 1000px; */
    margin: 0 auto;
    padding: 0 10px 100px;
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
