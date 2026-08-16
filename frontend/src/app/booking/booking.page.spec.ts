import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withXhr } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookingPage } from './booking.page';

describe('BookingPage', () => {
  let component: BookingPage;
  let fixture: ComponentFixture<BookingPage>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withXhr()), provideHttpClientTesting(), provideRouter([])],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(BookingPage);
    component = fixture.componentInstance;
    (component as any).tenant = { id: 'test', name: 'test', slug: 'test', config: {} };
    (component as any).booking = { id: 'b1', service: { name: 'S1', duration: 30, price: 10 } };
    (component as any).tenantId = 'test';
    (component as any).service = { id: 's1', name: 'S1', duration: 30, price: 10 };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
