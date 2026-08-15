import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuthResponse {
  token: string;
  userId: string;
  role: string;
  tenantId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);


  private apiUrl = environment.apiUrl;
  private tokenKey = 'auth_token';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());


  constructor() { }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, { email, password })
      .pipe(
        tap(response => {
          if (response && response.token) {
            this.setToken(response.token);
          }
        })
      );
  }

  registerCustomer(firstName: string, lastName: string, email: string, phone: string, password: string, inviteCode: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register-customer`, { firstName, lastName, email, phone, password, inviteCode })
      .pipe(
        tap(response => {
          if (response && response.token) {
            this.setToken(response.token);
          }
        })
      );
  }

  registerTenant(firstName: string, lastName: string, email: string, password: string, tenantDetails: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register-tenant`, { firstName, lastName, email, password, tenantDetails })
      .pipe(
        tap(response => {
          if (response && response.token) {
            this.setToken(response.token);
          }
        })
      );
  }

  private router = inject(Router);

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    this.isAuthenticatedSubject.next(true);
  }

  hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  getUserEmail(): string | null {
    return this.getClaim('sub');
  }

  getUserRole(): string | null {
    return this.getClaim('role');
  }

  getUserTenantId(): string | null {
    return this.getClaim('tenantId');
  }

  getUserTenantSlug(): string | null {
    return this.getClaim('tenantSlug');
  }

  getRoleRedirectUrl(): string {
    const role = this.getUserRole();
    const slug = this.getUserTenantSlug();

    if (role === 'ADMIN') {
      return '/dashboard';
    }
    if (role === 'SHOP' && slug) {
      return `/shop/${slug}`;
    }
    if (role === 'CUSTOMER' && slug) {
      return `/booking/${slug}`;
    }
    if (role === 'CUSTOMER') {
      return '/profile';
    }
    
    // Fallback generico per evitare loop infiniti nel NoAuthGuard
    return '/profile';
  }

  private getClaim(claimKey: string): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const payload = JSON.parse(jsonPayload);
      return payload[claimKey] || null;
    } catch (e) {
      return null;
    }
  }

  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }
}
