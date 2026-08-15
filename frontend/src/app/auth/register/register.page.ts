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

  role: 'CUSTOMER' | 'SHOP' = 'CUSTOMER';

  // Dati comuni
  firstName = '';
  lastName = '';
  email = '';
  password = '';

  // Dati Cliente
  phone = '';
  inviteCode = '';

  // Dati Negoziante
  tenantName = '';
  tenantSlug = '';

  error = '';
  loading = false;

  constructor() { }

  validatePhone(event: any) {
    const value = event.target.value;
    // Rimuove tutti i caratteri che non sono numeri o il prefisso +
    const cleaned = value.replace(/[^0-9+]/g, '');
    event.target.value = cleaned;
    this.phone = cleaned;
  }

  isFormValid(): boolean {
    if (!this.firstName || !this.lastName || !this.email || !this.password || this.password.length < 6) {
      return false;
    }

    if (this.role === 'CUSTOMER') {
      if (!this.phone || !this.inviteCode) return false;
    } else {
      if (!this.tenantName || !this.tenantSlug) return false;
    }

    return true;
  }

  async onRegister() {
    this.error = '';
    this.loading = true;
    try {
      if (this.role === 'CUSTOMER') {
        this.authService.registerCustomer(this.firstName, this.lastName, this.email, this.phone, this.password, this.inviteCode).subscribe({
          next: () => {
            const redirectUrl = this.authService.getRoleRedirectUrl();
            this.router.navigateByUrl(redirectUrl);
          },
          error: (err) => {
            this.error = 'Registrazione fallita. ' + (err.error?.message || 'Controlla i dati e il codice invito.');
            this.loading = false;
          }
        });
      } else {
        // SHOP
        const tenantDetails = {
          name: this.tenantName,
          slug: this.tenantSlug,
          config: { theme: 'light' }
        };
        this.authService.registerTenant(this.firstName, this.lastName, this.email, this.password, tenantDetails).subscribe({
          next: () => {
            const redirectUrl = this.authService.getRoleRedirectUrl();
            this.router.navigateByUrl(redirectUrl);
          },
          error: (err) => {
            this.error = 'Registrazione fallita. ' + (err.error?.message || 'Email o slug già in uso?');
            this.loading = false;
          }
        });
      }
    } catch (e) {
      this.error = 'Errore di rete';
      this.loading = false;
    }
  }
}

