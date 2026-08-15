import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  standalone: false
})
export class LoginPage {
  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  error = '';
  loading = false;


  constructor() { }


  async onLogin() {
    this.error = '';
    this.loading = true;
    try {
      this.authService.login(this.email, this.password).subscribe({
        next: () => {
          const redirectUrl = this.authService.getRoleRedirectUrl();
          this.router.navigateByUrl(redirectUrl);
        },
        error: (err) => {
          this.error = 'Login fallito. Controlla le credenziali.';
          this.loading = false;
        }
      });
    } catch (e) {
      this.error = 'Errore di rete';
      this.loading = false;
    }
  }
}
