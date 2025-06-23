<template>
    <div class="sidebar-container">
        <aside class="sidebar" :class="{ closed: !isOpen }">
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
        <div class="toggle-area" @click="$emit('update:isOpen', !isOpen)">
            <div class="toggle-icon">
                {{ isOpen ? '«' : '»' }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { useAuth } from '~/composables/useAuth'
import { computed, reactive, ref } from 'vue'

const { user } = useAuth()

defineProps({ isOpen: Boolean })
defineEmits(['update:isOpen'])

const open = reactive({})
const current = ref('')

const setCurrent = (label) => {
    current.value = label
    if (menuItems.value.find(item => item.label === label)?.children) {
        open[label] = !open[label]
    }
}

// 用 computed 依 user 產生 menuItems
const menuItems = computed(() => {
    const items = [
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
                { label: '預約清單', href: '/reservation-record' },
                { label: '預約紀錄', href: '/reservation-history' }
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
                { label: '自習座位預約', href: 'seat-reservation' },
                { label: '場地租借', href: '#' },
                { label: '書籍薦購', href: '#' }
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
    // 只有 admin 才加管理者專區
    if (user && user.value && user.value.role === 'admin') {
        items.push({
            label: '管理者專區',
            children: [
                { label: '管理者專區', href: '/manager' },
                { label: '書籍管理', href: '/manager/books' },
                { label: '帳號管理', href: '/manager/accounts' },
                { label: '違規紀錄', href: '/manager/violations' }
            ]
        })
    }
    return items
})
</script>

<style scoped>
.sidebar-container {
    position: relative;
    height: 100%;
}

.sidebar {
    width: 10.5rem;
    padding: 20px;
    height: 100%;
    transition: width 0.3s ease, padding 0.3s ease;
    position: relative;
    overflow-x: hidden;
    background: #fff;
}

.sidebar span {
    white-space: nowrap;
}

.sidebar.closed {
    width: 0;
    padding-left: 0;
    padding-right: 0;
}

.toggle-area {
    position: absolute;
    top: 50%;
    left: 190px;
    transform: translateY(-100%);
    /* transform: translateX(0); */
    width: 20px;
    height: 64px;
    background: linear-gradient(180deg, #1e3c72, #2a5298);
    border: none;
    border-radius: 0 3px 3px 0;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    z-index: 99;
    transition: left 0.3s ease, opacity 0.2s;
}

.sidebar.closed+.toggle-area {
    left: 0;
}

.sidebar+.toggle-area {
    left: 12.8rem;
}

.toggle-icon {
    font-size: 30px;
    line-height: 1;
}

.toggle-area:hover {
    opacity: 0.85;
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

.bg-book {
    position: absolute;
    top: 100px;
    left: 0;
    width: 100%;
    height: 80%;
    object-fit: cover;
    object-position: center;
    opacity: 0.1;
    z-index: 0;
    pointer-events: none;
}
</style>
