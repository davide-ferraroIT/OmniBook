import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.page.html',
  standalone: false
})
export class RegisterPage implements OnInit {
  firstName = '';
  lastName = '';
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
  }

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
