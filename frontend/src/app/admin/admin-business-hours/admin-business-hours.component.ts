import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { ApiService } from '../../core/services/api.service';
import { TenantResponse, TenantConfig, DaySchedule, Holiday, TimeSlot } from '../../core/models/models';

@Component({
  selector: 'app-admin-business-hours',
  templateUrl: './admin-business-hours.component.html',
  styleUrls: ['./admin-business-hours.component.scss'],
  standalone: false
})
export class AdminBusinessHoursComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private toastCtrl = inject(ToastController);


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
  holidays: Holiday[] = [];

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() { }

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
        if (res.config?.primaryColor) {
          this.applyTheme(res.config.primaryColor);
        }
        this.initBusinessHours();
        this.initHolidays();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Impossibile caricare i dati del negozio.';
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
          timeSlots: existing.timeSlots?.map(ts => ({
            startTime: this.formatTimeForIonic(ts.startTime),
            endTime: this.formatTimeForIonic(ts.endTime)
          })) || []
        };
      } else {
        // Default (es. chiuso, senza fasce)
        return {
          dayOfWeek: day.value,
          isOpen: false,
          timeSlots: []
        };
      }
    });
  }

  initHolidays() {
    const configHolidays = this.tenant.config?.holidays || [];
    this.holidays = configHolidays.map(h => ({ ...h }));
  }

  onDayToggle(day: DaySchedule, index: number) {
    if (day.isOpen) {
      if (!day.timeSlots || day.timeSlots.length === 0) {
        let prevDay = null;
        // Cerca il primo giorno precedente che è aperto e ha orari
        for (let i = index - 1; i >= 0; i--) {
          if (this.businessHours[i].isOpen && this.businessHours[i].timeSlots?.length > 0) {
            prevDay = this.businessHours[i];
            break;
          }
        }
        
        if (prevDay) {
          day.timeSlots = prevDay.timeSlots.map(ts => ({ ...ts }));
        } else {
          this.addTimeSlot(day);
        }
      }
    } else {
      // Quando il giorno viene disattivato, svuotiamo gli orari
      // in modo che se viene riattivato, ricopia quelli precedenti.
      day.timeSlots = [];
    }
  }

  addTimeSlot(day: DaySchedule) {
    if (!day.timeSlots) day.timeSlots = [];
    
    let newStartTime = '1970-01-01T09:00:00';
    let newEndTime = '1970-01-01T13:00:00';

    if (day.timeSlots.length > 0) {
      const lastSlot = day.timeSlots[day.timeSlots.length - 1];
      newStartTime = lastSlot.endTime;
      
      const startDate = new Date(newStartTime);
      startDate.setHours(startDate.getHours() + 4);
      
      const hh = startDate.getHours().toString().padStart(2, '0');
      const mm = startDate.getMinutes().toString().padStart(2, '0');
      newEndTime = `1970-01-01T${hh}:${mm}:00`;
    }

    day.timeSlots.push({
      startTime: newStartTime,
      endTime: newEndTime
    });
    // Se non era aperto, impostiamolo ad aperto automaticamente
    day.isOpen = true;
  }

  removeTimeSlot(day: DaySchedule, index: number) {
    day.timeSlots.splice(index, 1);
    if (day.timeSlots.length === 0) {
      day.isOpen = false;
    }
  }

  addHoliday() {
    const today = new Date().toISOString().split('T')[0];
    this.holidays.push({
      startDate: today,
      endDate: today,
      description: ''
    });
  }

  removeHoliday(index: number) {
    this.holidays.splice(index, 1);
  }

  getLabelForDay(dayOfWeek: string): string {
    const day = this.DAYS_OF_WEEK.find(d => d.value === dayOfWeek);
    return day ? day.label : dayOfWeek;
  }

  formatTimeForIonic(timeStr: string): string {
    if (!timeStr) return '1970-01-01T09:00:00';
    if (timeStr.includes('T')) return timeStr;
    const parts = timeStr.split(':');
    let hh = parts[0].padStart(2, '0');
    let mm = parts[1] ? parts[1].padStart(2, '0') : '00';
    return `1970-01-01T${hh}:${mm}:00`;
  }

  formatTimeForBackend(ionicDateStr: string): string {
    if (!ionicDateStr) return '00:00:00';
    const date = new Date(ionicDateStr);
    const hh = date.getHours().toString().padStart(2, '0');
    const mm = date.getMinutes().toString().padStart(2, '0');
    return `${hh}:${mm}:00`;
  }

  formatDateForBackend(ionicDateStr: string): string {
    if (!ionicDateStr) return new Date().toISOString().split('T')[0];
    // Rimuove la parte 'T' se presente, restituendo YYYY-MM-DD
    return ionicDateStr.split('T')[0];
  }

  async save() {
    this.isSaving = true;

    // Prepariamo i dati per il backend
    const updatedBusinessHours: DaySchedule[] = this.businessHours.map(h => ({
      ...h,
      timeSlots: h.isOpen ? h.timeSlots.map(ts => ({
        startTime: this.formatTimeForBackend(ts.startTime),
        endTime: this.formatTimeForBackend(ts.endTime)
      })) : []
    }));

    const updatedHolidays: Holiday[] = this.holidays.map(h => ({
      startDate: this.formatDateForBackend(h.startDate),
      endDate: this.formatDateForBackend(h.endDate),
      description: h.description
    }));

    const updatedConfig: TenantConfig = {
      ...this.tenant.config,
      businessHours: updatedBusinessHours,
      holidays: updatedHolidays
    };

    this.apiService.updateTenantConfig(this.tenant.id, updatedConfig).subscribe({
      next: async (res) => {
        this.tenant = res;
        this.isSaving = false;
        
        const toast = await this.toastCtrl.create({
          message: 'Orari di apertura e ferie salvati con successo!',
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
