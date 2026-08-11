import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { AdminDashboardRoutingModule } from './admin-dashboard-routing-module';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminBookingsComponent } from '../admin-bookings/admin-bookings.component';
import { AdminSettingsComponent } from '../admin-settings/admin-settings.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminBookingsComponent,
    AdminSettingsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    AdminDashboardRoutingModule
  ]
})
export class AdminDashboardModule { }
