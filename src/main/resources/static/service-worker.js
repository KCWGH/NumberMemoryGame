// 캐시 이름과 캐시할 정적 파일 목록을 정의합니다.
const CACHE_NAME = 'number-memory-game-v1';
const OFFLINE_URL = 'index.html';

// 게임 구동에 필수적인 모든 정적 파일 목록
const ASSETS_TO_CACHE = [
    '/',
    'style/style.css',
    'js/script.js',
    'fonts/PretendardVariable.woff2', 
];

// --- 설치 (Install) 단계 ---
self.addEventListener('install', event => {
    console.log('[Service Worker] Installing...');
    event.waitUntil(
        caches.open(CACHE_NAME)
        .then(async cache => {
            console.log('[Service Worker] Caching app shell');
            try {
                return await cache.addAll(ASSETS_TO_CACHE);
            } catch (error) {
                console.error('[Service Worker] Cache addAll failed:', error);
            }
        })
    );
});

// --- 활성화 (Activate) 단계 ---
self.addEventListener('activate', event => {
    console.log('[Service Worker] Activating...');
    event.waitUntil(
        caches.keys().then(keyList => {
            return Promise.all(keyList.map(key => {
                if (key !== CACHE_NAME) {
                    console.log('[Service Worker] Deleting old cache:', key);
                    return caches.delete(key);
                }
            }));
        })
    );
    return self.clients.claim();
});

// --- 페치 (Fetch) 단계 ---
self.addEventListener('fetch', event => {
    const requestUrl = new URL(event.request.url);

    // 1. API 요청 및 OAuth2 로그인 요청은 네트워크를 사용합니다.
    if (requestUrl.pathname.startsWith('/api/') || requestUrl.pathname.includes('/oauth2/')) {
        // 네트워크 요청 후 응답이 없어도 캐시 응답을 반환할 필요가 없어 fetch(event.request)를 그대로 반환
        return; 
    }
    
    // 2. 나머지 정적 자산은 'Cache First, then Network' 전략을 사용하며, 실패 시 폴백 응답을 제공합니다.
    event.respondWith(
        caches.match(event.request)
        .then(response => {
            // 캐시에 응답이 있으면 캐시된 응답을 반환
            if (response) {
                return response;
            }
            
            // 캐시에 없으면 네트워크로 요청 시도
            return fetch(event.request).catch(() => {
                // ⚠️ 네트워크 요청 실패 시 (오프라인 상태 등) 폴백 제공
                console.log('[Service Worker] Fetch failed, returning offline fallback.');
                return caches.match(OFFLINE_URL);
            });
        })
    );
});