#!/usr/bin/env python3
"""
TSX to HTML Converter
TSX 파일을 HTML로 변환하는 스크립트
"""

import os
import re
import json
from pathlib import Path

class TSXToHTMLConverter:
    def __init__(self, source_dir, output_dir):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir)
        
    def convert_tsx_to_html(self, tsx_content, component_name):
        """TSX 컨텐츠를 HTML로 변환"""
        
        # HTML 템플릿
        html_template = """<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <link rel="stylesheet" href="../../css/globals.css">
    <style>
        {styles}
    </style>
</head>
<body>
    <div id="root">
        {content}
    </div>
    
    <script src="../../js/utils.js"></script>
    <script>
        // 컴포넌트 초기화
        let state = {state_init};
        let props = {{}};
        
        // 부모 창으로부터 데이터 수신
        window.addEventListener('message', (event) => {{
            if (event.data.type === 'APP_STATE') {{
                props.user = event.data.user;
                props.orderId = event.data.orderId;
                props.userType = event.data.userType;
                initComponent();
            }}
        }});
        
        // 페이지 네비게이션
        function navigateTo(page, options = {{}}) {{
            window.parent.postMessage({{
                type: 'NAVIGATE',
                page: page,
                options: options
            }}, '*');
        }}
        
        // 로그인 처리
        function handleLogin(user) {{
            window.parent.postMessage({{
                type: 'LOGIN',
                user: user
            }}, '*');
        }}
        
        // 로그아웃 처리
        function handleLogout() {{
            window.parent.postMessage({{
                type: 'LOGOUT'
            }}, '*');
        }}
        
        // 컴포넌트 초기화 함수
        function initComponent() {{
            {init_script}
        }}
        
        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', () => {{
            initComponent();
        }});
        
        {component_script}
    </script>
</body>
</html>"""
        
        # 컴포넌트 이름 추출
        title = self.extract_component_title(component_name)
        
        # JSX 내용 추출 및 변환
        content = self.extract_jsx_content(tsx_content)
        content = self.convert_jsx_to_html(content)
        
        # 스타일 추출
        styles = self.extract_styles(tsx_content)
        
        # State 초기화 코드 생성
        state_init = self.extract_state_init(tsx_content)
        
        # 초기화 스크립트 생성
        init_script = self.generate_init_script(tsx_content)
        
        # 컴포넌트 스크립트 생성
        component_script = self.generate_component_script(tsx_content)
        
        # HTML 생성
        html = html_template.format(
            title=title,
            styles=styles,
            content=content,
            state_init=state_init,
            init_script=init_script,
            component_script=component_script
        )
        
        return html
    
    def extract_component_title(self, component_name):
        """컴포넌트 이름에서 타이틀 추출"""
        # CamelCase를 공백으로 분리
        title = re.sub(r'([A-Z])', r' \1', component_name).strip()
        title = title.replace('Page', '').replace('Component', '').strip()
        return f"{title} - YCS 물류 시스템"
    
    def extract_jsx_content(self, tsx_content):
        """TSX에서 JSX 내용 추출"""
        # return 문 찾기
        match = re.search(r'return\s*\(([\s\S]*?)\);', tsx_content)
        if not match:
            match = re.search(r'return\s*<([\s\S]*?)>;', tsx_content)
        
        if match:
            return match.group(1) if match.group(1) else f"<{match.group(1)}>"
        return "<div>컨텐츠를 불러올 수 없습니다.</div>"
    
    def convert_jsx_to_html(self, jsx_content):
        """JSX를 HTML로 변환"""
        html = jsx_content
        
        # className -> class
        html = re.sub(r'className=', 'class=', html)
        
        # onClick -> onclick
        html = re.sub(r'onClick=\{([^}]*)\}', r'onclick="\1()"', html)
        html = re.sub(r'onChange=\{([^}]*)\}', r'onchange="\1(event)"', html)
        html = re.sub(r'onSubmit=\{([^}]*)\}', r'onsubmit="\1(event)"', html)
        
        # {변수} -> <span id="변수"></span>
        html = re.sub(r'\{([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)*)\}', 
                     r'<span id="data-\1"></span>', html)
        
        # {조건 && 내용} 처리
        html = re.sub(r'\{([^}]*)\s*&&\s*\(([\s\S]*?)\)\}', 
                     r'<div class="conditional" data-condition="\1">\2</div>', html)
        
        # map 함수 처리
        html = re.sub(r'\{([^}]*?)\.map\([^)]*\)\s*=>\s*\(([\s\S]*?)\)\)}',
                     r'<div class="list-container" data-source="\1"></div>', html)
        
        # 자체 닫는 태그 변환
        html = re.sub(r'<([^>]+)\/>', r'<\1></\1>', html)
        
        return html
    
    def extract_styles(self, tsx_content):
        """TSX에서 스타일 추출"""
        styles = []
        
        # Tailwind 클래스를 실제 CSS로 변환 (간단한 예시)
        tailwind_map = {
            'flex': 'display: flex;',
            'flex-col': 'flex-direction: column;',
            'items-center': 'align-items: center;',
            'justify-center': 'justify-content: center;',
            'justify-between': 'justify-content: space-between;',
            'p-4': 'padding: 16px;',
            'p-6': 'padding: 24px;',
            'mb-4': 'margin-bottom: 16px;',
            'mb-6': 'margin-bottom: 24px;',
            'text-center': 'text-align: center;',
            'font-bold': 'font-weight: bold;',
            'text-lg': 'font-size: 18px;',
            'text-xl': 'font-size: 20px;',
            'text-2xl': 'font-size: 24px;',
            'bg-white': 'background-color: white;',
            'bg-blue-500': 'background-color: rgb(59 130 246);',
            'text-white': 'color: white;',
            'text-blue-600': 'color: rgb(37 99 235);',
            'rounded-lg': 'border-radius: 8px;',
            'shadow-lg': 'box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);',
            'min-h-screen': 'min-height: 100vh;',
        }
        
        # 기본 스타일
        default_styles = """
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
        """
        
        return default_styles
    
    def extract_state_init(self, tsx_content):
        """State 초기화 코드 추출"""
        state_vars = {}
        
        # useState 패턴 찾기
        pattern = r'const\s*\[(\w+),\s*set\w+\]\s*=\s*useState\((.*?)\);'
        matches = re.findall(pattern, tsx_content)
        
        for var_name, initial_value in matches:
            # 초기값 정리
            if initial_value == '':
                initial_value = '""'
            elif initial_value == 'null':
                initial_value = 'null'
            elif initial_value == 'false' or initial_value == 'true':
                initial_value = initial_value
            elif initial_value.startswith('['):
                initial_value = initial_value
            elif initial_value.startswith('{'):
                initial_value = initial_value
            else:
                initial_value = f'"{initial_value}"'
            
            state_vars[var_name] = initial_value
        
        return json.dumps(state_vars, ensure_ascii=False)
    
    def generate_init_script(self, tsx_content):
        """초기화 스크립트 생성"""
        init_script = """
        // 데이터 바인딩
        function updateUI() {
            // State 값을 DOM에 반영
            for (let key in state) {
                const elements = document.querySelectorAll(`[id="data-${key}"]`);
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
                const elements = document.querySelectorAll(`[id="prop-${key}"]`);
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
        """
        
        return init_script
    
    def generate_component_script(self, tsx_content):
        """컴포넌트별 스크립트 생성"""
        scripts = []
        
        # 함수 정의 찾기
        pattern = r'const\s+(\w+)\s*=\s*\([^)]*\)\s*=>\s*{([^}]*)}'
        matches = re.findall(pattern, tsx_content)
        
        for func_name, func_body in matches:
            # React 함수를 일반 JavaScript 함수로 변환
            js_func = f"""
        function {func_name}(event) {{
            if (event) event.preventDefault();
            {func_body}
        }}"""
            scripts.append(js_func)
        
        return '\n'.join(scripts)
    
    def convert_file(self, tsx_file):
        """단일 TSX 파일 변환"""
        print(f"Converting {tsx_file.name}...")
        
        # TSX 내용 읽기
        with open(tsx_file, 'r', encoding='utf-8') as f:
            tsx_content = f.read()
        
        # 컴포넌트 이름 추출
        component_name = tsx_file.stem
        
        # HTML로 변환
        html_content = self.convert_tsx_to_html(tsx_content, component_name)
        
        # 출력 경로 결정
        output_path = self.determine_output_path(component_name)
        
        # 디렉토리 생성
        output_path.parent.mkdir(parents=True, exist_ok=True)
        
        # HTML 파일 저장
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        print(f"  -> Saved to {output_path}")
        return output_path
    
    def determine_output_path(self, component_name):
        """컴포넌트 이름에 따라 출력 경로 결정"""
        name_lower = component_name.lower()
        
        # 경로 매핑
        if 'admin' in name_lower:
            folder = 'pages/admin'
        elif 'partner' in name_lower:
            folder = 'pages/partner'
        elif 'order' in name_lower or 'payment' in name_lower:
            folder = 'pages/order'
        elif any(x in name_lower for x in ['login', 'signup', 'find', 'dashboard', 'mypage', 'faq', 'notice', 'workflow', 'pagelist']):
            folder = 'pages/common'
        elif any(x in name_lower for x in ['navigation', 'footer']):
            folder = 'components'
        else:
            folder = 'pages/common'
        
        # 파일명 변환 (CamelCase -> kebab-case)
        file_name = re.sub(r'([A-Z])', r'-\1', component_name).lower().strip('-')
        file_name = file_name.replace('page', '').replace('--', '-').strip('-')
        
        return self.output_dir / folder / f"{file_name}.html"
    
    def convert_all(self):
        """모든 TSX 파일 변환"""
        # 변환할 파일 목록
        tsx_files = []
        
        # components 폴더의 TSX 파일 찾기 (ui 폴더 제외)
        for tsx_file in self.source_dir.glob('components/*.tsx'):
            if tsx_file.is_file():
                tsx_files.append(tsx_file)
        
        # App.tsx는 제외 (이미 index.html로 변환됨)
        tsx_files = [f for f in tsx_files if f.name != 'App.tsx']
        
        print(f"Found {len(tsx_files)} TSX files to convert")
        print("=" * 50)
        
        converted_files = []
        for tsx_file in tsx_files:
            try:
                output_path = self.convert_file(tsx_file)
                converted_files.append(output_path)
            except Exception as e:
                print(f"  ERROR: {e}")
        
        print("=" * 50)
        print(f"Successfully converted {len(converted_files)} files")
        
        return converted_files


if __name__ == "__main__":
    # 실행 경로 설정
    source_dir = r"C:\YCS-ver2\jjycs"
    output_dir = r"C:\YCS-ver2\jjycs\html"
    
    # 변환기 생성 및 실행
    converter = TSXToHTMLConverter(source_dir, output_dir)
    converter.convert_all()
    
    print("\n변환 완료!")