import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { ApiService } from '../../core/services/api.service';
import { TenantResponse, TenantConfig, DaySchedule } from '../../core/models/models';

@Component({
  selector: 'app-admin-business-hours',
  templateUrl: './admin-business-hours.component.html',
  styleUrls: ['./admin-business-hours.component.scss'],
  standalone: false
})
export class AdminBusinessHoursComponent implements OnInit {

  slug: string = '';
  tenant!: TenantResponse;
  isLoading = true;
  isSaving = false;
  error: string | null = null;

  // Lista di default dei giorni (inglese per il backend Java enum)
  readonly DAYS_OF_WEEK = [
    { value: 'MONDAY', label: 'Lunedì' },
    { value: 'TUESDAY', label: 'Martedì' },
    { value: 'WEDNESDAY', label: 'Mercoledì' },
    { value: 'THURSDAY', label: 'Giovedì' },
    { value: 'FRIDAY', label: 'Venerdì' },
    { value: 'SATURDAY', label: 'Sabato' },
    { value: 'SUNDAY', label: 'Domenica' }
  ];

  businessHours: DaySchedule[] = [];

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private toastCtrl: ToastController
  ) { }

  ngOnInit() {
    this.slug = this.route.snapshot.paramMap.get('slug') || '';
    if (this.slug) {
      this.loadTenant();
    }
  }

  loadTenant() {
    this.isLoading = true;
    this.error = null;
    this.apiService.getTenantBySlug(this.slug).subscribe({
      next: (res) => {
        this.tenant = res;
        this.initBusinessHours();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Impossibile caricare i dati del negozio.';
        this.isLoading = false;
      }
    });
  }

  initBusinessHours() {
    const configHours = this.tenant.config?.businessHours || [];
    
    // Mappa per accesso veloce
    const existingHoursMap = new Map<string, DaySchedule>();
    for (const h of configHours) {
      existingHoursMap.set(h.dayOfWeek, h);
    }

    // Costruiamo sempre l'array di 7 giorni nello stesso ordine
    this.businessHours = this.DAYS_OF_WEEK.map(day => {
      if (existingHoursMap.has(day.value)) {
        const existing = existingHoursMap.get(day.value)!;
        return {
          ...existing,
          // Convertiamo i tempi (es: "09:00:00" o "09:00") in formato stringa per ion-datetime ("1970-01-01T09:00:00")
          openTime: this.formatTimeForIonic(existing.openTime),
          closeTime: this.formatTimeForIonic(existing.closeTime)
        };
      } else {
        // Default (es. chiuso, 09:00-18:00)
        return {
          dayOfWeek: day.value,
          isOpen: false,
          openTime: '1970-01-01T09:00:00',
          closeTime: '1970-01-01T18:00:00'
        };
      }
    });
  }

  getLabelForDay(dayOfWeek: string): string {
    const day = this.DAYS_OF_WEEK.find(d => d.value === dayOfWeek);
    return day ? day.label : dayOfWeek;
  }

  formatTimeForIonic(timeStr: string): string {
    if (!timeStr) return '1970-01-01T09:00:00';
    // Se è già un ISO completo
    if (timeStr.includes('T')) return timeStr;
    // Se è "HH:mm:ss" o "HH:mm"
    const parts = timeStr.split(':');
    let hh = parts[0].padStart(2, '0');
    let mm = parts[1] ? parts[1].padStart(2, '0') : '00';
    return `1970-01-01T${hh}:${mm}:00`;
  }

  formatTimeForBackend(ionicDateStr: string): string {
    if (!ionicDateStr) return '00:00:00';
    // Estrai HH:mm:00
    const date = new Date(ionicDateStr);
    const hh = date.getHours().toString().padStart(2, '0');
    const mm = date.getMinutes().toString().padStart(2, '0');
    return `${hh}:${mm}:00`;
  }

  async save() {
    this.isSaving = true;

    // Prepariamo i dati per il backend
    const updatedBusinessHours: DaySchedule[] = this.businessHours.map(h => ({
      ...h,
      openTime: this.formatTimeForBackend(h.openTime),
      closeTime: this.formatTimeForBackend(h.closeTime)
    }));

    const updatedConfig: TenantConfig = {
      ...this.tenant.config,
      businessHours: updatedBusinessHours
    };

    this.apiService.updateTenantConfig(this.tenant.id, updatedConfig).subscribe({
      next: async (res) => {
        this.tenant = res;
        this.isSaving = false;
        
        const toast = await this.toastCtrl.create({
          message: 'Orari di apertura salvati con successo!',
          duration: 2000,
          color: 'success'
        });
        toast.present();
      },
      error: async (err) => {
        console.error(err);
        this.isSaving = false;
        
        const toast = await this.toastCtrl.create({
          message: 'Errore durante il salvataggio degli orari.',
          duration: 2000,
          color: 'danger'
        });
        toast.present();
      }
    });
  }
}
