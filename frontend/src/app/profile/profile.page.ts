import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class ProfilePage implements OnInit {

  private apiService = inject(ApiService);
  private authService = inject(AuthService);
  private router = inject(Router);

  profile: any = null;
  loading = true;
  saving = false;
  isEditMode = false;

  // Form fields
  editFirstName = '';
  editLastName = '';
  editEmail = '';
  editPhone = '';

  constructor() { }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.apiService.getUserProfile().subscribe({
      next: (res: any) => {
        this.profile = res;
        this.resetForm();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Errore nel caricamento profilo', err);
        this.loading = false;
      }
    });
  }

  resetForm() {
    if (this.profile) {
      this.editFirstName = this.profile.firstName || '';
      this.editLastName = this.profile.lastName || '';
      this.editEmail = this.profile.email || '';
      this.editPhone = this.profile.phone || '';
    }
  }

  toggleEditMode() {
    this.isEditMode = !this.isEditMode;
    if (!this.isEditMode) {
      this.resetForm(); // Annulla modifiche se si esce dalla modalità edit
    }
  }

  saveProfile() {
    this.saving = true;
    const updateData = {
      firstName: this.editFirstName,
      lastName: this.editLastName,
      email: this.editEmail,
      phone: this.editPhone
    };

    this.apiService.updateUserProfile(updateData).subscribe({
      next: (res: any) => {
        this.profile = res;
        this.isEditMode = false;
        this.saving = false;
      },
      error: (err: any) => {
        console.error('Errore durante salvataggio profilo', err);
        this.saving = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
