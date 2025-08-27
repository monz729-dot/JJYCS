// User types
export interface User {
  id: number;
  email: string;
  name: string;
  phone?: string;
  userType: UserType;
  status: UserStatus;
  memberCode?: string;
  companyName?: string;
  businessNumber?: string;
  companyAddress?: string;
  emailVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

// User types - constants for runtime + type for TypeScript
export const USER_TYPE = {
  GENERAL: 'GENERAL',
  CORPORATE: 'CORPORATE',
  PARTNER: 'PARTNER',
  ADMIN: 'ADMIN'
} as const;

export type UserType = typeof USER_TYPE[keyof typeof USER_TYPE];

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  PENDING = 'PENDING',
  REJECTED = 'REJECTED',
  SUSPENDED = 'SUSPENDED',
  WITHDRAWN = 'WITHDRAWN'
}

// Order types
export interface Order {
  id: number;
  orderNumber: string;
  user: User;
  status: OrderStatus;
  shippingType: ShippingType;
  country: string;
  postalCode?: string;
  recipientName: string;
  recipientPhone?: string;
  recipientAddress?: string;
  recipientPostalCode?: string;
  trackingNumber?: string;
  totalCbm: number;
  totalWeight: number;
  requiresExtraRecipient: boolean;
  noMemberCode: boolean;
  storageLocation?: string;
  storageArea?: string;
  arrivedAt?: string;
  actualWeight?: number;
  repackingRequested: boolean;
  repackingCompleted: boolean;
  warehouseNotes?: string;
  shippedAt?: string;
  deliveredAt?: string;
  specialRequests?: string;
  createdAt: string;
  updatedAt: string;
  items?: OrderItem[];
  billing?: Billing;
}

export enum OrderStatus {
  RECEIVED = 'RECEIVED',
  ARRIVED = 'ARRIVED',
  REPACKING = 'REPACKING',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  BILLING = 'BILLING',
  PAYMENT_PENDING = 'PAYMENT_PENDING',
  PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED',
  COMPLETED = 'COMPLETED'
}

export enum ShippingType {
  SEA = 'SEA',
  AIR = 'AIR'
}

// Order Item types
export interface OrderItem {
  id: number;
  orderId: number;
  hsCode: string;
  description: string;
  quantity: number;
  weight: number;
  width: number;
  height: number;
  depth: number;
  cbm: number;
  unitPrice?: number;
  totalPrice?: number;
  createdAt: string;
}

// Billing types
export interface Billing {
  id: number;
  orderId: number;
  proformaIssued: boolean;
  proformaDate?: string;
  finalIssued: boolean;
  finalDate?: string;
  shippingFee: number;
  localDeliveryFee: number;
  repackingFee: number;
  handlingFee: number;
  insuranceFee: number;
  customsFee: number;
  tax: number;
  total: number;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  depositorName?: string;
  paymentDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum PaymentMethod {
  BANK_TRANSFER = 'BANK_TRANSFER',
  CREDIT_CARD = 'CREDIT_CARD',
  CASH = 'CASH'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

// Dashboard types
export interface UserDashboard {
  user: User;
  recentOrders: Order[];
  statusCounts: Record<string, number>;
  paymentInfo: {
    pendingPayments: number;
    pendingAmount: number;
    totalPaid: number;
  };
  trackingOrders: Order[];
  monthlyStats: {
    monthlyData: Array<{
      month: number;
      year: number;
      orders: number;
      revenue: number;
    }>;
  };
  alerts: Alert[];
}

export interface PartnerDashboard {
  partner: User;
  orderStats: {
    totalOrders: number;
    pendingOrders: number;
    completedOrders: number;
    monthlyOrders: number;
  };
  recentOrders: Array<{
    id: number;
    orderNumber: string;
    customerName: string;
    status: string;
    createdAt: string;
    estimatedValue: number;
  }>;
  monthlyTrend: {
    monthlyData: Array<{
      month: number;
      year: number;
      orders: number;
      value: number;
    }>;
  };
}

// Alert types
export interface Alert {
  id: string;
  type: AlertType;
  message: string;
  severity: AlertSeverity;
  actionUrl?: string;
  createdAt: string;
}

export enum AlertType {
  CBM_EXCEEDED = 'CBM_EXCEEDED',
  THB_LIMIT_EXCEEDED = 'THB_LIMIT_EXCEEDED',
  NO_MEMBER_CODE = 'NO_MEMBER_CODE',
  PAYMENT_DUE = 'PAYMENT_DUE',
  ORDER_DELAYED = 'ORDER_DELAYED'
}

export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR'
}

// API Response types
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// Form types
export interface LoginForm {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
  name: string;
  phone?: string;
  userType: UserType;
  companyName?: string;
  businessNumber?: string;
  companyAddress?: string;
  agreeToTerms: boolean;
}

export interface OrderCreateForm {
  shippingType: ShippingType;
  country: string;
  postalCode?: string;
  recipientName: string;
  recipientPhone?: string;
  recipientAddress?: string;
  recipientPostalCode?: string;
  specialRequests?: string;
  items: Array<{
    hsCode: string;
    description: string;
    quantity: number;
    weight: number;
    width: number;
    height: number;
    depth: number;
    unitPrice?: number;
  }>;
}


// QR Code types
export interface QRCodeData {
  qrCode: string; // Base64 encoded
  qrString: string;
  orderNumber: string;
  data: Record<string, any>;
}

// Navigation types
export interface NavigationItem {
  name: string;
  href: string;
  icon: string;
  current?: boolean;
  badge?: number;
  children?: NavigationItem[];
}

// Store types
export interface RootState {
  auth: AuthState;
  dashboard: DashboardState;
  orders: OrdersState;
  partner: PartnerState;
  admin: AdminState;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
}

export interface DashboardState {
  userDashboard: UserDashboard | null;
  partnerDashboard: PartnerDashboard | null;
  loading: boolean;
}

export interface OrdersState {
  orders: Order[];
  currentOrder: Order | null;
  loading: boolean;
  pagination: {
    page: number;
    size: number;
    total: number;
  };
}


export interface PartnerState {
  loading: boolean;
}

export interface AdminState {
  users: User[];
  pendingApprovals: User[];
  loading: boolean;
}