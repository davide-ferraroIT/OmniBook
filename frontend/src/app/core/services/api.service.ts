import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BookingCreateRequest, BookingResponse, ServiceResponse, TenantResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Tenant API
  getTenantBySlug(slug: string): Observable<TenantResponse> {
    return this.http.get<TenantResponse>(`${this.apiUrl}/tenants/slug/${slug}`);
  }

  // Services API
  getServicesByTenantId(tenantId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/tenants/${tenantId}/services`);
  }

  // Booking API
  getAvailability(tenantId: string, serviceId: string, date: string, resourceId?: string): Observable<string[]> {
    let params = new HttpParams()
      .set('serviceId', serviceId)
      .set('date', date);
    
    if (resourceId) {
      params = params.set('resourceId', resourceId);
    }
    return this.http.get<string[]>(`${this.apiUrl}/tenants/${tenantId}/bookings/availability`, { params });
  }

  createBooking(tenantId: string, request: BookingCreateRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.apiUrl}/tenants/${tenantId}/bookings`, request);
  }

  updateTenantConfig(id: string, config: any): Observable<TenantResponse> {
    return this.http.patch<TenantResponse>(`${this.apiUrl}/tenants/${id}/config`, config);
  }

  getBookingsByTenantId(tenantId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/tenants/${tenantId}/bookings`);
  }

  updateBookingStatus(tenantId: string, bookingId: string, status: string): Observable<BookingResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<BookingResponse>(`${this.apiUrl}/tenants/${tenantId}/bookings/${bookingId}/status`, null, { params });
  }

  updateBooking(tenantId: string, bookingId: string, request: any): Observable<BookingResponse> {
    return this.http.put<BookingResponse>(`${this.apiUrl}/tenants/${tenantId}/bookings/${bookingId}`, request);
  }
}
