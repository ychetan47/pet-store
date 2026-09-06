import api from './api';
import { ApiResponse, Category, CategoryTree } from '../types';

export const categoryService = {
  async getCategories(): Promise<Category[]> {
    const res = await api.get<ApiResponse<Category[]>>('/categories');
    return res.data.data;
  },

  async getCategoryTree(): Promise<CategoryTree[]> {
    const res = await api.get<ApiResponse<CategoryTree[]>>('/categories/tree');
    return res.data.data;
  },

  async getCategoryById(id: number): Promise<Category> {
    const res = await api.get<ApiResponse<Category>>(`/categories/${id}`);
    return res.data.data;
  },
};
