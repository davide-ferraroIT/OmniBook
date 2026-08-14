import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TenantResponse, ServiceResponse } from '../../core/models/models';
import { ModalController, ToastController, AlertController } from '@ionic/angular';
import { ServiceModalComponent } from './service-modal/service-modal.component';

@Component({
  selector: 'app-admin-services',
  templateUrl: './admin-services.component.html',
  styleUrls: ['./admin-services.component.scss'],
  standalone: false
})
export class AdminServicesComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private modalCtrl = inject(ModalController);
  private toastCtrl = inject(ToastController);
  private alertCtrl = inject(AlertController);


  tenant: TenantResponse | null = null;
  slug: string | null = null;
  services: ServiceResponse[] = [];
  isLoading: boolean = true;
  error: string | null = null;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() { }

  ngOnInit() {
    // Il parametro 'slug' potrebbe essere nel parent (AdminDashboard) o nella route corrente,
    // dato che il path è definito come 'services' sotto '/admin/:slug'.
    this.slug = this.route.snapshot.paramMap.get('slug') || this.route.parent?.snapshot.paramMap.get('slug') || null;
    if (this.slug) {
      this.loadTenantAndServices();
    } else {
      this.error = 'Slug non fornito';
      this.isLoading = false;
    }
  }

  loadTenantAndServices() {
    this.apiService.getTenantBySlug(this.slug!).subscribe({
      next: (tenant) => {
        this.tenant = tenant;
        if (tenant.config?.primaryColor) {
          this.applyTheme(tenant.config.primaryColor);
        }
        this.loadServices();
      },
      error: (err) => {
        console.error(err);
        this.error = 'Impossibile caricare i dati del tenant';
        this.isLoading = false;
      }
    });
  }

  applyTheme(hexColor: string) {
    if (!hexColor) return;
    let r = parseInt(hexColor.slice(1, 3), 16),
        g = parseInt(hexColor.slice(3, 5), 16),
        b = parseInt(hexColor.slice(5, 7), 16);
        
    document.documentElement.style.setProperty('--ion-color-primary', hexColor);
    document.documentElement.style.setProperty('--ion-color-primary-rgb', `${r},${g},${b}`);
    document.documentElement.style.setProperty('--color-brand', `${r} ${g} ${b}`);
  }

  loadServices() {
    this.isLoading = true;
    this.apiService.getServicesByTenantId(this.tenant!.id).subscribe({
      next: (pageData) => {
        // Supponendo che restituisca un Page<ServiceResponse> in base al backend
        this.services = pageData.content || pageData;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Impossibile caricare i servizi';
        this.isLoading = false;
      }
    });
  }

  async openServiceModal(service?: ServiceResponse) {
    const modal = await this.modalCtrl.create({
      component: ServiceModalComponent,
      componentProps: {
        service: service,
        tenantId: this.tenant?.id
      }
    });

    modal.onDidDismiss().then((result) => {
      if (result.data && result.data.success) {
        this.loadServices();
      }
    });

    return await modal.present();
  }

  async deleteService(service: ServiceResponse) {
    const alert = await this.alertCtrl.create({
      header: 'Conferma',
      message: `Vuoi davvero eliminare il servizio "${service.name}"?`,
      buttons: [
        {
          text: 'Annulla',
          role: 'cancel'
        },
        {
          text: 'Elimina',
          role: 'destructive',
          handler: () => {
            this.apiService.deleteService(this.tenant!.id, service.id).subscribe({
              next: async () => {
                const toast = await this.toastCtrl.create({
                  message: 'Servizio eliminato',
                  duration: 2000,
                  color: 'success'
                });
                toast.present();
                this.loadServices();
              },
              error: async (err) => {
                console.error(err);
                const errorAlert = await this.alertCtrl.create({
                  header: 'Errore',
                  message: 'Impossibile eliminare il servizio',
                  buttons: ['OK']
                });
                errorAlert.present();
              }
            });
          }
        }
      ]
    });

    await alert.present();
  }
}
