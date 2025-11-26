const CACHE_VERSION = 'v1.0.0';
const CACHE_NAME = `number-memory-game-${CACHE_VERSION}`;
const OFFLINE_URL = '/index.html';

const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/style/style.css',
    '/js/script.js',
    '/manifest.json',
    '/icons/favicon.ico',
    '/icons/icon-192.png',
    '/icons/icon-512.png',
    '/icons/logo_google.svg',
    '/icons/logo_kakao.svg',
    '/icons/logo_naver.svg'
];

self.addEventListener('install', event => {
    console.log('[SW] Installing service worker version:', CACHE_VERSION);
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('[SW] Caching static assets');
                return cache.addAll(STATIC_ASSETS);
            })
            .catch(error => {
                console.error('[SW] Failed to cache assets:', error);
            })
    );
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    console.log('[SW] Activating service worker version:', CACHE_VERSION);
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== CACHE_NAME) {
                        console.log('[SW] Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    return self.clients.claim();
});

self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);

    if (url.origin !== location.origin) {
        return;
    }

    if (url.pathname.startsWith('/api/') || url.pathname.includes('/oauth2/') || url.pathname.includes('/login/')) {
        event.respondWith(
            fetch(request).catch(error => {
                console.error('[SW] Network request failed:', error);
                return new Response('Network error', {
                    status: 503,
                    statusText: 'Service Unavailable'
                });
            })
        );
        return;
    }

    event.respondWith(
        caches.match(request)
            .then(cachedResponse => {
                if (cachedResponse) {
                    return cachedResponse;
                }

                return fetch(request)
                    .then(response => {
                        if (!response || response.status !== 200 || response.type !== 'basic') {
                            return response;
                        }

                        const responseToCache = response.clone();
                        caches.open(CACHE_NAME).then(cache => {
                            cache.put(request, responseToCache);
                        });

                        return response;
                    })
                    .catch(() => {
                        console.log('[SW] Serving offline fallback');
                        return caches.match(OFFLINE_URL);
                    });
            })
    );
});