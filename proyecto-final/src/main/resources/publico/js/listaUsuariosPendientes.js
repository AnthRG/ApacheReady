// js/listarUsuariosPendientes.js

// Función modificada para crear filas de tabla
function loadPendingRecordsDashboard() {
    localforage.getItem('surveyData').then(students => {
        const tbody = document.getElementById('pendingStudentsList');
        tbody.innerHTML = '';

        (students || []).forEach((student, index) => {
            tbody.innerHTML += `
                <tr>
                    <td>${student.nombre}</td>
                    <td>${student.sector}</td>
                    <td>${student.nivelEscolar}</td>
                    <td>
                        <button onclick="sendPendingStudent(${index})" class="btn btn-success btn-sm">
                            <i class="fas fa-cloud-upload-alt"></i> Enviar
                        </button>
                        <button onclick="editPendingStudent(${index})" class="btn btn-warning btn-sm">
                            <i class="fas fa-edit"></i>Editar
                        </button>

                        <button onclick="deletePendingStudent(${index})" class="btn btn-danger btn-sm">
                            <i class="fas fa-trash"></i> Eliminar
                        </button>
                    </td>
                </tr>`;
        });
    });
}

function sendPendingStudent(index) {
    localforage.getItem('surveyData').then(records => {
        records = records || [];
        const student = records[index];
        return fetch('/crud-estudiante/crear', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams(student)
        }).then(response => {
            if (response.ok) {
                records.splice(index, 1);
                return localforage.setItem('surveyData', records);
            } else {
                alert("Error al enviar registro: " + response.statusText);
            }
        });
    }).then(() => window.location.reload());
}

function editPendingStudent(index) {
    window.location.href = "/crud-estudiante/crear?editPending=" + index;
}

function deletePendingStudent(index) {
    localforage.getItem('surveyData').then(records => {
        records = records || [];
        records.splice(index, 1);
        return localforage.setItem('surveyData', records);
    }).then(() => loadPendingRecordsDashboard());
}

window.addEventListener('load', loadPendingRecordsDashboard);

