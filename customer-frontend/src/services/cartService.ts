import api from './api';
import { ApiResponse, Cart } from '../types';

function normalizeCart(cart: Cart): Cart {
  if (!cart) return cart;
  const items = (cart.items || []).map((item: any) => {
    const priceVal = Number(item.unitPrice ?? item.price ?? 0);
    const subtotalVal = Number(item.subtotal ?? (priceVal * (item.quantity ?? 1)));
    return {
      ...item,
      unitPrice: priceVal,
      price: priceVal,
      productImage: item.productImage || item.imageUrl,
      imageUrl: item.imageUrl || item.productImage,
      subtotal: subtotalVal,
    };
  });
  const subtotalSum = items.reduce((sum: number, i: any) => sum + (i.subtotal || 0), 0);
  const deliveryFeeVal = Number(cart.deliveryFee ?? 0);
  return {
    ...cart,
    items,
    totalItems: cart.totalItems ?? items.reduce((sum: number, i: any) => sum + (i.quantity || 0), 0),
    subtotal: cart.subtotal !== undefined && cart.subtotal !== null ? Number(cart.subtotal) : subtotalSum,
    deliveryFee: deliveryFeeVal,
    totalAmount: cart.totalAmount !== undefined && cart.totalAmount !== null ? Number(cart.totalAmount) : (subtotalSum + deliveryFeeVal),
  };
}

export const cartService = {
  async getCart(): Promise<Cart> {
    const res = await api.get<ApiResponse<Cart>>('/cart');
    return normalizeCart(res.data.data);
  },

  async addToCart(productId: number, quantity: number = 1): Promise<Cart> {
    const res = await api.post<ApiResponse<Cart>>('/cart/items', { productId, quantity });
    return normalizeCart(res.data.data);
  },

  async updateItemQuantity(itemId: number, quantity: number): Promise<Cart> {
    const res = await api.put<ApiResponse<Cart>>(`/cart/items/${itemId}`, { quantity });
    return normalizeCart(res.data.data);
  },

  async removeItem(itemId: number): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>(`/cart/items/${itemId}`);
    return normalizeCart(res.data.data);
  },

  async clearCart(): Promise<Cart> {
    const res = await api.delete<ApiResponse<Cart>>('/cart');
    return normalizeCart(res.data.data);
  },
};
