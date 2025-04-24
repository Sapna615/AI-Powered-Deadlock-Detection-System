<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Deadlock Detection System</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .container {
            width: 90%;
            margin: 30px auto;
            padding: 20px;
            box-shadow: 0 0 15px rgba(0,0,0,0.1);
            border-radius: 12px;
            background-color: #ffffff;
        }
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f2f5;
        }
        h1 {
            text-align: center;
            color: #2c3e50;
            margin-bottom: 30px;
        }
        .input-section {
            background: #fff;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }
        .input-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        .input-group {
            margin-bottom: 15px;
        }
        .input-group label {
            display: block;
            margin-bottom: 5px;
            color: #2c3e50;
            font-weight: bold;
        }
        .input-group input, .input-group select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .btn {
            background: #2c3e50;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
        }
        .btn:hover {
            background: #34495e;
        }
        .graphs-container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        .graph-box {
            background: #fff;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
            height: 300px;
        }
        .metrics {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            margin-top: 20px;
        }
        .metric-card {
            background: #2c3e50;
            color: white;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
        }
        .metric-value {
            font-size: 24px;
            font-weight: bold;
            margin: 10px 0;
        }
        #processTable {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        #processTable th, #processTable td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: center;
        }
        #processTable th {
            background-color: #2c3e50;
            color: white;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>AI-Powered Deadlock Detection System</h1>
        
        <div class="input-section">
            <div class="input-grid">
                <div class="input-group">
                    <label for="numProcesses">Number of Processes:</label>
                    <input type="number" id="numProcesses" min="1" max="10" value="3">
                </div>
                <div class="input-group">
                    <label for="numResources">Number of Resources:</label>
                    <input type="number" id="numResources" min="1" max="10" value="3">
                </div>
            </div>
            <button class="btn" onclick="generateTable()">Generate Input Table</button>
        </div>

        <div class="input-section">
            <h3>Resource Allocation Matrix</h3>
            <div id="tableContainer"></div>
            <button class="btn" onclick="analyzeDeadlock()" style="margin-top: 15px;">Analyze Deadlock</button>
        </div>

        <div class="graphs-container">
            <div class="graph-box">
                <canvas id="resourceAllocationChart"></canvas>
            </div>
            <div class="graph-box">
                <canvas id="deadlockPredictionChart"></canvas>
            </div>
        </div>
        
        <div class="metrics">
            <div class="metric-card">
                <h3>Active Processes</h3>
                <div id="activeProcesses" class="metric-value">0</div>
            </div>
            <div class="metric-card">
                <h3>Deadlock Risk</h3>
                <div id="deadlockRisk" class="metric-value">0%</div>
            </div>
            <div class="metric-card">
                <h3>Resources Used</h3>
                <div id="resourcesUsed" class="metric-value">0%</div>
            </div>
        </div>
    </div>

    <script>
        let resourceAllocationChart = null;
        let deadlockPredictionChart = null;

        function generateTable() {
            const numProcesses = parseInt(document.getElementById('numProcesses').value);
            const numResources = parseInt(document.getElementById('numResources').value);
            
            let tableHTML = `
                <table id="processTable">
                    <tr>
                        <th>Process</th>
                        <th>Allocated Resources</th>
                        <th>Maximum Need</th>
                    </tr>
            `;

            for (let i = 0; i < numProcesses; i++) {
                tableHTML += `
                    <tr>
                        <td>P${i}</td>
                        <td>
                            <input type="text" placeholder="e.g., 1,2,3" 
                                   id="allocated${i}" class="resource-input">
                        </td>
                        <td>
                            <input type="text" placeholder="e.g., 4,5,6" 
                                   id="maximum${i}" class="resource-input">
                        </td>
                    </tr>
                `;
            }

            tableHTML += `
                <tr>
                    <td>Available</td>
                    <td colspan="2">
                        <input type="text" placeholder="e.g., 3,3,3" 
                               id="available" class="resource-input">
                    </td>
                </tr>
            `;

            tableHTML += '</table>';
            document.getElementById('tableContainer').innerHTML = tableHTML;
        }

        function analyzeDeadlock() {
            const numProcesses = parseInt(document.getElementById('numProcesses').value);
            const numResources = parseInt(document.getElementById('numResources').value);
            
            // Collect input data
            let allocated = [];
            let maximum = [];
            for (let i = 0; i < numProcesses; i++) {
                allocated.push(document.getElementById(`allocated${i}`).value.split(',').map(Number));
                maximum.push(document.getElementById(`maximum${i}`).value.split(',').map(Number));
            }
            let available = document.getElementById('available').value.split(',').map(Number);

            // Calculate metrics
            const totalResources = available.reduce((a, b) => a + b, 0);
            const usedResources = allocated.reduce((sum, proc) => 
                sum + proc.reduce((a, b) => a + b, 0), 0);
            
            // Update metrics
            document.getElementById('activeProcesses').textContent = numProcesses;
            document.getElementById('resourcesUsed').textContent = 
                Math.round((usedResources / (usedResources + totalResources)) * 100) + '%';
            
            // Calculate deadlock risk based on resource utilization
            const deadlockRisk = calculateDeadlockRisk(allocated, maximum, available);
            document.getElementById('deadlockRisk').textContent = deadlockRisk + '%';

            updateCharts(allocated, maximum, available);
        }

        function calculateDeadlockRisk(allocated, maximum, available) {
            // Simple deadlock risk calculation
            let risk = 0;
            const numProcesses = allocated.length;
            
            // Check resource utilization
            for (let i = 0; i < numProcesses; i++) {
                const processUtilization = allocated[i].reduce((a, b) => a + b, 0) / 
                                        maximum[i].reduce((a, b) => a + b, 0);
                risk += processUtilization;
            }
            
            return Math.min(Math.round((risk / numProcesses) * 100), 100);
        }

        function updateCharts(allocated, maximum, available) {
            const processLabels = Array.from({length: allocated.length}, (_, i) => `P${i}`);
            
            // Update Resource Allocation Chart
            if (resourceAllocationChart) {
                resourceAllocationChart.destroy();
            }
            
            const resourceCtx = document.getElementById('resourceAllocationChart').getContext('2d');
            resourceAllocationChart = new Chart(resourceCtx, {
                type: 'bar',
                data: {
                    labels: processLabels,
                    datasets: [{
                        label: 'Allocated',
                        data: allocated.map(proc => proc.reduce((a, b) => a + b, 0)),
                        backgroundColor: 'rgba(54, 162, 235, 0.5)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1
                    }, {
                        label: 'Maximum',
                        data: maximum.map(proc => proc.reduce((a, b) => a + b, 0)),
                        backgroundColor: 'rgba(255, 99, 132, 0.5)',
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        title: {
                            display: true,
                            text: 'Resource Allocation Status'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Resource Units'
                            }
                        }
                    }
                }
            });

            // Update Deadlock Prediction Chart
            if (deadlockPredictionChart) {
                deadlockPredictionChart.destroy();
            }

            const timeLabels = ['T0', 'T1', 'T2', 'T3', 'T4', 'T5'];
            const deadlockRisk = calculateDeadlockRisk(allocated, maximum, available);
            const predictionData = [
                deadlockRisk,
                deadlockRisk + Math.random() * 10 - 5,
                deadlockRisk + Math.random() * 15 - 7.5,
                deadlockRisk + Math.random() * 20 - 10,
                deadlockRisk + Math.random() * 25 - 12.5,
                deadlockRisk + Math.random() * 30 - 15
            ].map(val => Math.min(Math.max(val, 0), 100));

            const deadlockCtx = document.getElementById('deadlockPredictionChart').getContext('2d');
            deadlockPredictionChart = new Chart(deadlockCtx, {
                type: 'line',
                data: {
                    labels: timeLabels,
                    datasets: [{
                        label: 'Deadlock Risk Prediction',
                        data: predictionData,
                        fill: true,
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        title: {
                            display: true,
                            text: 'Deadlock Risk Prediction'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            title: {
                                display: true,
                                text: 'Risk (%)'
                            }
                        }
                    }
                }
            });
        }

        // Initialize empty table on page load
        window.onload = generateTable;
    </script>
</body>
</html>
