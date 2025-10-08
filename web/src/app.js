// Edge Detection Web Viewer
// This JavaScript application displays processed frames from the Android app

class EdgeDetectionViewer {
    constructor() {
        this.initializeElements();
        this.setupEventListeners();
        this.updateStatus("Ready");
    }
    
    initializeElements() {
        this.frameDisplay = document.getElementById('frameDisplay');
        this.fpsValue = document.getElementById('fpsValue');
        this.resolutionValue = document.getElementById('resolutionValue');
        this.processingTimeValue = document.getElementById('processingTime');
        this.statusValue = document.getElementById('statusValue');
        this.startBtn = document.getElementById('startBtn');
        this.stopBtn = document.getElementById('stopBtn');
    }
    
    setupEventListeners() {
        this.startBtn.addEventListener('click', () => this.startStreaming());
        this.stopBtn.addEventListener('click', () => this.stopStreaming());
    }
    
    updateStatus(status) {
        this.statusValue.textContent = status;
    }
    
    updateStats(stats) {
        this.fpsValue.textContent = stats.fps.toFixed(1);
        this.resolutionValue.textContent = stats.resolution;
        this.processingTimeValue.textContent = `${stats.processingTime} ms`;
    }
    
    startStreaming() {
        if (this.isStreaming) return;
        
        this.isStreaming = true;
        this.startBtn.disabled = true;
        this.stopBtn.disabled = false;
        this.updateStatus("Streaming");
        
        // Start simulated frame updates
        this.simulateFrameUpdates();
    }
    
    stopStreaming() {
        this.isStreaming = false;
        this.startBtn.disabled = false;
        this.stopBtn.disabled = true;
        this.updateStatus("Stopped");
    }
    
    simulateFrameUpdates() {
        if (!this.isStreaming) return;
        
        // Simulate receiving a processed frame
        this.receiveFrame();
        
        // Schedule next frame update
        setTimeout(() => this.simulateFrameUpdates(), 100); // ~10 FPS
    }
    
    receiveFrame() {
        // In a real implementation, this would receive actual frame data
        // For demo purposes, we'll use a placeholder image
        const timestamp = new Date().getTime();
        this.frameDisplay.src = `https://picsum.photos/640/480?random=${timestamp}`;
        
        // Update frame statistics
        this.frameCount = (this.frameCount || 0) + 1;
        
        const now = performance.now();
        if (this.lastFrameTime > 0) {
            const frameTime = now - this.lastFrameTime;
            this.fps = 0.9 * (this.fps || 0) + 0.1 * (1000 / frameTime);
        }
        this.lastFrameTime = now;
        
        // Update stats display
        const stats = {
            fps: this.fps || 0,
            resolution: "640x480",
            processingTime: Math.floor(Math.random() * 20) + 5 // Random processing time between 5-25ms
        };
        
        this.updateStats(stats);
    }
    
    // In a real implementation, these methods would handle actual communication
    // with the Android app via WebSocket or HTTP requests
    
    connectToAndroidApp() {
        // TODO: Implement WebSocket connection to Android app
        console.log("Connecting to Android app...");
        this.updateStatus("Connecting...");
        
        // Simulate connection
        setTimeout(() => {
            console.log("Connected to Android app");
            this.updateStatus("Connected");
        }, 1000);
    }
    
    disconnectFromAndroidApp() {
        // TODO: Implement disconnection from Android app
        console.log("Disconnecting from Android app...");
        this.updateStatus("Disconnecting...");
        
        // Simulate disconnection
        setTimeout(() => {
            console.log("Disconnected from Android app");
            this.updateStatus("Disconnected");
        }, 500);
    }
}

// Initialize the viewer when the page loads
document.addEventListener('DOMContentLoaded', () => {
    const viewer = new EdgeDetectionViewer();
    
    // For demo purposes, you might want to automatically start streaming
    // viewer.startStreaming();
});