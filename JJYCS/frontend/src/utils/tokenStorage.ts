interface TokenStorage {
  getToken(): string | null
  setToken(token: string): void
  getRefreshToken(): string | null
  setRefreshToken(token: string): void
  removeTokens(): void
}

class LocalStorageTokenStorage implements TokenStorage {
  getToken(): string | null {
    return localStorage.getItem('auth_token')
  }

  setToken(token: string): void {
    localStorage.setItem('auth_token', token)
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token')
  }

  setRefreshToken(token: string): void {
    localStorage.setItem('refresh_token', token)
  }

  removeTokens(): void {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('refresh_token')
  }
}

class CookieTokenStorage implements TokenStorage {
  private getCookie(name: string): string | null {
    const value = `; ${document.cookie}`
    const parts = value.split(`; ${name}=`)
    if (parts.length === 2) {
      return parts.pop()?.split(';').shift() || null
    }
    return null
  }

  private setCookie(name: string, value: string, options: { secure?: boolean; sameSite?: string; httpOnly?: boolean } = {}): void {
    let cookieString = `${name}=${value}; path=/`
    
    if (options.secure) {
      cookieString += '; secure'
    }
    
    if (options.sameSite) {
      cookieString += `; samesite=${options.sameSite}`
    }
    
    // Note: httpOnly cannot be set from JavaScript
    // It should be set by the server when sending the cookie
    document.cookie = cookieString
  }

  private deleteCookie(name: string): void {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
  }

  getToken(): string | null {
    // In production with HTTP-only cookies, tokens are handled automatically by browser
    // This method mainly serves as a check for token existence
    return this.getCookie('auth_token')
  }

  setToken(token: string): void {
    // In production, this should be set by server with HTTP-only flag
    // This is a fallback for client-side token management
    this.setCookie('auth_token', token, { 
      secure: location.protocol === 'https:', 
      sameSite: 'Lax' 
    })
  }

  getRefreshToken(): string | null {
    return this.getCookie('refresh_token')
  }

  setRefreshToken(token: string): void {
    this.setCookie('refresh_token', token, { 
      secure: location.protocol === 'https:', 
      sameSite: 'Lax' 
    })
  }

  removeTokens(): void {
    this.deleteCookie('auth_token')
    this.deleteCookie('refresh_token')
  }
}

// Factory function to get appropriate storage based on environment
export function createTokenStorage(): TokenStorage {
  const isProduction = import.meta.env.PROD
  
  if (isProduction) {
    return new CookieTokenStorage()
  } else {
    return new LocalStorageTokenStorage()
  }
}

export const tokenStorage = createTokenStorage()