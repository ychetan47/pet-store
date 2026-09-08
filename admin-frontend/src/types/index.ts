export type Role = 'CUSTOMER' | 'ADMIN';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  name: string;
  email: string;
  role: Role;
}

export type ProductStatus = 'ACTIVE' | 'INACTIVE';

export interface ProductImageDto {
  id: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  brand: string;
  price: number;
  stockQuantity: number;
  categoryId?: number;
  categoryName?: string;
  primaryImageUrl?: string;
  weight?: string;
  status: ProductStatus;
  createdAt: string;
}

export interface ProductDetail extends Product {
  description?: string;
  images: ProductImageDto[];
  categorySlug?: string;
  updatedAt?: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  parentId?: number;
  parentName?: string;
  imageUrl?: string;
  isActive: boolean;
}

export interface CategoryTree {
  id: number;
  name: string;
  slug: string;
  imageUrl?: string;
  subcategories: CategoryTree[];
}

export type OrderStatus = 'PLACED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
export type PaymentMethod = 'COD';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  totalPrice: number;
}

export interface Order {
  id: number;
  userId: number;
  totalAmount: number;
  orderStatus: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  shippingName: string;
  shippingPhone: string;
  shippingAddressLine1: string;
  shippingAddressLine2?: string;
  shippingCity: string;
  shippingState: string;
  shippingPincode: string;
  shippingAddressFormatted: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface InventoryItem {
  id: number;
  productId: number;
  productName?: string;
  availableQuantity: number;
  reservedQuantity: number;
  totalQuantity: number;
  createdAt: string;
  updatedAt?: string;
}

export interface InventoryReservation {
  id: number;
  orderId: number;
  productId: number;
  reservedQuantity: number;
  status: 'PENDING' | 'COMMITTED' | 'RELEASED';
  createdAt: string;
  updatedAt?: string;
}

