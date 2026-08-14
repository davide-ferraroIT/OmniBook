import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.page.html',
  standalone: false
})
export class RegisterPage {
  private authService = inject(AuthService);
  private router = inject(Router);

  firstName = '';
  lastName = '';
  email = '';
  password = '';
  error = '';
  loading = false;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() { }


  async onRegister() {
    this.error = '';
    this.loading = true;
    try {
      // Per default registriamo un TENANT_ADMIN per i nuovi iscritti dal form principale
      this.authService.register(this.firstName, this.lastName, this.email, this.password, 'TENANT_ADMIN').subscribe({
        next: () => {
          this.router.navigate(['/home']);
        },
        error: (err) => {
          this.error = 'Registrazione fallita. ' + (err.error?.message || 'Email già in uso?');
          this.loading = false;
        }
      });
    } catch (e) {
      this.error = 'Errore di rete';
      this.loading = false;
    }
  }
}
