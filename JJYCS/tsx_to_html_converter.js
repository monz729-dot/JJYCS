#!/usr/bin/env node
/**
 * Advanced TSX to HTML Converter
 * 실제 TSX 파일 내용을 그대로 HTML로 변환
 */

const fs = require('fs');
const path = require('path');

class AdvancedTSXToHTMLConverter {
    constructor(sourceDir, outputDir) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        
        // UI 컴포넌트 매핑
        this.uiComponentMap = {
            'Card': { tag: 'div', class: 'card' },
            'CardContent': { tag: 'div', class: 'card-content' },
            'CardHeader': { tag: 'div', class: 'card-header' },
            'CardTitle': { tag: 'h3', class: 'card-title' },
            'Button': { tag: 'button', class: 'btn' },
            'Badge': { tag: 'span', class: 'badge' },
            'Input': { tag: 'input', class: 'form-input' },
            'Separator': { tag: 'hr', class: 'separator' },
            'Table': { tag: 'table', class: 'table' },
            'TableBody': { tag: 'tbody', class: 'table-body' },
            'TableCell': { tag: 'td', class: 'table-cell' },
            'TableHead': { tag: 'th', class: 'table-head' },
            'TableHeader': { tag: 'thead', class: 'table-header' },
            'TableRow': { tag: 'tr', class: 'table-row' },
            'Alert': { tag: 'div', class: 'alert' },
            'AlertDescription': { tag: 'div', class: 'alert-description' }
        };
        
