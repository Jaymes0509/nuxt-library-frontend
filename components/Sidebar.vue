<template>
    <aside class="sidebar" :class="{ closed: !isOpen }">

        <!-- <button class="toggle-btn" @click="$emit('update:isOpen', !isOpen)">
            {{ isOpen ? '« 收合' : '» 展開' }}
        </button> -->

        <!-- ✅ 背景圖（獨立控制透明） -->
        <!-- 圖片來源：https://www.aigei.com，僅供展示用 -->
        <img src="/images/book-flip.gif" alt="翻書動畫" class="bg-book" />

        <nav v-show="isOpen">
            <ul class="menu-list">
                <li v-for="item in menuItems" :key="item.label">
                    <div class="menu-item" :class="{ active: current === item.label }"
                        @click="() => setCurrent(item.label)">
                        <span>{{ item.label }}</span>
                        <span v-if="item.children">{{ open[item.label] ? '▾' : '▸' }}</span>
                    </div>

                    <ul v-if="item.children && open[item.label]" class="submenu">
                        <li v-for="child in item.children" :key="child.label">
                            <NuxtLink v-if="child.href" :to="child.href" :title="child.label" class="submenu-link">
                                {{ child.label }}
                            </NuxtLink>
                        </li>
                    </ul>
                </li>
            </ul>
        </nav>
    </aside>
</template>

<script setup>
import { reactive, ref } from 'vue'

defineProps({ isOpen: Boolean })
defineEmits(['update:isOpen'])

const open = reactive({})
const current = ref('')

const setCurrent = (label) => {
    current.value = label
    if (menuItems.find(item => item.label === label)?.children) {
        open[label] = !open[label]
    }
}

const menuItems = [
    {
        label: '認識本館',
        children: [
            { label: '本館簡介', href: '/about' },
            { label: '服務對象', href: '/audience' },
            { label: '空間設施', href: '#' },
            { label: '開放時間', href: '/opening-hours' },
            { label: '本館位置', href: '/location' }
        ]
    },
    {
        label: '活動消息',
        children: [
            { label: '最新活動', href: '#' },
            { label: '活動報名', href: '#' }
        ]
    },
    {
        label: '館藏查詢',
        children: [
            { label: '館藏查詢', href: '/catalogue-search' }
        ]
    },
    {
        label: '館藏預約',
        children: [
            { label: '我要預約', href: '/book-reservation' },
            { label: '預約查詢', href: '/reservation-record' }
        ]
    },
    {
        label: '書籍續借',
        children: [
            { label: '我要續借', href: '/borrow-continue' },
            { label: '歷史查詢', href: '#' }
        ]
    },
    {
        label: '申請服務',
        children: [
            { label: '借閱證申請', href: '/card-application' },
            { label: '自習座位預約', href: '#' },
            { label: '團體討論室預約', href: '#' },
            { label: '場地租借', href: '#' },
            { label: '團體參訪預約', href: '#' },
            { label: '書籍薦購', href: '#' },
        ]
    },
    {
        label: '排行榜 & 評論',
        children: [
            { label: '借閱排行榜', href: '/borrowing-rankings' },
            { label: '讀者書評', href: '/book-review' }
        ]
    }
]

// function toggle(label) {
//     if (menuItems.find(item => item.label === label)?.children) {
//         open[label] = !open[label]
//     }
// }
</script>

<style scoped>
.sidebar {
    width: 150px;
    /* max-height: 100vh; */
    /* ⭐ 限制 sidebar 最大高度為視窗高 */
    padding: 20px;
    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
    height: 100%;
    transition: width 0.3s ease, padding 0.3s ease;
    position: relative;
    /* overflow-y: auto; */
    /* ⭐ 加上垂直捲動條 */
    overflow-x: hidden;
}

.sidebar.closed {
    width: 0;
    /* 不清掉 padding-top or padding-bottom，避免垂直縮小 */
    padding-left: 0;
    padding-right: 0;
}

.sidebar nav {
    display: flex;
}

.menu-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.menu-item {
    background-color: lightgray;
    color: #333;
    border-radius: 800px;
    padding: 10px 16px;
    text-align: center;
    font-weight: bold;
    text-decoration: none;
    border: 1.5px dotted transparent;
    cursor: pointer;
    transition: background-color 0.3s ease, color 0.3s ease;
}

.menu-item:hover {
    background-color: darkgray;
}

.menu-item.active {
    background: linear-gradient(180deg, #1e3c72, #2a5298);
    color: white;
}

.submenu {
    padding-left: 40px;
    font-size: 0.9rem;
    margin-top: 4px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.submenu-link {
    padding: 6px 12px;
    border-radius: 8px;
    text-decoration: none;
    color: black;
    /* background-color: lightgray; */
    white-space: nowrap;
}

.submenu-link:hover {
    background-color: skyblue;
    color: #000;
}

.submenu-link:active {
    background-color: tomato;
    color: #000;
}

/* .toggle-btn {
    position: absolute;
    top: 0;
    right: -65px;
    z-index: 9999;
    background: linear-gradient(135deg, #ff9900, #ff3300);
    color: black;
    font-family: "Hachi Maru Pop";
    font-weight: bold;
    border: none;
    padding: 6px 12px;
    cursor: pointer;
    border-radius: 4px;
    white-space: nowrap;
} */

/* ✅ 背景圖透明控制 */
.bg-book {
    position: absolute;
    top: 100px;
    left: 0;
    width: 100%;
    height: 80%;
    object-fit: cover;
    /* ✅ 等同 background-size: cover */
    object-position: center;
    /* ✅ 等同 background-position: center */
    opacity: 0.1;
    /* ✅ 淡化圖片 */
    z-index: 0;
    pointer-events: none;
}
</style>
