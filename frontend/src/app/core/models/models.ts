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
  businessHours: any[];
  allowAutoAssignment: boolean;
  autoAcceptBookings?: boolean;
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
  type: string;
  capacity: number;
}

export interface ServiceResponse {
  id: string;
  name: string;
  durationMinutes: number;
  allowedResources: ResourceResponse[];
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
