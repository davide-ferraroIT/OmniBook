import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../core/services/api.service';
import { TenantResponse, ServiceResponse, ResourceResponse, BookingCreateRequest } from '../core/models/models';

@Component({
  selector: 'app-booking',
  templateUrl: './booking.page.html',
  styleUrls: ['./booking.page.scss'],
  standalone: false
})
export class BookingPage implements OnInit {

  tenant: TenantResponse | null = null;
  services: ServiceResponse[] = [];
  
  // Wizard state
  currentStep = 1;
  
  // Selections
  selectedService: ServiceResponse | null = null;
  selectedResource: ResourceResponse | null = null;
  selectedDate: string = new Date().toISOString().split('T')[0];
  availableSlots: string[] = [];
  selectedSlot: string = '';
  
  // Customer Data
  customerName = '';
  customerEmail = '';
  customerPhone = '';
  
  // UI State
  isLoading = true;
  isBooking = false;
  bookingSuccess = false;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService
  ) { }

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.loadTenantAndServices(slug);
    }
  }

  loadTenantAndServices(slug: string) {
    this.apiService.getTenantBySlug(slug).subscribe({
      next: (tenant) => {
        this.tenant = tenant;
        this.applyTheme(tenant.config.primaryColor);
        
        this.apiService.getServicesByTenantId(tenant.id).subscribe({
          next: (servicesData) => {
            // Se l'API restituisce una pageable
            this.services = servicesData.content || servicesData;
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
    
    // Converte HEX in RGB per Tailwind e Ionic
    let r = parseInt(hexColor.slice(1, 3), 16),
        g = parseInt(hexColor.slice(3, 5), 16),
        b = parseInt(hexColor.slice(5, 7), 16);
        
    document.documentElement.style.setProperty('--ion-color-primary', hexColor);
    document.documentElement.style.setProperty('--ion-color-primary-rgb', `${r},${g},${b}`);
    
    // Utile se si usa Tailwind color opacity
    document.documentElement.style.setProperty('--color-brand', `${r} ${g} ${b}`);
  }

  selectService(service: ServiceResponse) {
    this.selectedService = service;
    this.selectedResource = null;
    this.selectedSlot = '';
    
    if (this.tenant?.config.allowAutoAssignment) {
      this.currentStep = 3; // Salta selezione risorsa
      this.loadAvailability();
    } else {
      this.currentStep = 2; // Scegli risorsa
    }
  }

  selectResource(resource: ResourceResponse) {
    this.selectedResource = resource;
    this.currentStep = 3;
    this.loadAvailability();
  }

  onDateChange(event: any) {
    this.selectedDate = event.detail.value.split('T')[0];
    this.loadAvailability();
  }

  loadAvailability() {
    if (!this.tenant || !this.selectedService) return;
    
    this.apiService.getAvailability(
      this.tenant.id, 
      this.selectedService.id, 
      this.selectedDate,
      this.selectedResource?.id
    ).subscribe({
      next: (slots) => {
        this.availableSlots = slots;
      },
      error: (err) => {
        console.error('Error loading availability', err);
        this.availableSlots = [];
      }
    });
  }

  selectSlot(slot: string) {
    this.selectedSlot = slot;
    this.currentStep = 4;
  }

  confirmBooking() {
    if (!this.tenant || !this.selectedService || !this.selectedSlot) return;
    
    this.isBooking = true;
    
    const request: BookingCreateRequest = {
      serviceId: this.selectedService.id,
      resourceId: this.selectedResource?.id,
      startTime: `${this.selectedDate}T${this.selectedSlot}`,
      customerName: this.customerName,
      customerEmail: this.customerEmail,
      customerPhone: this.customerPhone
    };
    
    this.apiService.createBooking(this.tenant.id, request).subscribe({
      next: (response) => {
        this.isBooking = false;
        this.bookingSuccess = true;
      },
      error: (err) => {
        this.isBooking = false;
        alert('Errore durante la prenotazione: ' + (err.error?.message || 'Riprova.'));
      }
    });
  }

  goBack() {
    if (this.currentStep > 1) {
      if (this.currentStep === 3 && this.tenant?.config.allowAutoAssignment) {
        this.currentStep = 1;
      } else {
        this.currentStep--;
      }
    }
  }

  get isValidEmail(): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(this.customerEmail);
  }
}
