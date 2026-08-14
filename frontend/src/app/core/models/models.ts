export interface TimeSlot {
  startTime: string;
  endTime: string;
}

export interface DaySchedule {
  dayOfWeek: string;
  isOpen: boolean;
  timeSlots: TimeSlot[];
}

export interface Holiday {
  startDate: string;
  endDate: string;
  description: string;
}

export interface TenantConfig {
  primaryColor: string;
  terminology: {
    resourceTerm: string;
    serviceTerm: string;
    bookingTerm: string;
  };
  supportedLocales: string[];
  activeModules: string[];
  paymentGatewayConfig: Record<string, string> | null;
  businessHours: DaySchedule[];
  allowAutoAssignment: boolean;
  autoAcceptBookings?: boolean;
  holidays: Holiday[];
}

export interface TenantResponse {
  id: string;
  name: string;
  slug: string;
  config: TenantConfig;
}

export interface ResourceResponse {
  id: string;
  name: string;
  capacity: number;
  imageUrl?: string;
}

export interface ResourceCreateRequest {
  name: string;
  capacity: number;
  imageUrl?: string;
}

export interface ServiceResponse {
  id: string;
  name: string;
  durationMinutes: number;
  allowedResources: ResourceResponse[];
  imageUrl?: string;
}

export interface ServiceCreateRequest {
  name: string;
  durationMinutes: number;
  allowedResourceIds?: string[];
  imageUrl?: string;
}

export interface BookingCreateRequest {
  serviceId: string;
  resourceId?: string;
  startTime: string; // ISO 8601 string
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
}

export interface BookingUpdateRequest {
  serviceId: string;
  resourceId?: string;
  startTime: string; // ISO 8601 string
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
}

export interface BookingResponse {
  id: string;
  service: ServiceResponse;
  resource: ResourceResponse;
  startTime: string;
  endTime: string;
  status: string;
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
}
