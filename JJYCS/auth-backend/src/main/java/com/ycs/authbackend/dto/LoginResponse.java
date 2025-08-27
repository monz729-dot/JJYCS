package com.ycs.authbackend.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String name;
    private String userType;
    private String memberCode;
    
    public LoginResponse() {}
    
    public LoginResponse(String token, String email, String name, String userType, String memberCode) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.memberCode = memberCode;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
}