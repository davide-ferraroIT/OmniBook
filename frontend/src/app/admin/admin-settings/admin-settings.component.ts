import { Component, Input, OnInit, OnChanges, SimpleChanges, OnDestroy, inject } from '@angular/core';
import { TenantResponse } from '../../core/models/models';
import { ApiService } from '../../core/services/api.service';
import { ToastController, AlertController } from '@ionic/angular';

@Component({
  selector: 'app-admin-settings',
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.scss'],
  standalone: false
})
export class AdminSettingsComponent implements OnInit, OnChanges, OnDestroy {
  private apiService = inject(ApiService);
  private toastCtrl = inject(ToastController);
  private alertCtrl = inject(AlertController);

  @Input() tenant!: TenantResponse;
  
  autoAcceptBookings: boolean = false;
  inviteCode: string = '';
  isSaving: boolean = false;
  isSavingInviteCode: boolean = false;

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
    if (this.tenant) {
      this.inviteCode = this.tenant.inviteCode || '';
      if (this.tenant.config) {
        this.autoAcceptBookings = this.tenant.config.autoAcceptBookings || false;
      }
    }
  }

  ngOnDestroy() {
    // Salva le modifiche in background quando il componente viene distrutto (es. cambio tab)
    this.saveSettings();
  }

  async saveInviteCode() {
    if (!this.tenant || !this.inviteCode || this.inviteCode === this.tenant.inviteCode) {
      return;
    }

    this.isSavingInviteCode = true;
    this.apiService.updateTenantInviteCode(this.tenant.id, this.inviteCode).subscribe({
      next: async (res) => {
        this.tenant = res;
        this.isSavingInviteCode = false;
        const toast = await this.toastCtrl.create({
          message: 'Codice invito aggiornato con successo.',
          duration: 2000,
          color: 'success'
        });
        toast.present();
      },
      error: async (err) => {
        this.isSavingInviteCode = false;
        this.inviteCode = this.tenant.inviteCode || ''; // revert
        const toast = await this.toastCtrl.create({
          message: 'Errore: ' + (err.error?.message || 'Codice già in uso?'),
          duration: 3000,
          color: 'danger'
        });
        toast.present();
      }
    });
  }

  async saveSettings() {
    if (this.tenant && this.tenant.config && this.tenant.config.autoAcceptBookings === this.autoAcceptBookings) {
      return; // Nessuna modifica, inutile chiamare il backend
    }

    this.isSaving = true;
    const updatedConfig = {
      ...this.tenant.config,
      autoAcceptBookings: this.autoAcceptBookings
    };

    this.apiService.updateTenantConfig(this.tenant.id, updatedConfig).subscribe({
      next: async (res) => {
        this.tenant = res;
        this.isSaving = false;
      },
      error: async (err) => {
        this.isSaving = false;
        console.error(err);
        // Mostriamo l'errore se fallisce, ma in background
        const toast = await this.toastCtrl.create({
          message: 'Errore durante il salvataggio in background delle impostazioni.',
          duration: 3000,
          color: 'danger'
        });
        toast.present();
      }
    });
  }
}
