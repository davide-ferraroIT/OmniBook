import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    const expectedRoles = route.data['roles'] as Array<string>;
    const userRole = this.authService.getUserRole();

    if (!this.authService.hasToken() || !userRole) {
      return this.router.parseUrl('/login');
    }

    // Se non sono stati specificati ruoli o se l'utente ha uno dei ruoli attesi
    if (!expectedRoles || expectedRoles.includes(userRole)) {
      return true;
    }

    // Se l'utente non ha il ruolo corretto, rimandalo alla home pubblica
    return this.router.parseUrl('/home');
  }
}
