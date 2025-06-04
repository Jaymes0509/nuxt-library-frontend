<template>
    <div class="scroll-wrapper">
        <div class="intro">
            <div class="title-row">
                <img src="/public/images/libraryLogo.png" alt="Logo" />
                <h1>本館簡介</h1>
            </div>
            <img src="public\images\libraryBuilding.png" alt="圖書館外觀" />
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>
            <p class="description">
                國立公共資訊圖書館創立於1955年，為全國指標性圖書館之一，致力於推廣閱讀、資訊素養教育與數位服務。
                館藏豐富多元，涵蓋圖書、期刊、報紙、視聽資料與數位資源，提供市民終身學習與知識探索的空間。
            </p>

            <!-- <img src="/images/library-building.jpg" alt="圖書館外觀" class="intro-image" /> -->
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

.intro {
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

.intro img {
  max-width: 800px;
  width: 100%;
  height: 70%;
  object-fit:cover;
  margin: 1rem 0;
  border-radius: 30px;
}


/* 滾動條預設為透明 */
.intro::-webkit-scrollbar {
    width: 8px;
}

.intro::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .intro::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .intro {
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
    margin-bottom: 2rem;
}
</style>
