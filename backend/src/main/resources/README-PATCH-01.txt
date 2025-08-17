Patch Pack 01 - Backend YAML fixes
----------------------------------
1) app.jwt.* 필드 누락 보완 (issuer, clock-skew, secret 등)
2) dev 프로파일의 data-locations를 실제 존재하는 data-h2.sql로 수정
3) mybatis.mapper-locations 경로를 실제 리소스 폴더명(mapper)과 일치
4) local 프로파일에서 sql.init.platform=h2 설정 추가 (data-h2.sql 자동 인식)

적용 방법:
- 아래 PowerShell 또는 Bash 스크립트를 사용하거나,
- 각 파일을 동일 경로에 그대로 덮어쓰기 하세요.
