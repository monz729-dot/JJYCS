#!/usr/bin/env node
/**
 * TSX to HTML Converter
 * TSX 파일을 HTML로 변환하는 스크립트
 */

const fs = require('fs');
const path = require('path');

class TSXToHTMLConverter {
    constructor(sourceDir, outputDir) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
    }

    convertTsxToHtml(tsxContent, componentName) {
        const htmlTemplate = `<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${this.extractComponentTitle(componentName)}</title>
    <link rel="stylesheet" href="../../css/globals.css">
    <style>
        ${this.extractStyles(tsxContent)}
    </style>
</head>
<body>
    <div id="root">
        ${this.convertJsxToHtml(this.extractJsxContent(tsxContent))}
    </div>
    
    <script src="../../js/utils.js"></script>
    <script>
        // 컴포넌트 초기화
        let state = ${this.extractStateInit(tsxContent)};
        let props = {};
        
        // 부모 창으로부터 데이터 수신
        window.addEventListener('message', (event) => {
            if (event.data.type === 'APP_STATE') {
                props.user = event.data.user;
                props.orderId = event.data.orderId;
                props.userType = event.data.userType;
                initComponent();
            }
        });
        
        // 페이지 네비게이션
        function navigateTo(page, options = {}) {
            window.parent.postMessage({
                type: 'NAVIGATE',
                page: page,
                options: options
            }, '*');
        }
        
        // 로그인 처리
        function handleLogin(user) {
            window.parent.postMessage({
                type: 'LOGIN',
                user: user
            }, '*');
        }
        
        // 로그아웃 처리
        function handleLogout() {
            window.parent.postMessage({
                type: 'LOGOUT'
            }, '*');
        }
        
        // 컴포넌트 초기화 함수
        function initComponent() {
            ${this.generateInitScript(tsxContent)}
        }
        
        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', () => {
            initComponent();
        });
        
        ${this.generateComponentScript(tsxContent)}
    </script>
</body>
</html>`;
        
        return htmlTemplate;
    }

    extractComponentTitle(componentName) {
        // CamelCase를 공백으로 분리
        let title = componentName.replace(/([A-Z])/g, ' $1').trim();
        title = title.replace('Page', '').replace('Component', '').trim();
        return `${title} - YCS 물류 시스템`;
    }

    extractJsxContent(tsxContent) {
        // return 문 찾기
        let match = tsxContent.match(/return\s*\(([\s\S]*?)\);/);
        if (!match) {
            match = tsxContent.match(/return\s*<([\s\S]*?)>;/);
        }
        
        if (match) {
            return match[1] || `<${match[1]}>`;
        }
        return "<div>컨텐츠를 불러올 수 없습니다.</div>";
    }

    convertJsxToHtml(jsxContent) {
        let html = jsxContent;
        
        // className -> class
        html = html.replace(/className=/g, 'class=');
        
        // onClick -> onclick
        html = html.replace(/onClick=\{([^}]*)\}/g, 'onclick="$1()"');
        html = html.replace(/onChange=\{([^}]*)\}/g, 'onchange="$1(event)"');
        html = html.replace(/onSubmit=\{([^}]*)\}/g, 'onsubmit="$1(event)"');
        
        // {변수} -> <span id="변수"></span>
        html = html.replace(/\{([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)*)\}/g, 
                           '<span id="data-$1"></span>');
        
        // {조건 && 내용} 처리
        html = html.replace(/\{([^}]*)\s*&&\s*\(([\s\S]*?)\)\}/g, 
                           '<div class="conditional" data-condition="$1">$2</div>');
        
        // map 함수 처리
        html = html.replace(/\{([^}]*?)\.map\([^)]*\)\s*=>\s*\(([\s\S]*?)\)\)}/g,
                           '<div class="list-container" data-source="$1"></div>');
        
        // 자체 닫는 태그 변환
        html = html.replace(/<([^>]+)\/>/g, '<$1></$1>');
        
        return html;
    }

    extractStyles(tsxContent) {
        // 기본 스타일
        const defaultStyles = `
        body {
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .button {
            padding: 10px 20px;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            font-weight: 500;
            transition: all 0.3s;
        }
        
        .button-primary {
            background: rgb(59 130 246);
            color: white;
        }
        
        .button-primary:hover {
            background: rgb(37 99 235);
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
        }
        
        .form-input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 16px;
        }
        
        .grid {
            display: grid;
            gap: 20px;
        }
        
        .grid-cols-2 {
            grid-template-columns: repeat(2, 1fr);
        }
        
        @media (max-width: 768px) {
            .grid-cols-2 {
                grid-template-columns: 1fr;
            }
        }
        `;
        
        return defaultStyles;
    }

    extractStateInit(tsxContent) {
        const stateVars = {};
        
        // useState 패턴 찾기
        const pattern = /const\s*\[(\w+),\s*set\w+\]\s*=\s*useState\((.*?)\);/g;
        let match;
        
        while ((match = pattern.exec(tsxContent)) !== null) {
            const varName = match[1];
            let initialValue = match[2];
            
            // 초기값 정리
            if (initialValue === '') {
                initialValue = '""';
            } else if (initialValue === 'null') {
                initialValue = 'null';
            } else if (initialValue === 'false' || initialValue === 'true') {
                initialValue = initialValue;
            } else if (initialValue.startsWith('[')) {
                initialValue = initialValue;
            } else if (initialValue.startsWith('{')) {
                initialValue = initialValue;
            } else {
                initialValue = `"${initialValue}"`;
            }
            
            stateVars[varName] = initialValue;
        }
        
        return JSON.stringify(stateVars);
    }

    generateInitScript(tsxContent) {
        const initScript = `
        // 데이터 바인딩
        function updateUI() {
            // State 값을 DOM에 반영
            for (let key in state) {
                const elements = document.querySelectorAll(\`[id="data-\${key}"]\`);
                elements.forEach(el => {
                    if (typeof state[key] === 'object') {
                        el.textContent = JSON.stringify(state[key]);
                    } else {
                        el.textContent = state[key];
                    }
                });
            }
            
            // Props 값을 DOM에 반영
            for (let key in props) {
                const elements = document.querySelectorAll(\`[id="prop-\${key}"]\`);
                elements.forEach(el => {
                    if (typeof props[key] === 'object' && props[key] !== null) {
                        if (props[key].name) {
                            el.textContent = props[key].name;
                        } else {
                            el.textContent = JSON.stringify(props[key]);
                        }
                    } else {
                        el.textContent = props[key];
                    }
                });
            }
        }
        
        // State 업데이트 함수
        function setState(updates) {
            state = { ...state, ...updates };
            updateUI();
        }
        
        // 초기 UI 업데이트
        updateUI();
        `;
        
        return initScript;
    }

    generateComponentScript(tsxContent) {
        const scripts = [];
        
        // 함수 정의 찾기
        const pattern = /const\s+(\w+)\s*=\s*\([^)]*\)\s*=>\s*{([^}]*)}/g;
        let match;
        
        while ((match = pattern.exec(tsxContent)) !== null) {
            const funcName = match[1];
            const funcBody = match[2];
            
            // React 함수를 일반 JavaScript 함수로 변환
            const jsFunc = `
        function ${funcName}(event) {
            if (event) event.preventDefault();
            ${funcBody}
        }`;
            scripts.push(jsFunc);
        }
        
        return scripts.join('\n');
    }

    convertFile(tsxFile) {
        console.log(`Converting ${path.basename(tsxFile)}...`);
        
        // TSX 내용 읽기
        const tsxContent = fs.readFileSync(tsxFile, 'utf-8');
        
        // 컴포넌트 이름 추출
        const componentName = path.basename(tsxFile, '.tsx');
        
        // HTML로 변환
        const htmlContent = this.convertTsxToHtml(tsxContent, componentName);
        
        // 출력 경로 결정
        const outputPath = this.determineOutputPath(componentName);
        
        // 디렉토리 생성
        const outputDir = path.dirname(outputPath);
        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }
        
        // HTML 파일 저장
        fs.writeFileSync(outputPath, htmlContent, 'utf-8');
        
        console.log(`  -> Saved to ${outputPath}`);
        return outputPath;
    }

    determineOutputPath(componentName) {
        const nameLower = componentName.toLowerCase();
        let folder;
        
        // 경로 매핑
        if (nameLower.includes('admin')) {
            folder = 'pages/admin';
        } else if (nameLower.includes('partner')) {
            folder = 'pages/partner';
        } else if (nameLower.includes('order') || nameLower.includes('payment')) {
            folder = 'pages/order';
        } else if (['login', 'signup', 'find', 'dashboard', 'mypage', 'faq', 'notice', 'workflow', 'pagelist'].some(x => nameLower.includes(x))) {
            folder = 'pages/common';
        } else if (['navigation', 'footer'].some(x => nameLower.includes(x))) {
            folder = 'components';
        } else {
            folder = 'pages/common';
        }
        
        // 파일명 변환 (CamelCase -> kebab-case)
        let fileName = componentName.replace(/([A-Z])/g, '-$1').toLowerCase();
        fileName = fileName.replace('page', '').replace('--', '-').replace(/^-|-$/g, '');
        
        return path.join(this.outputDir, folder, `${fileName}.html`);
    }

    convertAll() {
        // components 폴더의 TSX 파일 찾기
        const componentsDir = path.join(this.sourceDir, 'components');
        const tsxFiles = fs.readdirSync(componentsDir)
            .filter(file => file.endsWith('.tsx'))
            .map(file => path.join(componentsDir, file));
        
        console.log(`Found ${tsxFiles.length} TSX files to convert`);
        console.log('='.repeat(50));
        
        const convertedFiles = [];
        for (const tsxFile of tsxFiles) {
            try {
                const outputPath = this.convertFile(tsxFile);
                convertedFiles.push(outputPath);
            } catch (error) {
                console.log(`  ERROR: ${error.message}`);
            }
        }
        
        console.log('='.repeat(50));
        console.log(`Successfully converted ${convertedFiles.length} files`);
        
        return convertedFiles;
    }
}

// 실행
if (require.main === module) {
    const sourceDir = 'C:\\YCS-ver2\\jjycs';
    const outputDir = 'C:\\YCS-ver2\\jjycs\\html';
    
    const converter = new TSXToHTMLConverter(sourceDir, outputDir);
    converter.convertAll();
    
    console.log('\n변환 완료!');
}