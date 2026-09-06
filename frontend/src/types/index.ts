export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  parentId?: number;
  parentName?: string;
  imageUrl?: string;
  active: boolean;
}

export interface CategoryTree {
  id: number;
  name: string;
  slug: string;
  imageUrl?: string;
  subcategories: CategoryTree[];
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  primary: boolean;
  displayOrder: number;
}

export interface ProductSummary {
  id: number;
  name: string;
  slug: string;
  brand: string;
  price: number;
  stockQuantity: number;
  inStock: boolean;
  primaryImageUrl?: string;
  categoryId: number;
  categoryName: string;
  weight?: string;
}

export interface ProductDetail {
  id: number;
  name: string;
  slug: string;
  description: string;
  brand: string;
  price: number;
  stockQuantity: number;
  inStock: boolean;
  weight?: string;
  categoryId: number;
  categoryName: string;
  categoryBreadcrumbs: string[];
  images: ProductImage[];
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  productBrand: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  availableStock: number;
  inStock: boolean;
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  deliveryFee: number;
  totalAmount: number;
}

export interface Address {
  id: number;
  name: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  pincode: string;
  default: boolean;
  createdAt: string;
}

export interface CreateAddressRequest {
  name: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  pincode: string;
  isDefault?: boolean;
}

export interface OrderItem {
  id: number;
  productId?: number;
  productName: string;
  quantity: number;
  price: number;
  totalPrice: number;
}

export type OrderStatus = 'PLACED' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
export type PaymentMethod = 'COD';
export type PaymentStatus = 'PENDING' | 'PAID';

export interface OrderSummary {
  id: number;
  orderDate: string;
  totalAmount: number;
  orderStatus: OrderStatus;
  paymentMethod: PaymentMethod;
  itemCount: number;
  canCancel: boolean;
}

export interface OrderDetail {
  id: number;
  orderDate: string;
  totalAmount: number;
  orderStatus: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  shippingAddress: string;
  canCancel: boolean;
  items: OrderItem[];
}

export interface CreateOrderRequest {
  addressId: number;
  paymentMethod: PaymentMethod;
}
