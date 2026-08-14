import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  standalone: false
})
export class LoginPage implements OnInit {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
  }

  async onLogin() {
    this.error = '';
    this.loading = true;
    try {
      this.authService.login(this.email, this.password).subscribe({
        next: () => {
          this.router.navigate(['/home']);
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
