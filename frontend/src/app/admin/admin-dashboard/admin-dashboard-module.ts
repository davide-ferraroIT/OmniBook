import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminDashboardRoutingModule } from './admin-dashboard-routing-module';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminBookingsComponent } from '../admin-bookings/admin-bookings.component';
import { AdminSettingsComponent } from '../admin-settings/admin-settings.component';
import { AdminServicesComponent } from '../admin-services/admin-services.component';
import { ServiceModalComponent } from '../admin-services/service-modal/service-modal.component';
import { AdminBusinessHoursComponent } from '../admin-business-hours/admin-business-hours.component';
import { BookingModalComponent } from '../admin-bookings/booking-modal/booking-modal.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminBookingsComponent,
    AdminSettingsComponent,
    AdminServicesComponent,
    ServiceModalComponent,
    AdminBusinessHoursComponent,
    BookingModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    AdminDashboardRoutingModule
  ]
})
export class AdminDashboardModule { }
