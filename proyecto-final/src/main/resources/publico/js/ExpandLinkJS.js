// path /js/ExpandLinkJS.js



document.addEventListener('DOMContentLoaded', () => {

    // Sidebar toggle functionality
    const toggleSidebar = () => {
        const sidebar = document.getElementById('sidebar');
        const logo = document.getElementById('logo');

        if (sidebar.classList.contains('expanded')) {
            sidebar.classList.replace('expanded', 'collapsed');
            logo.style.visibility = 'hidden';
        } else {
            sidebar.classList.replace('collapsed', 'expanded');
            logo.style.visibility = 'visible';
        }
    };

    // Attach sidebar toggle event
    document.getElementById('toggle-btn').addEventListener('click', toggleSidebar);

    // Initialize Chart.js with injected Thymeleaf data
    const ctx = document.getElementById('visitChart').getContext('2d');

    const visitChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: initialChartLabels,
            datasets: [{
                label: 'Visitas',
                data: initialChartData,
                backgroundColor: 'rgba(155, 89, 182, 0.2)',
                borderColor: 'rgba(155, 89, 182, 1)',
                borderWidth: 1,
                fill: true
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { color: '#fff' }
                },
                x: {
                    ticks: { color: '#fff' }
                }
            },
            plugins: {
                legend: {
                    labels: { color: '#fff' }
                }
            }
        }
    });

    // Function to fetch updated data from backend API
    const fetchAndUpdateChart = () => {
        const period = document.getElementById('periodSelect').value;
        const date = document.getElementById('datePicker').value;

        fetch(`${apiUrl}?period=${period}&date=${date}`)
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                visitChart.data.labels = data.labels;
                visitChart.data.datasets[0].data = data.data;
                visitChart.update();
            })
            .catch(error => console.error('Error fetching data:', error));
    };

    // Event listeners to trigger chart updates
    document.getElementById('periodSelect').addEventListener('change', fetchAndUpdateChart);
    document.getElementById('datePicker').addEventListener('change', fetchAndUpdateChart);

    // Initial chart data fetch (optional, if initial data isn't fully loaded from Thymeleaf)
    fetchAndUpdateChart();
});
