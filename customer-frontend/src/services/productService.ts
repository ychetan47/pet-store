import api from './api';
import { ApiResponse, PageResponse, ProductDetail, ProductSummary } from '../types';

export interface ProductFilters {
  pet?: string;
  categoryId?: number;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export const productService = {
  async getProducts(filters: ProductFilters = {}): Promise<PageResponse<ProductSummary>> {
    const params: Record<string, any> = {};
    if (filters.pet) params.pet = filters.pet;
    if (filters.categoryId) params.categoryId = filters.categoryId;
    if (filters.brand) params.brand = filters.brand;
    if (filters.minPrice !== undefined && filters.minPrice !== null) params.minPrice = filters.minPrice;
    if (filters.maxPrice !== undefined && filters.maxPrice !== null) params.maxPrice = filters.maxPrice;
    if (filters.search) params.search = filters.search;
    if (filters.page !== undefined) params.page = filters.page;
    if (filters.size !== undefined) params.size = filters.size;
    if (filters.sortBy) params.sortBy = filters.sortBy;
    if (filters.sortDirection) params.sortDirection = filters.sortDirection;

    const res = await api.get<ApiResponse<PageResponse<ProductSummary>>>('/products', { params });
    const data = res.data.data;
    if (data) {
      const page = data.page !== undefined ? data.page : (data.pageNumber ?? 0);
      const size = data.size !== undefined ? data.size : (data.pageSize ?? 12);
      data.page = page;
      data.pageNumber = page;
      data.size = size;
      data.pageSize = size;
      if (data.content) {
        data.content = data.content.map((p) => ({
          ...p,
          inStock: p.inStock !== undefined ? p.inStock : p.stockQuantity > 0,
        }));
      }
    }
    return data;
  },

  async getProductById(id: number): Promise<ProductDetail> {
    const res = await api.get<ApiResponse<ProductDetail>>(`/products/${id}`);
    const data = res.data.data;
    if (data) {
      data.inStock = data.inStock !== undefined ? data.inStock : data.stockQuantity > 0;
      data.categoryBreadcrumbs = data.categoryBreadcrumbs && data.categoryBreadcrumbs.length > 0
        ? data.categoryBreadcrumbs
        : (data.categoryName ? [data.categoryName] : []);
    }
    return data;
  },

  async getProductBySlug(slug: string): Promise<ProductDetail> {
    const res = await api.get<ApiResponse<ProductDetail>>(`/products/slug/${slug}`);
    const data = res.data.data;
    if (data) {
      data.inStock = data.inStock !== undefined ? data.inStock : data.stockQuantity > 0;
      data.categoryBreadcrumbs = data.categoryBreadcrumbs && data.categoryBreadcrumbs.length > 0
        ? data.categoryBreadcrumbs
        : (data.categoryName ? [data.categoryName] : []);
    }
    return data;
  },

  async getBrands(): Promise<string[]> {
    const res = await api.get<ApiResponse<string[]>>('/products/brands');
    return res.data.data;
  },
};
