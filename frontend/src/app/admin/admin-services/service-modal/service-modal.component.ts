import { Component, Input, OnInit } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { ApiService } from '../../../core/services/api.service';
import { ServiceResponse, ServiceCreateRequest, ResourceResponse } from '../../../core/models/models';

@Component({
  selector: 'app-service-modal',
  templateUrl: './service-modal.component.html',
  styleUrls: ['./service-modal.component.scss'],
  standalone: false
})
export class ServiceModalComponent implements OnInit {

  @Input() service?: ServiceResponse;
  @Input() tenantId!: string;

  name: string = '';
  // Utilizziamo una stringa ISO per l'ion-datetime
  durationTime: string = '1970-01-01T00:30:00'; 
  
  resources: ResourceResponse[] = [];
  selectedResourceIds: string[] = [];

  isSaving: boolean = false;

  constructor(
    private modalCtrl: ModalController,
    private apiService: ApiService,
    private toastCtrl: ToastController
  ) { }

  ngOnInit() {
    this.loadResources();

    if (this.service) {
      this.name = this.service.name;
      // Convertire i minuti del servizio in un formato accettabile per ion-datetime (HH:mm)
      const hours = Math.floor(this.service.durationMinutes / 60);
      const minutes = this.service.durationMinutes % 60;
      const hh = hours.toString().padStart(2, '0');
      const mm = minutes.toString().padStart(2, '0');
      this.durationTime = `1970-01-01T${hh}:${mm}:00`;

      if (this.service.allowedResources) {
        this.selectedResourceIds = this.service.allowedResources.map(r => r.id);
      }
    }
  }

  loadResources() {
    this.apiService.getResourcesByTenantId(this.tenantId).subscribe({
      next: (resources) => {
        // Handle pagination response or raw array
        this.resources = (resources as any).content || resources;
      },
      error: (err) => {
        console.error('Errore nel caricamento delle risorse', err);
      }
    });
  }

  cancel() {
    return this.modalCtrl.dismiss(null, 'cancel');
  }

  async save() {
    if (!this.name.trim()) {
      return;
    }
    
    this.isSaving = true;

    // Calcoliamo i minuti totali selezionati dall'ion-datetime
    const dateObj = new Date(this.durationTime);
    const durationMinutes = (dateObj.getHours() * 60) + dateObj.getMinutes();

    if (durationMinutes === 0) {
      this.isSaving = false;
      const toast = await this.toastCtrl.create({
        message: 'La durata deve essere maggiore di 0',
        duration: 2000,
        color: 'warning'
      });
      toast.present();
      return;
    }

    if (this.service) {
      // Modifica
      const req: ServiceCreateRequest = {
        name: this.name,
        durationMinutes: durationMinutes,
        allowedResourceIds: this.selectedResourceIds
      };
      
      this.apiService.updateService(this.tenantId, this.service.id, req).subscribe({
        next: async (res) => {
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Servizio aggiornato',
            duration: 2000,
            color: 'success'
          });
          toast.present();
          this.modalCtrl.dismiss({ success: true }, 'confirm');
        },
        error: async (err) => {
          console.error(err);
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Errore durante l\'aggiornamento del servizio',
            duration: 2000,
            color: 'danger'
          });
          toast.present();
        }
      });
    } else {
      // Creazione
      const req: ServiceCreateRequest = {
        name: this.name,
        durationMinutes: durationMinutes,
        allowedResourceIds: this.selectedResourceIds
      };

      this.apiService.createService(this.tenantId, req).subscribe({
        next: async (res) => {
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Servizio creato',
            duration: 2000,
            color: 'success'
          });
          toast.present();
          this.modalCtrl.dismiss({ success: true }, 'confirm');
        },
        error: async (err) => {
          console.error(err);
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Errore durante la creazione del servizio',
            duration: 2000,
            color: 'danger'
          });
          toast.present();
        }
      });
    }
  }
}
