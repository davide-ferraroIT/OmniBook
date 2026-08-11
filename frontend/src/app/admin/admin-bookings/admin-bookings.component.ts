import { Component, Input, OnInit } from '@angular/core';
import { BookingResponse, TenantResponse } from '../../core/models/models';
import { ApiService } from '../../core/services/api.service';
import { ToastController, AlertController } from '@ionic/angular';

@Component({
  selector: 'app-admin-bookings',
  templateUrl: './admin-bookings.component.html',
  styleUrls: ['./admin-bookings.component.scss'],
  standalone: false
})
export class AdminBookingsComponent implements OnInit {
  @Input() tenant!: TenantResponse;
  
  bookings: BookingResponse[] = [];
  isLoading: boolean = true;

  constructor(
    private apiService: ApiService,
    private toastCtrl: ToastController,
    private alertCtrl: AlertController
  ) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.isLoading = true;
    this.apiService.getBookingsByTenantId(this.tenant.id).subscribe({
      next: (res) => {
        // Backend returns Page<BookingResponse>, so bookings are in res.content
        this.bookings = res.content || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  async updateStatus(booking: BookingResponse, status: string) {
    this.apiService.updateBookingStatus(this.tenant.id, booking.id, status).subscribe({
      next: async (res) => {
        booking.status = res.status;
        const toast = await this.toastCtrl.create({
          message: `Stato aggiornato a ${status}`,
          duration: 2000,
          color: 'success'
        });
        toast.present();
      },
      error: async (err) => {
        const alert = await this.alertCtrl.create({
          header: 'Errore',
          message: 'Impossibile aggiornare lo stato.',
          buttons: ['OK']
        });
        alert.present();
      }
    });
  }
  
  getColorForStatus(status: string): string {
    switch(status) {
      case 'PENDING': return 'warning';
      case 'CONFIRMED': return 'success';
      case 'CANCELED': return 'danger';
      case 'COMPLETED': return 'primary';
      default: return 'medium';
    }
  }

  getLabelForStatus(status: string): string {
    switch(status) {
      case 'PENDING': return 'In attesa';
      case 'CONFIRMED': return 'Confermata';
      case 'CANCELED': return 'Annullata';
      case 'COMPLETED': return 'Completata';
      default: return status;
    }
  }
}
