import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModalController, AlertController } from '@ionic/angular';
import { ApiService } from '../../core/services/api.service';
import { ResourceResponse } from '../../core/models/models';
import { ResourceModalComponent } from './resource-modal/resource-modal.component';

@Component({
  selector: 'app-admin-resources',
  templateUrl: './admin-resources.component.html',
  styleUrls: ['./admin-resources.component.scss'],
  standalone: false
})
export class AdminResourcesComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private modalCtrl = inject(ModalController);
  private alertCtrl = inject(AlertController);


  slug: string = '';
  tenantId: string = '';
  resources: ResourceResponse[] = [];
  isLoading: boolean = true;
  primaryColor: string = '#000000';

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() { }

  ngOnInit() {
    this.slug = this.route.snapshot.paramMap.get('slug') || this.route.parent?.snapshot.paramMap.get('slug') || '';
    if (this.slug) {
      this.loadTenantAndResources();
    } else {
      console.error('Slug non fornito nella route');
      this.isLoading = false;
    }
  }

  loadTenantAndResources() {
    this.apiService.getTenantBySlug(this.slug).subscribe({
      next: (tenant: any) => {
        this.tenantId = tenant.id;
        this.primaryColor = tenant.config.primaryColor || '#000000';
        this.applyTheme(this.primaryColor);
        this.loadResources();
      },
      error: (err: any) => {
        console.error('Errore recupero tenant', err);
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

  loadResources() {
    this.isLoading = true;
    this.apiService.getResourcesByTenantId(this.tenantId).subscribe({
      next: (res: any) => {
        this.resources = res.content || res;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Errore recupero risorse', err);
        this.isLoading = false;
      }
    });
  }

  async openResourceModal(resource?: ResourceResponse) {
    const modal = await this.modalCtrl.create({
      component: ResourceModalComponent,
      componentProps: {
        resource: resource,
        tenantId: this.tenantId
      }
    });

    modal.onDidDismiss().then((data) => {
      if (data.data?.success) {
        this.loadResources();
      }
    });

    await modal.present();
  }

  async deleteResource(resource: ResourceResponse) {
    const alert = await this.alertCtrl.create({
      header: 'Conferma eliminazione',
      message: `Sei sicuro di voler eliminare la risorsa "${resource.name}"?`,
      buttons: [
        { text: 'Annulla', role: 'cancel' },
        {
          text: 'Elimina',
          role: 'destructive',
          handler: () => {
            this.apiService.deleteResource(this.tenantId, resource.id).subscribe({
              next: () => {
                this.loadResources();
              },
              error: (err: any) => {
                console.error('Errore eliminazione', err);
              }
            });
          }
        }
      ]
    });
    await alert.present();
  }
}
