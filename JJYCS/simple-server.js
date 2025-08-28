const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = 3001;
const BACKEND_URL = 'http://localhost:8080';

// API 프록시 설정: /api/* 요청을 백엔드로 전달
app.use('/api', createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    onProxyReq: (proxyReq, req, res) => {
        console.log(`🔄 [PROXY] ${req.method} ${req.url} → ${BACKEND_URL}${req.url}`);
    },
    onError: (err, req, res) => {
        console.error(`❌ [PROXY ERROR] ${err.message}`);
        res.status(502).json({
            success: false,
            error: 'Backend server is not available. Please make sure the backend is running on port 8080.',
            details: err.message
        });
    }
}));

// 정적 파일 서빙 (Vue 빌드 결과물)
app.use(express.static(path.join(__dirname, 'frontend', 'dist')));

// SPA History API Fallback - 모든 라우트는 index.html로
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'dist', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`
╔════════════════════════════════════════════════════════╗
║                                                        ║
║   🚀 YCS LMS Express Server Started                   ║
║                                                        ║
║   📱 Frontend: http://localhost:${PORT}                   ║
║   🔄 API Proxy: /api/* → ${BACKEND_URL}/api/*         ║
║                                                        ║
║   📋 테스트:                                          ║
║   • http://localhost:${PORT}/login                       ║
║   • http://localhost:${PORT}/admin                       ║
║                                                        ║
║   ⚠️  백엔드가 필요합니다: ./backend/mvnw.cmd spring-boot:run ║
║   종료하려면 Ctrl+C를 누르세요                        ║
║                                                        ║
╚════════════════════════════════════════════════════════╝
    `);
});

process.on('SIGINT', () => {
    console.log('\n\n👋 서버를 종료합니다...');
    process.exit();
});