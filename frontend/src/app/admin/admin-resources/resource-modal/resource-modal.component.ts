import { Component, Input, OnInit, inject } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { ApiService } from '../../../core/services/api.service';
import { ResourceResponse, ResourceCreateRequest } from '../../../core/models/models';

@Component({
  selector: 'app-resource-modal',
  templateUrl: './resource-modal.component.html',
  styleUrls: ['./resource-modal.component.scss'],
  standalone: false
})
export class ResourceModalComponent implements OnInit {
  private modalCtrl = inject(ModalController);
  private apiService = inject(ApiService);
  private toastCtrl = inject(ToastController);


  @Input() resource?: ResourceResponse;
  @Input() tenantId!: string;

  name: string = '';
  capacity: number = 1;
  imageUrl?: string;

  isSaving: boolean = false;
  isUploadingImage: boolean = false;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() { }

  ngOnInit() {
    if (this.resource) {
      this.name = this.resource.name;
      this.capacity = this.resource.capacity;
      this.imageUrl = this.resource.imageUrl;
    }
  }

  cancel() {
    return this.modalCtrl.dismiss(null, 'cancel');
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.uploadToCloudinary(file);
    }
  }

  uploadToCloudinary(file: File) {
    this.isUploadingImage = true;
    this.apiService.uploadResourceImage(this.tenantId, file).subscribe({
      next: (response: {imageUrl: string}) => {
        this.imageUrl = response.imageUrl;
        this.isUploadingImage = false;
      },
      error: async (err: any) => {
        console.error('Upload error', err);
        this.isUploadingImage = false;
        const toast = await this.toastCtrl.create({
          message: 'Errore durante il caricamento dell\'immagine.',
          duration: 3000,
          color: 'danger'
        });
        toast.present();
      }
    });
  }

  removeImage() {
    this.imageUrl = undefined;
    const fileInput = document.getElementById('resourceFileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  async save() {
    if (!this.name.trim() || this.capacity < 1) {
      return;
    }
    
    this.isSaving = true;

    const req: ResourceCreateRequest = {
      name: this.name,
      capacity: this.capacity,
      imageUrl: this.imageUrl
    };

    if (this.resource) {
      // Update
      this.apiService.updateResource(this.tenantId, this.resource.id, req).subscribe({
        next: async () => {
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Risorsa aggiornata',
            duration: 2000,
            color: 'success'
          });
          toast.present();
          this.modalCtrl.dismiss({ success: true }, 'confirm');
        },
        error: async (err: any) => {
          console.error(err);
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Errore durante l\'aggiornamento',
            duration: 2000,
            color: 'danger'
          });
          toast.present();
        }
      });
    } else {
      // Create
      this.apiService.createResource(this.tenantId, req).subscribe({
        next: async () => {
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Risorsa creata',
            duration: 2000,
            color: 'success'
          });
          toast.present();
          this.modalCtrl.dismiss({ success: true }, 'confirm');
        },
        error: async (err: any) => {
          console.error(err);
          this.isSaving = false;
          const toast = await this.toastCtrl.create({
            message: 'Errore durante la creazione',
            duration: 2000,
            color: 'danger'
          });
          toast.present();
        }
      });
    }
  }
}
