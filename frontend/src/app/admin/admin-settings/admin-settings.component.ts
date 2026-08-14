import { Component, Input, OnInit, OnChanges, SimpleChanges, inject } from '@angular/core';
import { TenantResponse } from '../../core/models/models';
import { ApiService } from '../../core/services/api.service';
import { ToastController, AlertController } from '@ionic/angular';

@Component({
  selector: 'app-admin-settings',
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.scss'],
  standalone: false
})
export class AdminSettingsComponent implements OnInit, OnChanges {
  private apiService = inject(ApiService);
  private toastCtrl = inject(ToastController);
  private alertCtrl = inject(AlertController);

  @Input() tenant!: TenantResponse;
  
  autoAcceptBookings: boolean = false;
  isSaving: boolean = false;


  constructor() {}

  ngOnInit() {
    this.updateLocalState();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['tenant'] && !changes['tenant'].firstChange) {
      this.updateLocalState();
    }
  }

  private updateLocalState() {
    if (this.tenant && this.tenant.config) {
      this.autoAcceptBookings = this.tenant.config.autoAcceptBookings || false;
    }
  }

  async saveSettings() {
    this.isSaving = true;
    const updatedConfig = {
      ...this.tenant.config,
      autoAcceptBookings: this.autoAcceptBookings
    };

    this.apiService.updateTenantConfig(this.tenant.id, updatedConfig).subscribe({
      next: async (res) => {
        this.tenant = res;
        this.isSaving = false;
        const toast = await this.toastCtrl.create({
          message: 'Impostazioni salvate con successo',
          duration: 2000,
          color: 'success'
        });
        toast.present();
      },
      error: async (err) => {
        this.isSaving = false;
        console.error(err);
        const alert = await this.alertCtrl.create({
          header: 'Errore',
          message: 'Si è verificato un errore durante il salvataggio delle impostazioni.',
          buttons: ['OK']
        });
        alert.present();
      }
    });
  }
}
