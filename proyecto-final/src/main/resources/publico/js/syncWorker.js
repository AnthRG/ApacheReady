//syncWorker

self.importScripts('https://cdnjs.cloudflare.com/ajax/libs/localforage/1.9.0/localforage.min.js');

function syncData() {
    const ws = new WebSocket('ws://localhost:7000/sync');

    ws.onopen = () => {
        // Sincronizar estudiantes pendientes
        localforage.getItem('surveyData').then(data => {
            if (data && data.length > 0) {
                const cleanedData = data.map(estudiante => ({
                    ...estudiante,
                    latitud: estudiante.latitud === "" ? 0.0 : parseFloat(estudiante.latitud),
                    longitud: estudiante.longitud === "" ? 0.0 : parseFloat(estudiante.longitud)
                }));
                ws.send(JSON.stringify({type: 'syncStudents', data: cleanedData}));
            }
        });
        // Sincronizar usuarios pendientes (opcional)
        localforage.getItem('pendingUsers').then(data => {
            if (data && data.length > 0) {
                ws.send(JSON.stringify({type: 'syncUsers', data: data}));
            }
        });
    };

    ws.onmessage = (event) => {
        const response = JSON.parse(event.data);
        if (response.status === 'success') {
            if (response.type === 'syncStudents') {
                localforage.removeItem('surveyData');
            }
            if (response.type === 'syncUsers') {
                localforage.removeItem('pendingUsers');
            }
        }
    };

    ws.onerror = () => {
        // Reintentar después de 5 segundos si hay error en la conexión
        setTimeout(syncData, 5000);
    };
}

self.addEventListener('message', (e) => {
    if (e.data === 'sync') {
        syncData();
    }
});