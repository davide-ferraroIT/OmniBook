import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TenantResponse } from '../../core/models/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  standalone: false
})
export class AdminDashboardComponent implements OnInit {
  tenant: TenantResponse | null = null;
  slug: string | null = null;
  activeSegment: string = 'bookings';
  isLoading: boolean = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService
  ) {}

  ngOnInit() {
    this.slug = this.route.snapshot.paramMap.get('slug');
    if (this.slug) {
      this.loadTenant();
    } else {
      this.error = 'Slug non fornito';
      this.isLoading = false;
    }
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
}
