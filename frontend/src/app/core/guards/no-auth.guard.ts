import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NoAuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);


  constructor() {}

  canActivate(): boolean | UrlTree {
    if (!this.authService.hasToken()) {
      return true;
    }
    
    // Già autenticato, redirect alla home
    return this.router.parseUrl('/home');
  }
}
