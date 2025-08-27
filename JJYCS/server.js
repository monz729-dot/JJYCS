const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const HTML_DIR = path.join(__dirname, 'html');

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

const server = http.createServer((req, res) => {
    console.log(`${req.method} ${req.url}`);
    
    // URL 파싱
    let pathname = url.parse(req.url).pathname;
    
    // 기본 경로 처리
    if (pathname === '/') {
        pathname = '/index.html';
    }
    
    // 파일 경로 생성
    let filePath = path.join(HTML_DIR, pathname);
    
    // 디렉토리 요청 시 index.html 찾기
    if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
        filePath = path.join(filePath, 'index.html');
    }
    
    // 파일 확장자 가져오기
    const extname = String(path.extname(filePath)).toLowerCase();
    const contentType = mimeTypes[extname] || 'application/octet-stream';
    
    // 파일 읽기 및 응답
    fs.readFile(filePath, (error, content) => {
        if (error) {
            if (error.code === 'ENOENT') {
                // 404 에러
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
                            a:hover {
                                background: rgba(255,255,255,0.3);
                            }
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
                // 500 에러
                res.writeHead(500);
                res.end(`서버 오류: ${error.code}`);
            }
        } else {
            // 성공
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
║   서버 주소: http://localhost:${PORT}                     ║
║   테스트 페이지: http://localhost:${PORT}/test.html       ║
║                                                        ║
║   종료하려면 Ctrl+C를 누르세요                        ║
║                                                        ║
╚════════════════════════════════════════════════════════╝
    `);
});

// 종료 시그널 처리
process.on('SIGINT', () => {
    console.log('\n\n👋 서버를 종료합니다...');
    process.exit();
});