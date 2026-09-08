import axios from 'axios';
import {
  AdminUser,
  AuthResponse,
  Category,
  CategoryTree,
  Order,
  OrderStatus,
  PageResponse,
  Product,
  ProductDetail,
  ProductImageDto,
  InventoryItem,
  InventoryReservation,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('petstore_admin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      if (window.location.pathname !== '/login') {
        localStorage.removeItem('petstore_admin_token');
        localStorage.removeItem('petstore_admin_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// --- Auth API ---
export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const res = await api.post('/auth/login', { email, password });
    return res.data.data;
  },
  me: async (): Promise<AdminUser> => {
    const res = await api.get('/auth/me');
    return res.data.data;
  },
};

// --- Admin Products API ---
export const adminProductsApi = {
  getProducts: async (params?: {
    categoryId?: number;
    brand?: string;
    status?: string;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<Product>> => {
    const res = await api.get('/admin/products', { params });
    const data = res.data.data;
    if (data && !data.content) {
      data.content = [];
    }
    return data;
  },
  getProductById: async (id: number): Promise<ProductDetail> => {
    const res = await api.get(`/products/${id}`);
    return res.data.data;
  },
  createProduct: async (data: any): Promise<ProductDetail> => {
    const res = await api.post('/admin/products', data);
    return res.data.data;
  },
  updateProduct: async (id: number, data: any): Promise<ProductDetail> => {
    const res = await api.put(`/admin/products/${id}`, data);
    return res.data.data;
  },
  deleteProduct: async (id: number): Promise<void> => {
    await api.delete(`/admin/products/${id}`);
  },
  updateStock: async (id: number, stock: number): Promise<Product> => {
    const res = await api.put(`/admin/products/${id}/stock`, { stock });
    return res.data.data;
  },
  addImage: async (id: number, imageUrl: string, isPrimary: boolean = false, displayOrder: number = 0): Promise<ProductImageDto> => {
    const res = await api.post(`/admin/products/${id}/images`, null, {
      params: { imageUrl, isPrimary, displayOrder },
    });
    return res.data.data;
  },
  deleteImage: async (productId: number, imageId: number): Promise<void> => {
    await api.delete(`/admin/products/${productId}/images/${imageId}`);
  },
  getDashboardStats: async (): Promise<{ totalProducts: number; lowStockCount: number; lowStockProducts: Product[] }> => {
    const res = await api.get('/admin/products/dashboard');
    return res.data.data;
  },
};

// --- Admin Categories API ---
export const adminCategoriesApi = {
  getCategories: async (): Promise<Category[]> => {
    const res = await api.get('/categories');
    return res.data.data;
  },
  getCategoryTree: async (): Promise<CategoryTree[]> => {
    const res = await api.get('/categories/tree');
    return res.data.data;
  },
  createCategory: async (data: { name: string; slug?: string; parentId?: number; imageUrl?: string; isActive?: boolean }): Promise<Category> => {
    const res = await api.post('/admin/categories', data);
    return res.data.data;
  },
  updateCategory: async (id: number, data: { name: string; slug?: string; parentId?: number; imageUrl?: string; isActive?: boolean }): Promise<Category> => {
    const res = await api.put(`/admin/categories/${id}`, data);
    return res.data.data;
  },
  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/admin/categories/${id}`);
  },
};

// --- Admin Orders API ---
export const adminOrdersApi = {
  getOrders: async (params?: { status?: OrderStatus; page?: number; size?: number }): Promise<PageResponse<Order>> => {
    const res = await api.get('/admin/orders', { params });
    return res.data.data;
  },
  getOrderById: async (id: number): Promise<Order> => {
    const res = await api.get(`/admin/orders/${id}`);
    return res.data.data;
  },
  updateStatus: async (id: number, status: OrderStatus): Promise<Order> => {
    const res = await api.put(`/admin/orders/${id}/status`, { status });
    return res.data.data;
  },
  getDashboardStats: async (): Promise<{ totalOrders: number; pendingOrders: number; shippedOrders: number; deliveredOrders: number; cancelledOrders: number; totalRevenue: number }> => {
    const res = await api.get('/admin/orders/dashboard');
    return res.data.data;
  },
};

// --- Admin Inventory API ---
export const adminInventoryApi = {
  getInventory: async (): Promise<InventoryItem[]> => {
    const res = await api.get('/admin/inventory/items');
    const data = res.data.data;
    if (data && Array.isArray(data.content)) {
      return data.content;
    }
    return Array.isArray(data) ? data : [];
  },
  getItem: async (productId: number): Promise<InventoryItem> => {
    const res = await api.get(`/admin/inventory/items/${productId}`);
    return res.data.data;
  },
  updateStock: async (productId: number, totalStock: number): Promise<InventoryItem> => {
    const res = await api.put(`/admin/inventory/items/${productId}/stock`, { stock: totalStock });
    return res.data.data;
  },
  getReservations: async (productId?: number): Promise<InventoryReservation[]> => {
    const res = await api.get('/admin/inventory/reservations', { params: productId ? { productId } : undefined });
    return res.data.data;
  },
};
