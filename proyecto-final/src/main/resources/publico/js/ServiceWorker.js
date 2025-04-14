const CACHE_NAME = 'survey-cache-v1';
const urlsToCache = [
    '/',
    '/formulario',
    '/registro',
    '/crud-estudiante/crear',
    '/crud-estudiante/pendientes',
    '/js/worker.js',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js',
    '/js/formulario-estudiante.js',
    'https://cdnjs.cloudflare.com/ajax/libs/localforage/1.10.0/localforage.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css',
    'https://code.jquery.com/jquery-3.5.1.slim.min.js'
];

// Instalar el Service Worker y almacenar las páginas principales en el caché
self.addEventListener('install', (event) => {
    console.log('Service Worker: Instalando...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                console.log('Service Worker: Cacheando archivos...');
                return cache.addAll(urlsToCache);
            })
    );
});

// Activar el Service Worker y eliminar el caché viejo
self.addEventListener('activate', (event) => {
    console.log('Service Worker: Activado...');
    const cacheWhitelist = [CACHE_NAME];
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames.map((cacheName) => {
                    if (!cacheWhitelist.includes(cacheName)) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});

// Interceptar las solicitudes de red y devolverlas desde el caché si no hay conexión
self.addEventListener('fetch', (event) => {
    console.log('Service Worker: Interceptando petición para:', event.request.url);
    event.respondWith(
        caches.match(event.request)
            .then((cachedResponse) => {
                if (cachedResponse) {
                    // Si hay respuesta en caché, devolverla
                    return cachedResponse;
                }

                // Si no hay respuesta en caché, realizar la solicitud normalmente
                return fetch(event.request);
            })
    );
});