        // 아이콘 매핑 (Lucide React → 이모지/텍스트)
        this.iconMap = {
            'Search': '🔍',
            'Home': '🏠',
            'User': '👤',
            'Package': '📦',
            'FileText': '📄',
            'CreditCard': '💳',
            'Settings': '⚙️',
            'HelpCircle': '❓',
            'UserPlus': '👥',
            'LogIn': '🔑',
            'Shield': '🛡️',
            'Truck': '🚛',
            'Building2': '🏢',
            'BarChart3': '📊',
            'Clock': '⏰',
            'Eye': '👁️',
            'ArrowLeft': '←',
            'ExternalLink': '🔗',
            'Info': 'ℹ️',
            'CheckCircle': '✅',
            'AlertCircle': '⚠️',
            'Copy': '📋',
            'Database': '🗄️',
            'Bell': '🔔',
            'Calendar': '📅',
            'Phone': '📞',
            'DollarSign': '💰',
            'TrendingUp': '📈',
            'AlertTriangle': '⚠️',
            'ArrowRight': '→',
            'Menu': '☰'
        };
    }
    
    // TSX 파일을 HTML로 변환
    convertTSXToHTML(tsxContent, componentName) {
        try {
            // JSX 반환문 추출
            const jsxContent = this.extractJSXFromTSX(tsxContent);
            
            // JSX를 HTML로 변환
            const htmlContent = this.convertJSXToHTML(jsxContent);
            
            // State와 함수들 추출
            const stateVars = this.extractStateVariables(tsxContent);
            const functions = this.extractFunctions(tsxContent);
            const imports = this.extractImports(tsxContent);
            
            // 완전한 HTML 문서 생성
            return this.generateCompleteHTML(componentName, htmlContent, stateVars, functions, imports);
        } catch (error) {
            console.error(`Error converting ${componentName}:`, error.message);
            return this.generateErrorHTML(componentName, error.message);
        }
    }
    
    // TSX에서 JSX 반환문 추출
    extractJSXFromTSX(tsxContent) {
        // export function 또는 export default function에서 return문 찾기
        const functionMatch = tsxContent.match(/export\s+(default\s+)?function\s+\w+[^{]*\{([\s\S]*)\}/);
        if (!functionMatch) {
            throw new Error('No export function found');
        }
        
        const functionBody = functionMatch[2];
        
        // return문에서 JSX 추출
        const returnMatch = functionBody.match(/return\s*\(([\s\S]*?)\);?\s*\}?\s*$/m);
        if (returnMatch) {
            return returnMatch[1].trim();
        }
        
        // return 없이 바로 JSX 반환하는 경우
        const simpleReturnMatch = functionBody.match(/return\s+<([\s\S]*?)>\s*;?\s*\}?\s*$/m);
        if (simpleReturnMatch) {
            return `<${simpleReturnMatch[1]}>`;
        }
        
        throw new Error('No JSX return statement found');
    }
    
    // JSX를 HTML로 변환
    convertJSXToHTML(jsxContent) {
        let html = jsxContent;
        
        // 1. UI 컴포넌트를 HTML 태그로 변환
        for (const [component, mapping] of Object.entries(this.uiComponentMap)) {
            const regex = new RegExp(`<${component}([^>]*)>`, 'g');
            html = html.replace(regex, (match, attributes) => {
                const attrs = this.convertAttributes(attributes);
                return `<${mapping.tag} class="${mapping.class}"${attrs}>`;
            });
            
            // 닫는 태그 변환
            html = html.replace(new RegExp(`</${component}>`, 'g'), `</${mapping.tag}>`);
        }
        
        // 2. 아이콘 컴포넌트를 이모지로 변환
        for (const [iconName, emoji] of Object.entries(this.iconMap)) {
            const iconRegex = new RegExp(`<${iconName}[^>]*/>`, 'g');
            html = html.replace(iconRegex, `<span class="icon">${emoji}</span>`);
        }
        
        // 3. className을 class로 변환
        html = html.replace(/className=/g, 'class=');
        
        // 4. JSX 표현식을 데이터 속성으로 변환
        html = html.replace(/\{([^}]+)\}/g, (match, expression) => {
            // 간단한 변수는 data 속성으로
            if (/^[a-zA-Z_][a-zA-Z0-9_.]*$/.test(expression.trim())) {
                return `<span data-bind="${expression.trim()}"></span>`;
            }
            
            // 함수 호출은 onclick 등으로
            if (expression.includes('(')) {
                return `onclick="${expression.trim()}"`;
            }
            
            // 기타는 그대로 표시
            return `<span data-expr="${expression.trim()}">__${expression.trim()}__</span>`;
        });
        
        // 5. 이벤트 핸들러 변환
        html = html.replace(/onClick=\{([^}]*)\}/g, 'onclick="$1"');
        html = html.replace(/onChange=\{([^}]*)\}/g, 'onchange="$1"');
        html = html.replace(/onSubmit=\{([^}]*)\}/g, 'onsubmit="$1"');
        
        // 6. 자체 닫는 태그를 명시적으로 닫기
        html = html.replace(/<(\w+)([^>]*)\s*\/>/g, '<$1$2></$1>');
        
        // 7. map 함수를 리스트 컨테이너로 변환
        html = html.replace(/\{[\s\S]*?\.map\([\s\S]*?\)[\s\S]*?\}/g, 
            '<div class="list-container" data-list="dynamic"><!-- Dynamic list content --></div>');
        
        // 8. 조건부 렌더링을 조건부 div로 변환
        html = html.replace(/\{([^}]*?)\s*&&\s*\(([\s\S]*?)\)\}/g, 
            '<div class="conditional" data-condition="$1">$2</div>');
        
        return html;
    }
    
    // JSX 속성을 HTML 속성으로 변환
    convertAttributes(attributeString) {
        if (!attributeString.trim()) return '';
        
        let converted = attributeString;
        
        // className을 class로
        converted = converted.replace(/className=/g, 'class=');
        
        // 이벤트 핸들러들
        converted = converted.replace(/onClick=/g, 'onclick=');
        converted = converted.replace(/onChange=/g, 'onchange=');
        converted = converted.replace(/onSubmit=/g, 'onsubmit=');
        
        // JSX 표현식을 문자열로 변환 (간단한 경우만)
        converted = converted.replace(/\{([^}]+)\}/g, '"$1"');
        
        return ' ' + converted;
    }
    
    // State 변수들 추출
    extractStateVariables(tsxContent) {
        const stateVars = {};
        const useStateRegex = /const\s*\[([^,]+),\s*([^]]+)\]\s*=\s*useState\(([^)]*)\)/g;
        
        let match;
        while ((match = useStateRegex.exec(tsxContent)) !== null) {
            const varName = match[1].trim();
            const initialValue = match[3].trim() || 'null';
            stateVars[varName] = initialValue;
        }
        
        return stateVars;
    }
    
    // 함수들 추출
    extractFunctions(tsxContent) {
        const functions = [];
        
        // const 함수들
        const constFunctionRegex = /const\s+(\w+)\s*=\s*\([^)]*\)\s*=>\s*\{([^}]*(?:\{[^}]*\}[^}]*)*)\}/g;
        let match;
        
        while ((match = constFunctionRegex.exec(tsxContent)) !== null) {
            functions.push({
                name: match[1],
                body: match[2].trim()
            });
        }
        
        // 일반 함수들
        const functionRegex = /function\s+(\w+)\s*\([^)]*\)\s*\{([^}]*(?:\{[^}]*\}[^}]*)*)\}/g;
        
        while ((match = functionRegex.exec(tsxContent)) !== null) {
            functions.push({
                name: match[1],
                body: match[2].trim()
            });
        }
        
        return functions;
    }
    
    // Import문들 추출
    extractImports(tsxContent) {
        const imports = [];
        const importRegex = /import\s+[^;]+;/g;
        
        let match;
        while ((match = importRegex.exec(tsxContent)) !== null) {
            imports.push(match[0]);
        }
        
        return imports;
    }
    
    // 완전한 HTML 문서 생성
    generateCompleteHTML(componentName, htmlContent, stateVars, functions, imports) {
        const title = componentName.replace(/([A-Z])/g, ' $1').trim();
        
        return `<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - YCS 물류 시스템</title>
    <link rel="stylesheet" href="../../css/globals.css">
    <style>
        ${this.generateCSS()}
    </style>
</head>
<body>
    <div id="root" class="component-${componentName.toLowerCase()}">
        ${htmlContent}
    </div>
    
    <script src="../../js/utils.js"></script>
    <script>
        // 원본 imports (참조용)
        /* ${imports.join('\n        ')} */
        
        // State 변수들
        let state = ${JSON.stringify(stateVars, null, 8)};
        let props = {};
        
        // 컴포넌트 함수들
        ${functions.map(fn => `
        function ${fn.name}() {
            ${this.convertFunctionBodyToJS(fn.body)}
        }`).join('\n        ')}
        
        // 부모 창으로부터 데이터 수신
        window.addEventListener('message', (event) => {
            if (event.data.type === 'APP_STATE') {
                props = event.data;
                updateUI();
            }
        });
        
        // UI 업데이트 함수
        function updateUI() {
            // Data binding 처리
            document.querySelectorAll('[data-bind]').forEach(element => {
                const bindKey = element.getAttribute('data-bind');
                if (state[bindKey] !== undefined) {
                    element.textContent = state[bindKey];
                } else if (props[bindKey] !== undefined) {
                    if (typeof props[bindKey] === 'object' && props[bindKey].name) {
                        element.textContent = props[bindKey].name;
                    } else {
                        element.textContent = props[bindKey];
                    }
                }
            });
            
            // Expression 처리
            document.querySelectorAll('[data-expr]').forEach(element => {
                const expr = element.getAttribute('data-expr');
                try {
                    // 간단한 표현식 평가 (보안상 제한적으로)
                    if (expr.includes('user') && props.user) {
                        if (expr === 'user.name') {
                            element.textContent = props.user.name || '';
                        } else if (expr === 'user.email') {
                            element.textContent = props.user.email || '';
                        }
                    }
                } catch (e) {
                    element.textContent = expr;
                }
            });
        }
        
        // 페이지 네비게이션 함수
        function navigateTo(page, options = {}) {
            if (window.parent !== window) {
                window.parent.postMessage({
                    type: 'NAVIGATE',
                    page: page,
                    options: options
                }, '*');
            } else {
                let url = page.includes('.html') ? page : \`../\${page}.html\`;
                if (options.orderId) url += \`?orderId=\${options.orderId}\`;
                if (options.userType) url += \`\${options.orderId ? '&' : '?'}userType=\${options.userType}\`;
                window.location.href = url;
            }
        }
        
        // 초기화
        document.addEventListener('DOMContentLoaded', () => {
            updateUI();
            
            // 클릭 이벤트 위임
            document.addEventListener('click', (e) => {
                const target = e.target.closest('[onclick]');
                if (target) {
                    const onclick = target.getAttribute('onclick');
                    try {
                        eval(onclick);
                    } catch (err) {
                        console.error('Click handler error:', err);
                    }
                }
            });
        });
    </script>
</body>
</html>`;
    }
    
    // 함수 본문을 JavaScript로 변환
    convertFunctionBodyToJS(body) {
        let jsBody = body;
        
        // React 관련 코드를 vanilla JS로 변환
        jsBody = jsBody.replace(/setSearchTerm\(([^)]+)\)/g, 'state.searchTerm = $1; updateUI()');
        jsBody = jsBody.replace(/onNavigate\(([^)]+)\)/g, 'navigateTo($1)');
        
        // 간단한 변환만 수행, 복잡한 로직은 그대로 유지
        return jsBody;
    }
    
    // CSS 생성
    generateCSS() {
        return `
        /* Component 기본 스타일 */
        body {
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f8f9fa;
        }
        
        #root {
            padding: 20px;
            max-width: 1200px;
            margin: 0 auto;
        }
        
        /* Card 스타일 */
        .card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .card-header {
            padding: 20px 20px 0 20px;
        }
        
        .card-title {
            font-size: 18px;
            font-weight: 600;
            margin: 0 0 10px 0;
            color: #333;
        }
        
        .card-content {
            padding: 20px;
        }
        
        /* Button 스타일 */
        .btn {
            padding: 8px 16px;
            border-radius: 6px;
            border: 1px solid #ddd;
            background: white;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s;
        }
        
        .btn:hover {
            background: #f5f5f5;
            border-color: #999;
        }
        
        /* Badge 스타일 */
        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
            background: #e3f2fd;
            color: #1976d2;
        }
        
        /* Form 스타일 */
        .form-input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        
        .form-input:focus {
            outline: none;
            border-color: #2196f3;
        }
        
        /* Table 스타일 */
        .table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        
        .table-header {
            background: #f5f5f5;
        }
        
        .table-head, .table-cell {
            padding: 12px 8px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        
        .table-head {
            font-weight: 600;
            color: #666;
        }
        
        /* Separator */
        .separator {
            border: none;
            height: 1px;
            background: #eee;
            margin: 16px 0;
        }
        
        /* Icon */
        .icon {
            display: inline-block;
            width: 16px;
            height: 16px;
            text-align: center;
        }
        
        /* Alert */
        .alert {
            padding: 12px 16px;
            border-radius: 6px;
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            margin-bottom: 16px;
        }
        
        .alert-description {
            margin: 0;
        }
        
        /* Conditional rendering */
        .conditional[data-condition] {
            /* JavaScript로 표시/숨김 제어 */
        }
        
        /* List container */
        .list-container {
            /* 동적 리스트가 렌더링될 공간 */
        }
        
        /* Utility classes */
        .flex { display: flex; }
        .items-center { align-items: center; }
        .justify-between { justify-content: space-between; }
        .gap-2 { gap: 8px; }
        .gap-4 { gap: 16px; }
        .mb-4 { margin-bottom: 16px; }
        .mt-4 { margin-top: 16px; }
        .p-4 { padding: 16px; }
        .text-sm { font-size: 14px; }
        .font-medium { font-weight: 500; }
        .text-gray-600 { color: #666; }
        `;
    }
    
    // 오류 발생 시 기본 HTML 생성
    generateErrorHTML(componentName, errorMessage) {
        return `<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${componentName} - 변환 오류</title>
</head>
<body>
    <div style="padding: 20px; text-align: center;">
        <h1>⚠️ 변환 오류</h1>
        <p>컴포넌트: ${componentName}</p>
        <p>오류: ${errorMessage}</p>
        <button onclick="history.back()">뒤로 가기</button>
    </div>
</body>
</html>`;
    }
    
    // 단일 파일 변환
    convertFile(tsxFile) {
        console.log(`Converting ${path.basename(tsxFile)}...`);
        
        const tsxContent = fs.readFileSync(tsxFile, 'utf-8');
        const componentName = path.basename(tsxFile, '.tsx');
        
        const htmlContent = this.convertTSXToHTML(tsxContent, componentName);
        
        const outputPath = this.determineOutputPath(componentName);
        
        // 디렉토리 생성
        const outputDir = path.dirname(outputPath);
        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }
        
        fs.writeFileSync(outputPath, htmlContent, 'utf-8');
        
        console.log(`  -> Saved to ${outputPath}`);
        return outputPath;
    }
    
    // 출력 경로 결정
    determineOutputPath(componentName) {
        const nameLower = componentName.toLowerCase();
        let folder;
        
        if (nameLower.includes('admin')) {
            folder = 'pages/admin';
        } else if (nameLower.includes('partner')) {
            folder = 'pages/partner';
        } else if (nameLower.includes('order') || nameLower.includes('payment')) {
            folder = 'pages/order';
        } else if (['navigation', 'footer'].some(x => nameLower.includes(x))) {
            folder = 'components';
        } else {
            folder = 'pages/common';
        }
        
        let fileName = componentName.replace(/([A-Z])/g, '-$1').toLowerCase();
        fileName = fileName.replace('page', '').replace('--', '-').replace(/^-|-$/g, '');
        
        return path.join(this.outputDir, folder, `${fileName}.html`);
    }
    
    // 모든 파일 변환
    convertAll() {
        const componentsDir = path.join(this.sourceDir, 'components');
        const tsxFiles = fs.readdirSync(componentsDir)
            .filter(file => file.endsWith('.tsx'))
            .map(file => path.join(componentsDir, file));
        
        console.log(`Found ${tsxFiles.length} TSX files to convert`);
        console.log('='.repeat(60));
        
        const convertedFiles = [];
        for (const tsxFile of tsxFiles) {
            try {
                const outputPath = this.convertFile(tsxFile);
                convertedFiles.push(outputPath);
            } catch (error) {
                console.log(`  ERROR: ${error.message}`);
            }
        }
        
        console.log('='.repeat(60));
        console.log(`Successfully converted ${convertedFiles.length}/${tsxFiles.length} files`);
        
        return convertedFiles;
    }
}

// 실행
if (require.main === module) {
    const sourceDir = 'C:\\YCS-ver2\\jjycs';
    const outputDir = 'C:\\YCS-ver2\\jjycs\\html';
    
    const converter = new AdvancedTSXToHTMLConverter(sourceDir, outputDir);
    converter.convertAll();
    
    console.log('\n✅ TSX → HTML 변환 완료!');
}