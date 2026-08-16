import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TenantResponse } from '../../core/models/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class AdminDashboardComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);

  tenant: TenantResponse | null = null;
  slug: string | null = null;
  activeSegment: string = 'bookings';
  isLoading: boolean = true;
  error: string | null = null;


  constructor() {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.slug = params.get('slug');
      if (this.slug) {
        this.isLoading = true;
        this.loadTenant();
      } else {
        this.error = 'Slug non fornito';
        this.isLoading = false;
      }
    });
  }

  loadTenant() {
    this.apiService.getTenantBySlug(this.slug!).subscribe({
      next: (tenant) => {
        this.tenant = tenant;
        if (tenant.config?.primaryColor) {
          this.applyTheme(tenant.config.primaryColor);
        }
        this.isLoading = false;
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

  segmentChanged(event: any) {
    this.activeSegment = event.detail.value;
  }

  touchStartX = 0;
  touchEndX = 0;

  onTouchStart(event: TouchEvent) {
    this.touchStartX = event.changedTouches[0].screenX;
  }

  onTouchEnd(event: TouchEvent) {
    this.touchEndX = event.changedTouches[0].screenX;
    this.handleSwipeGesture();
  }

  handleSwipeGesture() {
    const swipeThreshold = 50;
    // Swipe left (move to next tab)
    if (this.touchEndX < this.touchStartX - swipeThreshold) {
      if (this.activeSegment === 'bookings') {
        this.activeSegment = 'settings';
      }
    }
    // Swipe right (move to prev tab)
    if (this.touchEndX > this.touchStartX + swipeThreshold) {
      if (this.activeSegment === 'settings') {
        this.activeSegment = 'bookings';
      }
    }
  }
}
