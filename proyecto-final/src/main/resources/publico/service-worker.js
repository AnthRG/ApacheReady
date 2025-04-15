// You can bump this version (or add a version query param when loading the file)
// to force the browser to fetch a fresh copy.
/*const SW_VERSION = '1.0.3';

// Also change your cache version each time you want to bust the old cache.
const CACHE_VERSION = 'v3';
const CACHE_NAME = `suslinks-cache-${CACHE_VERSION}`;

const urlsToCache = [
    '/',
    '/crud-url',
    '/dashboardShortUser.html',
    '/dashboardShortLinks.html',
    '/offline.html',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css',
    'https://cdn.jsdelivr.net/npm/chart.js',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/js/all.min.js',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js'
];

// 1) INSTALL
self.addEventListener('install', event => {
    console.log(`[Service Worker v${SW_VERSION}] Installing…`);
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log(`[Service Worker v${SW_VERSION}] Caching main URLs…`);
                const cachePromises = urlsToCache.map(url => {
                    return cache.add(new Request(url, { cache: 'reload' }))
                        .catch(err => {
                            console.warn(`[SW v${SW_VERSION}] Failed to cache ${url}:`, err);
                        });
                });
                return Promise.all(cachePromises);
            })
            .then(() => {
                console.log(`[Service Worker v${SW_VERSION}] Finished caching. skipWaiting()…`);
                return self.skipWaiting(); // Forces new SW to activate soon.
            })
            .catch(err => {
                console.error(`[Service Worker v${SW_VERSION}] Install failed:`, err);
            })
    );
});

// 2) ACTIVATE
self.addEventListener('activate', event => {
    console.log(`[Service Worker v${SW_VERSION}] Activating…`);
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    // Delete old caches that match our naming pattern but aren’t this version
                    if (cacheName.startsWith('suslinks-cache-') && cacheName !== CACHE_NAME) {
                        console.log(`[SW v${SW_VERSION}] Deleting old cache: ${cacheName}`);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
            .then(() => {
                console.log(`[Service Worker v${SW_VERSION}] Clients claim…`);
                return self.clients.claim();
            })
    );
});

// 3) FETCH
self.addEventListener('fetch', event => {
    // Only handle GET
    if (event.request.method !== 'GET') {
        return;
    }

    // Example: bypass caching for your own API calls
    if (event.request.url.includes('/api/')) {
        return;
    }

    event.respondWith(
        caches.match(event.request)
            .then(cachedResponse => {
                // If found in cache, return it
                if (cachedResponse) {
                    return cachedResponse;
                }
                // Otherwise, fetch from network
                return fetch(event.request)
                    .then(networkResponse => {
                        // Only cache valid responses
                        if (networkResponse && networkResponse.status === 200) {
                            const responseToCache = networkResponse.clone();
                            caches.open(CACHE_NAME)
                                .then(cache => {
                                    cache.put(event.request, responseToCache);
                                    console.log(`[SW v${SW_VERSION}] Dynamically cached: ${event.request.url}`);
                                });
                        }
                        return networkResponse;
                    })
                    .catch(error => {
                        // If the network fails entirely
                        console.warn(`[SW v${SW_VERSION}] Network failed. Showing offline page…`);
                        return caches.match('/offline.html');
                    });
            })
            .catch(error => {
                // Final fallback
                console.error(`[SW v${SW_VERSION}] Error in fetch handler:`, error);
                return caches.match('/offline.html');
            })
    );
});
*/