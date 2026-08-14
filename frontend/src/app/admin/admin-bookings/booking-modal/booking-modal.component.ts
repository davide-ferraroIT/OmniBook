import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { BookingResponse, BookingUpdateRequest, ServiceResponse } from '../../../core/models/models';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-booking-modal',
  templateUrl: './booking-modal.component.html',
  styleUrls: ['./booking-modal.component.scss'],
  standalone: false
})
export class BookingModalComponent implements OnInit {

  @Input() tenantId!: string;
  @Input() booking!: BookingResponse;

  services: ServiceResponse[] = [];
  isLoading = true;

  // Dati form
  serviceId: string = '';
  resourceId: string | null = null;
  startTime: string = ''; // formato ISO
  customerName: string = '';
  customerEmail: string = '';
  customerPhone: string = '';

  constructor(
    private modalCtrl: ModalController,
    private apiService: ApiService
  ) { }

  ngOnInit() {
    this.serviceId = this.booking.service?.id;
    this.resourceId = this.booking.resource?.id || null;
    this.startTime = this.booking.startTime;
    this.customerName = this.booking.customerName;
    this.customerEmail = this.booking.customerEmail;
    this.customerPhone = this.booking.customerPhone || '';

    this.loadServices();
  }

  loadServices() {
    this.apiService.getServicesByTenantId(this.tenantId).subscribe({
      next: (res: any) => {
        this.services = res.content || res;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  get selectedService(): ServiceResponse | undefined {
    return this.services.find(s => s.id === this.serviceId);
  }

  cancel() {
    this.modalCtrl.dismiss(null, 'cancel');
  }

  save() {
    if (!this.serviceId || !this.startTime || !this.customerName || !this.customerEmail) {
      return; // Aggiungere validazione UI in HTML (required)
    }

    const request: BookingUpdateRequest = {
      serviceId: this.serviceId,
      resourceId: this.resourceId || undefined,
      startTime: this.startTime,
      customerName: this.customerName,
      customerEmail: this.customerEmail,
      customerPhone: this.customerPhone
    };

    this.modalCtrl.dismiss(request, 'confirm');
  }
}
