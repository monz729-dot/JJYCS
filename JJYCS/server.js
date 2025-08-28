const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = 3000;
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

// 정적 파일 서빙 (html 폴더)
app.use(express.static(path.join(__dirname, 'html'), {
    setHeaders: (res, path) => {
        // 캐시 비활성화
        res.setHeader('Cache-Control', 'no-cache');
        console.log(`📄 [STATIC] ${path}`);
    }
}));

// 루트 경로 처리
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'html', 'index.html'));
});

// 404 처리
app.use((req, res) => {
    res.status(404).send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>404 - Not Found</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                }
                .error-container {
                    text-align: center;
                    color: white;
                }
                h1 { font-size: 72px; margin: 0; }
                p { font-size: 24px; }
                a {
                    color: white;
                    background: rgba(255,255,255,0.2);
                    padding: 10px 20px;
                    border-radius: 25px;
                    text-decoration: none;
                    display: inline-block;
                    margin-top: 20px;
                }
                a:hover { background: rgba(255,255,255,0.3); }
            </style>
        </head>
        <body>
            <div class="error-container">
                <h1>404</h1>
                <p>페이지를 찾을 수 없습니다</p>
                <p>요청한 경로: ${req.path}</p>
                <a href="/">홈으로 돌아가기</a>
            </div>
        </body>
        </html>
    `);
});

app.listen(PORT, () => {
    console.log(`
╔════════════════════════════════════════════════════════╗
║                                                        ║
║   🚀 YCS 물류 시스템 개발 서버가 시작되었습니다!      ║
║                                                        ║
║   📱 Frontend: http://localhost:${PORT}                   ║
║   🔄 API Proxy: /api/* → ${BACKEND_URL}/api/*         ║
║                                                        ║
║   📋 테스트 페이지:                                   ║
║   • http://localhost:${PORT}/auth-login.html             ║
║   • http://localhost:${PORT}/dashboard-general.html     ║
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