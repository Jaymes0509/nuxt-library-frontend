<template>
    <div class="scroll-wrapper">
        <div class="location">
            <div class="title-row">
                <img src="public\images\car.png" alt="Logo" />
                <h1>本館位置</h1>
            </div>
            <div id="map"></div>
            <h3>地址 & 連絡電話</h3>
            <br>
            <span>地址：台中市南屯區公益路二段51號18樓</span>
            <br>
            <span>Tel：(04) 2326-5860</span>

        </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import L from 'leaflet'

// 館位置經緯度
const latitude = 24.150768
const longitude = 120.651024

onMounted(() => {
    const map = L.map('map').setView([latitude, longitude], 16)

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map)

    L.marker([latitude, longitude])
        .addTo(map)
        .bindPopup('本館位置')
        .openPopup()
})
</script>

<style scoped>
.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.location {
    flex: 1;
    max-width: 1000px;
    width: 100%;
    height: 100vh;
    margin: 0 auto;
    padding: 0 10px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    scrollbar-width: thin;
    scrollbar-color: transparent transparent;
}

.location img {
    max-width: 100%;
    height: auto;
    object-fit: cover;
    margin: 1rem 0;
    border-radius: 30px;
}

/* 滾動條樣式 */
.location::-webkit-scrollbar {
    width: 8px;
}

.location::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

.scroll-wrapper:hover .location::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

.scroll-wrapper:hover .location {
    scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
}

.title-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    /* ✅ 保證小螢幕不爆版 */
}

.title-row img {
    width: 50px;
    height: auto;
}

.title-row h1 {
    margin: 0;
    font-size: 2rem;
}

::v-deep(.leaflet-popup-content-wrapper) {
    background-color: #ffefd5;
    /* 淡橙色（PapayaWhip） */
    border-radius: 12px;
    padding: 8px 12px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

::v-deep(.leaflet-popup-content) {
    color: orangered;
    /* 橘紅字體 */
    font-weight: bold;
    font-size: 1.2rem;
    text-align: center;
    margin: 0;
}



/* ✅ 響應式地圖容器 */
#map {
    width: 100%;
    max-width: 1000px;
    /* ✅ 最大寬度 */
    height: 700px;
    /* ✅ 增加高度 */
    margin-top: 20px;
    border: 1px solid #ccc;
    border-radius: 12px;
}

/* ✅ 響應式字體與地圖高度：手機專用 */
@media (min-width: 1024px) {
    #map {
        height: 700px;
        max-width: 1000px;
    }
}

@media (max-width: 600px) {
    .title-row h1 {
        font-size: 1.5rem;
    }

    #map {
        height: 350px;
    }
}
</style>
