const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const HTML_DIR = path.join(__dirname, 'html');
const BACKEND_URL = 'http://localhost:8080';

// MIME 타입 매핑
const mimeTypes = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

// API 프록시 함수
function proxyToBackend(req, res) {
    const options = {
        hostname: 'localhost',
        port: 8080,
        path: req.url,
        method: req.method,
        headers: req.headers
    };

    console.log(`🔄 [PROXY] ${req.method} ${req.url} → ${BACKEND_URL}${req.url}`);

    const proxyReq = http.request(options, (proxyRes) => {
        // CORS 헤더 추가
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
        
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
        console.error(`❌ [PROXY ERROR] ${err.message}`);
        res.writeHead(502, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            success: false,
            error: 'Backend server is not available. Please make sure the backend is running on port 8080.',
            details: err.message
        }));
    });

    // POST 데이터 전달
    if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
        let body = '';
        req.on('data', (chunk) => {
            body += chunk.toString();
        });
        req.on('end', () => {
            proxyReq.write(body);
            proxyReq.end();
        });
    } else {
        proxyReq.end();
    }
}

const server = http.createServer((req, res) => {
    const pathname = url.parse(req.url).pathname;
    
    // OPTIONS 요청 처리 (CORS preflight)
    if (req.method === 'OPTIONS') {
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
        res.writeHead(200);
        res.end();
        return;
    }

    // API 요청은 백엔드로 프록시
    if (pathname.startsWith('/api/')) {
        proxyToBackend(req, res);
        return;
    }

    // 정적 파일 서빙
    console.log(`📄 [STATIC] ${req.method} ${req.url}`);
    
    let requestPath = pathname === '/' ? '/index.html' : pathname;
    let filePath = path.join(HTML_DIR, requestPath);
    
    // 디렉토리 요청 시 index.html 찾기
    if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
        filePath = path.join(filePath, 'index.html');
    }
    
    const extname = String(path.extname(filePath)).toLowerCase();
    const contentType = mimeTypes[extname] || 'application/octet-stream';
    
    fs.readFile(filePath, (error, content) => {
        if (error) {
            if (error.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/html' });
                res.end(`
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
                            <p>요청한 경로: ${pathname}</p>
                            <a href="/">홈으로 돌아가기</a>
                        </div>
                    </body>
                    </html>
                `, 'utf-8');
            } else {
                res.writeHead(500);
                res.end(`서버 오류: ${error.code}`);
            }
        } else {
            res.writeHead(200, { 
                'Content-Type': contentType,
                'Cache-Control': 'no-cache'
            });
            res.end(content, 'utf-8');
        }
    });
});

server.listen(PORT, () => {
    console.log(`
╔════════════════════════════════════════════════════════╗
║                                                        ║
║   🚀 YCS 물류 시스템 개발 서버가 시작되었습니다!      ║
║                                                        ║
║   📱 Frontend: http://localhost:${PORT}                   ║
║   🔄 API Proxy: /api/* → http://localhost:8080/api/*   ║
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