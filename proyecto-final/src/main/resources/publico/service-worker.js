const CACHE_NAME = 'suslinks-cache-v1';


const urlsToCache = [
  '/',
  '/crud-url',
  '/dashboardShortUser.html',
  '/dashboardShortLinks.html',
  '/dashboardUser.html',
  '/css/mainpage.css',
  '/offline.html',
  'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css',
  'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css',
  'https://cdn.tailwindcss.com',
  'https://cdn.jsdelivr.net/npm/chart.js',
  'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/js/all.min.js',
  'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js'
];

  self.addEventListener('install', event => {
  console.log('[Service Worker] Instalando...');
  console.log("SW: Hola, estoy vivo");

  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('[Service Worker] Cacheando recursos principales:', urlsToCache);

        const cachePromises = urlsToCache.map(url => {
          return cache.add(new Request(url, { cache: 'reload' })).catch(err => {
            console.warn(`[Service Worker] Falló al cachear: ${url}`, err);
          });
        });
        return Promise.all(cachePromises);
      })
      .then(() => {
        console.log('[Service Worker] Recursos principales cacheados.');
        return self.skipWaiting();
      })
      .catch(err => {
        console.error('[Service Worker] Falló la instalación del caché:', err);
      })
  );
});

// Activación: Limpia cachés antiguos
self.addEventListener('activate', event => {
  console.log('[Service Worker] Activado.');
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          // Elimina cualquier caché que empiece con 'suslinks-cache-' pero no sea la actual
          if (cacheName.startsWith('suslinks-cache-') && cacheName !== CACHE_NAME) {
            console.log('[Service Worker] Eliminando caché antiguo:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => {
      return self.clients.claim(); // Permite al SW controlar las pestañas abiertas inmediatamente
    })
  );
});


self.addEventListener('fetch', event => {

  if (event.request.method !== 'GET') {
    return;
  }


  if (event.request.url.includes('/api/')) {

     return;
  }


  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {

        if (cachedResponse) {

          return cachedResponse;
        }


        return fetch(event.request).then(networkResponse => {



           if (networkResponse && networkResponse.status === 200 /*&& !event.request.url.includes('/api/')*/) { // Asegúrate de no cachear APIs aquí si no quieres
               const responseToCache = networkResponse.clone();
               caches.open(CACHE_NAME).then(cache => {
                    //console.log('[Service Worker] Cacheando dinámicamente:', event.request.url);
                   cache.put(event.request, responseToCache);
               });
           }

          return networkResponse;
        }).catch(error => {
          // 3. Si la red falla (offline)
          console.warn('[Service Worker] Red falló. Sirviendo página offline.', error);
          // Intenta devolver la página offline.html como fallback
          return caches.match('/offline.html');
        });
      })
      .catch(error => {
           console.error('[Service Worker] Error en el fetch handler:', error);
           // Fallback final si incluso el match de caché falla
           return caches.match('/offline.html');
      })
  );
});