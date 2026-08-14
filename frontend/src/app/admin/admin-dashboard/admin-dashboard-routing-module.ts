import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminServicesComponent } from '../admin-services/admin-services.component';
import { AdminBusinessHoursComponent } from '../admin-business-hours/admin-business-hours.component';
import { AdminResourcesComponent } from '../admin-resources/admin-resources.component';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent
  },
  {
    path: 'services',
    component: AdminServicesComponent
  },
  {
    path: 'business-hours',
    component: AdminBusinessHoursComponent
  },
  {
    path: 'resources',
    component: AdminResourcesComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminDashboardRoutingModule { }
