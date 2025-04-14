// js/formulario-estudiante.js

// Animación de entrada del formulario
window.addEventListener('load', () => {
    // Si no estamos en modo edición, se obtiene la ubicación
    if (!getIsEditMode()) {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                position => {
                    document.getElementById('latitud').value = position.coords.latitude;
                    document.getElementById('longitud').value = position.coords.longitude;
                },
                error => {
                    console.error("Error obteniendo ubicación: ", error);
                    // Opcional: Mostrar mensaje al usuario
                    alert("Debes permitir la ubicación para registrar al estudiante.");
                }
            );
        } else {
            alert("Tu navegador no soporta geolocalización.");
        }
    }
    cargarRegistroPendienteEstudiante();
});

// Función para determinar si estamos en modo edición (mediante URL)
function getIsEditMode() {
    const params = new URLSearchParams(window.location.search);
    return params.has('editPending');
}

// Guardado offline: se guarda en LocalForage y se redirige al dashboard.
document.getElementById('estudianteForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const formData = {
            nombre: document.querySelector('[name="nombre"]').value,
            sector: document.querySelector('[name="sector"]').value,
            nivelEscolar: document.querySelector('[name="nivelEscolar"]').value,
            latitud: document.querySelector('[name="latitud"]').value || "0",
            longitud: document.querySelector('[name="longitud"]').value || "0"
        };
        localforage.getItem('surveyData').then(existing => {
            let records = existing || [];
            const params = new URLSearchParams(window.location.search);
            const editIndex = params.get('editPending');
            if (editIndex !== null) {
                records[editIndex] = formData;
            } else {
                records.push(formData);
            }
            return localforage.setItem('surveyData', records);
        }).then(() => {
            window.location.href = "/crud-estudiante/crear" +
                "";
        });

});

// Cargar registro pendiente para edición (si existe el parámetro editPending)
function cargarRegistroPendienteEstudiante() {
    const params = new URLSearchParams(window.location.search);
    const editIndex = params.get('editPending');
    if (editIndex !== null) {
        localforage.getItem('surveyData').then(records => {
            records = records || [];
            const registro = records[editIndex];
            if (registro) {
                document.querySelector('[name="nombre"]').value = registro.nombre;
                document.querySelector('[name="sector"]').value = registro.sector;
                document.querySelector('[name="nivelEscolar"]').value = registro.nivelEscolar;
                document.getElementById('latitud').value = registro.latitud;
                document.getElementById('longitud').value = registro.longitud;
            }
        });
    }
}
