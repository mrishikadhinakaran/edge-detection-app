// @ts-nocheck
const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Serve static files from the public directory
app.use(express.static(path.join(__dirname, '../public')));

// Serve the main page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

// API endpoint to receive processed frames (simulated)
app.get('/api/frame', (req, res) => {
    // In a real implementation, this would return actual processed frame data
    // For demo purposes, we'll send a placeholder response
    res.json({
        timestamp: new Date().toISOString(),
        frameData: 'base64-encoded-frame-data-placeholder',
        width: 640,
        height: 480
    });
});

// API endpoint to get frame statistics
app.get('/api/stats', (req, res) => {
    // In a real implementation, this would return actual processing statistics
    res.json({
        fps: Math.random() * 30 + 10, // Random FPS between 10-40
        processingTime: Math.floor(Math.random() * 50) + 10, // Random time between 10-60ms
        resolution: "640x480"
    });
});

app.listen(PORT, () => {
    console.log(`Edge Detection Viewer server is running on http://localhost:${PORT}`);
});