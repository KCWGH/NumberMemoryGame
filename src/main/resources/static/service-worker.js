// 캐시 이름과 캐시할 정적 파일 목록을 정의합니다.
const CACHE_NAME = 'number-memory-game-v1';

// 게임 구동에 필수적인 모든 정적 파일 목록
const ASSETS_TO_CACHE = [
    '/', // 시작 페이지 (index.html)
    'index.html',
    'style/style.css',
    'js/script.js',
    // 테마 토글에 사용되는 폰트 (Pretendard CDN)
    'https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css',
    // ⚠️ 경고: CDN 폰트 캐싱이 브라우저 추적 방지 기능에 의해 차단될 수 있습니다. 
    // 가능하다면 폰트 파일을 직접 다운로드하여 서버에 두고 캐시 목록에 추가하는 것이 가장 안전합니다.
];

// --- 설치 (Install) 단계 ---
// Service Worker가 설치될 때 정적 자산들을 캐시에 추가합니다.
self.addEventListener('install', event => {
    console.log('[Service Worker] Installing...');
    event.waitUntil(
        caches.open(CACHE_NAME)
        .then(cache => {
            console.log('[Service Worker] Caching app shell');
            return cache.addAll(ASSETS_TO_CACHE).catch(error => {
                // 캐싱 실패를 무시하지 않고 로깅합니다. (특히 CDN 파일 로딩 실패 시)
                console.error('[Service Worker] Cache addAll failed:', error);
            });
        })
    );
});

// --- 활성화 (Activate) 단계 ---
// 이전 버전의 캐시를 정리하고 새로운 캐시를 활성화합니다.
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
    // Service Worker가 제어하는 클라이언트에게 즉시 제어권을 넘깁니다.
    return self.clients.claim();
});

// --- 페치 (Fetch) 단계 ---
// 요청이 발생했을 때, 캐시에 있으면 캐시된 버전을 반환하고, 없으면 네트워크에서 가져옵니다.
self.addEventListener('fetch', event => {
    const requestUrl = new URL(event.request.url);

    // 1. 점수 API 요청 및 OAuth2 로그인 요청은 네트워크를 사용합니다.
    if (requestUrl.pathname.startsWith('/api/') || requestUrl.pathname.includes('/oauth2/')) {
        return fetch(event.request);
    }
    
    // 2. 나머지 정적 자산은 'Cache First, then Network' 전략을 사용합니다.
    // 즉, 캐시에서 먼저 찾고, 없으면 네트워크 요청을 시도합니다.
    event.respondWith(
        caches.match(event.request)
        .then(response => {
            // 캐시에 응답이 있으면 캐시된 응답을 반환
            if (response) {
                return response;
            }
            // 캐시에 없으면 네트워크로 요청을 시도
            return fetch(event.request).catch(error => {
                console.error('[Service Worker] Fetch failed:', error);
            });
        })
    );
});