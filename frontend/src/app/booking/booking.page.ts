import { Component, OnInit, ViewChild, inject, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { TenantResponse, ServiceResponse, ResourceResponse, BookingCreateRequest } from '../core/models/models';
import { IonModal, AlertController, ActionSheetController, IonicSafeString } from '@ionic/angular';

interface GridColumn {
  date: string;
  dayLabel: string;
  resource: ResourceResponse;
  slots: string[];
}

@Component({
  selector: 'app-booking',
  templateUrl: './booking.page.html',
  styleUrls: ['./booking.page.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class BookingPage implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private authService = inject(AuthService);
  private alertController = inject(AlertController);
  private actionSheetController = inject(ActionSheetController);
  private router = inject(Router);

  @ViewChild('serviceModal') serviceModal!: IonModal;
  @ViewChild('loginModal') loginModal!: IonModal;

  tenant: TenantResponse | null = null;
  services: ServiceResponse[] = [];
  
  // UI State
  isLoading = true;
  isBooking = false;
  bookingSuccess = false;
  activeTab = 'home';

  // Selections
  selectedService: ServiceResponse | null = null;
  isServiceModalOpen = false;

  ionViewDidEnter() {
    this.isServiceModalOpen = !this.bookingSuccess;
  }

  ionViewWillLeave() {
    this.isServiceModalOpen = false;
    if (this.serviceModal) {
      this.serviceModal.dismiss().catch(() => {});
    }
  }
  
  // Custom Table Data
  gridColumns: GridColumn[] = [];
  currentEndDate: Date | null = null;
  isLoadingMore = false;

  // Login State
  isLoggedIn = false;
  username = '';
  password = '';


  userEmail: string | null = null;


  constructor() { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.loadTenantAndServices(slug);
      }
    });

    this.authService.isAuthenticated().subscribe(isAuth => {
      this.isLoggedIn = isAuth;
      if (isAuth) {
        this.userEmail = this.authService.getUserEmail();
      } else {
        this.userEmail = null;
      }
    });
  }

  loadTenantAndServices(slug: string) {
    this.apiService.getTenantBySlug(slug).subscribe({
      next: (tenant) => {
        this.tenant = tenant;
        this.applyTheme(tenant.config.primaryColor);
        
        this.apiService.getServicesByTenantId(tenant.id).subscribe({
          next: (servicesData) => {
            this.services = servicesData.content || servicesData;
            if (this.services.length > 0) {
              this.selectedService = this.services[0];
              this.loadGridData();
            }
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Error loading services', err);
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error loading tenant', err);
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

  selectService(service: ServiceResponse) {
    this.selectedService = service;
    this.loadGridData();
  }

  getCloudinaryOptimizedUrl(url?: string): string {
    if (!url) return 'assets/logo-placeholder.png';
    // Se è un URL di Cloudinary, aggiungiamo i parametri di trasformazione
    if (url.includes('res.cloudinary.com')) {
      return url.replace('/upload/', '/upload/w_100,h_100,c_fill,r_max,f_auto/');
    }
    return url;
  }

  loadGridData() {
    this.gridColumns = [];
    if (!this.selectedService || !this.tenant) return;

    // Calcolo dinamico: 1 colonna è circa 140px (128px + 12px gap)
    const containerWidth = window.innerWidth;
    const visibleDays = Math.ceil(containerWidth / 140);
    const weeksToFill = Math.ceil(visibleDays / 7);
    const totalWeeksToFetch = weeksToFill + 1; // Riempe lo schermo + 1 settimana di buffer
    const daysToFetch = totalWeeksToFetch * 7;

    const today = new Date();
    this.currentEndDate = new Date(today);
    this.currentEndDate.setDate(today.getDate() + daysToFetch - 1); // -1 perché include 'today'

    const startDateStr = today.toISOString().split('T')[0];
    const endDateStr = this.currentEndDate.toISOString().split('T')[0];

    this.apiService.getAvailabilityRange(this.tenant.id, this.selectedService.id, startDateStr, endDateStr)
      .subscribe({
        next: (data) => {
          // The backend returns an array of DayResourceAvailability
          // We map it to GridColumn
          this.gridColumns = data.map(item => ({
            date: item.date,
            dayLabel: item.dayLabel,
            resource: item.resource,
            slots: item.availableSlots.map((s: string) => s.substring(0, 5))
          }));
        },
        error: (err) => {
          console.error('Errore nel caricamento della disponibilità', err);
        }
      });
  }

  loadMoreData() {
    if (!this.selectedService || !this.tenant || !this.currentEndDate || this.isLoadingMore) return;

    this.isLoadingMore = true;
    const nextStartDate = new Date(this.currentEndDate);
    nextStartDate.setDate(nextStartDate.getDate() + 1); // Start from the day after

    const nextEndDate = new Date(nextStartDate);
    nextEndDate.setDate(nextStartDate.getDate() + 6); // Fetch next 7 days

    const startDateStr = nextStartDate.toISOString().split('T')[0];
    const endDateStr = nextEndDate.toISOString().split('T')[0];

    this.apiService.getAvailabilityRange(this.tenant.id, this.selectedService.id, startDateStr, endDateStr)
      .subscribe({
        next: (data) => {
          const newColumns = data.map(item => ({
            date: item.date,
            dayLabel: item.dayLabel,
            resource: item.resource,
            slots: item.availableSlots.map((s: string) => s.substring(0, 5))
          }));
          
          this.gridColumns = [...this.gridColumns, ...newColumns];
          this.currentEndDate = nextEndDate;
          this.isLoadingMore = false;
        },
        error: (err) => {
          console.error('Errore nel caricamento di altre disponibilità', err);
          this.isLoadingMore = false;
        }
      });
  }

  onScroll(event: any) {
    const target = event.target;
    // Se mancano 100px alla fine dello scroll orizzontale, carica altri giorni
    if (target.scrollLeft + target.clientWidth >= target.scrollWidth - 100) {
      this.loadMoreData();
    }
  }

  async selectSlot(col: GridColumn, slot: string) {
    const alert = await this.alertController.create({
      header: 'Conferma Prenotazione',
      message: new IonicSafeString(`Vuoi prenotare <strong>${this.selectedService?.name}</strong> con <strong>${col.resource.name}</strong> per il <strong>${col.dayLabel}</strong> alle <strong>${slot}</strong>?`),
      buttons: [
        {
          text: 'Annulla',
          role: 'cancel',
          cssClass: 'secondary'
        }, {
          text: 'Conferma',
          handler: () => {
            this.confirmBooking(col, slot);
          }
        }
      ]
    });

    await alert.present();
  }

  confirmBooking(col: GridColumn, slot: string) {
    if (!this.tenant || !this.selectedService) return;

    this.isBooking = true;
    
    let email = this.authService.getUserEmail() || 'ospite@omnibook.app';
    
    // Rimuoviamo eventuali spazi e formattiamo
    email = email.replace(/\s+/g, '').toLowerCase();

    const name = email !== 'ospite@omnibook.app' ? email.split('@')[0] : 'Ospite';

    const request = {
      serviceId: this.selectedService.id,
      resourceId: col.resource.id,
      startTime: `${col.date}T${slot}:00`, // e.g. "2026-08-13T09:00:00"
      customerName: name,
      customerEmail: email
    };

    this.apiService.createBooking(this.tenant.id, request).subscribe({
      next: (response) => {
        this.isBooking = false;
        this.bookingSuccess = true;
        this.isServiceModalOpen = false;
      },
      error: async (err) => {
        console.error("Errore durante la creazione della prenotazione", err);
        this.isBooking = false;
        
        let errorMsg = "Si è verificato un errore durante la prenotazione.";
        if (err.error) {
            if (err.error.detail) {
                errorMsg = err.error.detail;
            } else if (err.error.message) {
                errorMsg = err.error.message;
            } else if (typeof err.error === 'string') {
                errorMsg = err.error;
            }
        } else if (err.message) {
            errorMsg = err.message;
        }

        const alert = await this.alertController.create({
          header: 'Prenotazione Fallita',
          message: errorMsg,
          buttons: ['OK']
        });
        await alert.present();
      }
    });
  }

  resetBooking() {
    this.bookingSuccess = false;
    this.isServiceModalOpen = true;
  }

  async handleProfileClick() {
    if (this.isLoggedIn) {
      if (this.serviceModal) {
        await this.serviceModal.dismiss().catch(() => {});
      }
      this.router.navigate(['/profile']);
    } else {
      this.username = '';
      this.password = '';
      this.loginModal.present();
    }
  }

  goToRegister() {
    this.loginModal.dismiss();
    this.router.navigate(['/register']);
  }

  submitAuth() {
    const action = this.authService.login(this.username, this.password);

    action.subscribe({
      next: () => {
        this.loginModal.dismiss();
      },
      error: async (err) => {
        console.error('Auth error', err);
        const alert = await this.alertController.create({
          header: 'Errore',
          message: 'Autenticazione fallita. Controlla le tue credenziali.',
          buttons: ['OK']
        });
        await alert.present();
      }
    });
  }

  setTab(tab: string) {
    this.activeTab = tab;
  }
}
