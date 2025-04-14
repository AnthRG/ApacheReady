//offlineSync


if (window.Worker) {
    const syncWorker = new Worker('/js/syncWorker.js');

    function checkOnlineStatus() {
        if (navigator.onLine) {
            syncWorker.postMessage('sync');
        }
    }

    window.addEventListener('online', checkOnlineStatus);
    window.addEventListener('load', checkOnlineStatus);
}