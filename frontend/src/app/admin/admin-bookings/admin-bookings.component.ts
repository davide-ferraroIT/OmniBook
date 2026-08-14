import { Component, Input, OnInit, ViewChild, inject } from '@angular/core';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { BookingResponse, TenantResponse } from '../../core/models/models';
import { ApiService } from '../../core/services/api.service';
import { ToastController, AlertController, ModalController } from '@ionic/angular';
import { BookingModalComponent } from './booking-modal/booking-modal.component';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import itLocale from '@fullcalendar/core/locales/it';
import { ActionSheetController } from '@ionic/angular';

@Component({
  selector: 'app-admin-bookings',
  templateUrl: './admin-bookings.component.html',
  styleUrls: ['./admin-bookings.component.scss'],
  standalone: false
})
export class AdminBookingsComponent implements OnInit {
  private apiService = inject(ApiService);
  private toastCtrl = inject(ToastController);
  private alertCtrl = inject(AlertController);
  private modalCtrl = inject(ModalController);
  private actionSheetCtrl = inject(ActionSheetController);

  @Input() tenant!: TenantResponse;
  @ViewChild('calendar') calendarComponent!: FullCalendarComponent;
  
  bookings: BookingResponse[] = [];
  isLoading: boolean = true;
  
  // Touch swipe variables
  touchStartX = 0;
  touchStartY = 0;
  touchEndX = 0;
  touchEndY = 0;


  constructor() {}

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    locales: [itLocale],
    locale: 'it',
    events: [],
    eventClick: this.handleEventClick.bind(this),
    height: 'auto',
    allDaySlot: false
  };

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.isLoading = true;
    this.apiService.getBookingsByTenantId(this.tenant.id).subscribe({
      next: (res) => {
        // Backend returns Page<BookingResponse>, so bookings are in res.content
        this.bookings = res.content || [];
        this.calendarOptions.events = this.bookings.map(b => ({
          id: b.id,
          title: `${b.customerName} - ${b.service.name}`,
          start: b.startTime,
          backgroundColor: this.getEventColorHex(b.status),
          borderColor: this.getEventColorHex(b.status),
          extendedProps: { booking: b }
        }));
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
        
        // Ricarica per aggiornare il colore nel calendario
        this.loadBookings();
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

  async editBooking(booking: BookingResponse) {
    const modal = await this.modalCtrl.create({
      component: BookingModalComponent,
      componentProps: {
        tenantId: this.tenant.id,
        booking: booking
      }
    });

    modal.onDidDismiss().then((result) => {
      if (result.role === 'confirm' && result.data) {
        this.apiService.updateBooking(this.tenant.id, booking.id, result.data).subscribe({
          next: async (res) => {
            // Aggiorna la prenotazione nella lista corrente
            const index = this.bookings.findIndex(b => b.id === booking.id);
            if (index > -1) {
              this.bookings[index] = res;
            }
            const toast = await this.toastCtrl.create({
              message: 'Prenotazione modificata con successo.',
              duration: 2000,
              color: 'success'
            });
            toast.present();
            
            // Ricarica i dati per aggiornare il calendario
            this.loadBookings();
          },
          error: async (err) => {
            const alert = await this.alertCtrl.create({
              header: 'Errore',
              message: 'Impossibile modificare la prenotazione.',
              buttons: ['OK']
            });
            alert.present();
          }
        });
      }
    });

    await modal.present();
  }
  
  getEventColorHex(status: string): string {
    switch(status) {
      case 'PENDING': return '#ffc409'; // warning
      case 'CONFIRMED': return '#2dd36f'; // success
      case 'CANCELED': return '#eb445a'; // danger
      case 'COMPLETED': return '#3880ff'; // primary
      default: return '#92949c'; // medium
    }
  }

  handleEventClick(clickInfo: EventClickArg) {
    const booking = clickInfo.event.extendedProps['booking'] as BookingResponse;
    if (booking) {
      this.presentBookingActions(booking);
    }
  }

  async presentBookingActions(booking: BookingResponse) {
    const buttons = [];
    if (booking.status === 'PENDING') {
      buttons.push({ text: 'Accetta', handler: () => { this.updateStatus(booking, 'CONFIRMED'); } });
      buttons.push({ text: 'Rifiuta', role: 'destructive', handler: () => { this.updateStatus(booking, 'CANCELED'); } });
    } else if (booking.status === 'CONFIRMED') {
      buttons.push({ text: 'Segna Completata', handler: () => { this.updateStatus(booking, 'COMPLETED'); } });
      buttons.push({ text: 'Annulla', role: 'destructive', handler: () => { this.updateStatus(booking, 'CANCELED'); } });
    }
    
    buttons.push({ text: 'Dettagli / Modifica', icon: 'create-outline', handler: () => { this.editBooking(booking); } });
    buttons.push({ text: 'Chiudi', role: 'cancel' });

    const actionSheet = await this.actionSheetCtrl.create({
      header: `Prenotazione: ${booking.customerName}`,
      subHeader: `${booking.service.name} - ${this.getLabelForStatus(booking.status)}`,
      buttons: buttons
    });

    await actionSheet.present();
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

  // Swipe handling
  onTouchStart(e: TouchEvent) {
    this.touchStartX = e.changedTouches[0].screenX;
    this.touchStartY = e.changedTouches[0].screenY;
  }

  onTouchEnd(e: TouchEvent) {
    this.touchEndX = e.changedTouches[0].screenX;
    this.touchEndY = e.changedTouches[0].screenY;
    this.handleSwipe();
  }

  handleSwipe() {
    const swipeDistanceX = this.touchEndX - this.touchStartX;
    const swipeDistanceY = this.touchEndY - this.touchStartY;
    
    // Verifica che lo swipe sia orizzontale e non verticale
    if (Math.abs(swipeDistanceX) > Math.abs(swipeDistanceY)) {
      const swipeThreshold = 50; // pixel
      if (swipeDistanceX < -swipeThreshold) {
        // Swiped left (next)
        if (this.calendarComponent) this.calendarComponent.getApi().next();
      } else if (swipeDistanceX > swipeThreshold) {
        // Swiped right (prev)
        if (this.calendarComponent) this.calendarComponent.getApi().prev();
      }
    }
  }
}
