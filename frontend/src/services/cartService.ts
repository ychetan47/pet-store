import api from './api';
import { ApiResponse, Cart } from '../types';

export const cartService = {
  async getCart(): Promise<Cart> {
    const res = await api.get<ApiResponse<Cart>>('/cart');
    return res.data.data;
  },

  async addToCart(productId: number, quantity: number = 1): Promise<Cart> {
    const res = await api.post<ApiResponse<Cart>>('/cart/items', { productId, quantity });
    return res.data.data;
  },

  async updateItemQuantity(itemId: number, quantity: number): Promise<Cart> {
    const res = await api.put<ApiResponse<Cart>>(`/cart/items/${itemId}`, { quantity });
    return res.data.data;
  },

  async removeItem(itemId: number): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>(`/cart/items/${itemId}`);
    return res.data.data;
  },

  async clearCart(): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>('/cart');
    return res.data.data;
  },
};
