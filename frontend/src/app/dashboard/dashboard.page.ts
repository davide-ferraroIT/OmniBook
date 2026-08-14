import { Component } from '@angular/core';
import { AuthService } from '../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: 'dashboard.page.html',
  styleUrls: ['dashboard.page.scss'],
  standalone: false,
})
export class DashboardPage {

  constructor(private authService: AuthService, private router: Router) {}

  goToRegister() {
    // Se è autenticato, il NoAuthGuard lo bloccherebbe.
    // Effettuiamo il logout forzato se l'utente vuole andare a registrarsi da qui
    if (this.authService.hasToken()) {
      this.authService.logout();
    }
    this.router.navigate(['/register']);
  }

}
