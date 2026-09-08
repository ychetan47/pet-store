import api from './api';
import { ApiResponse, CreateOrderRequest, OrderDetail, OrderSummary } from '../types';

export const orderService = {
  async createOrder(data: CreateOrderRequest): Promise<OrderDetail> {
    const res = await api.post<ApiResponse<OrderDetail>>('/orders', data);
    return res.data.data;
  },

  async getOrders(): Promise<OrderSummary[]> {
    const res = await api.get<ApiResponse<OrderSummary[]>>('/orders');
    return res.data.data;
  },

  async getOrderById(id: number): Promise<OrderDetail> {
    const res = await api.get<ApiResponse<OrderDetail>>(`/orders/${id}`);
    return res.data.data;
  },

  async cancelOrder(id: number): Promise<OrderDetail> {
    const res = await api.post<ApiResponse<OrderDetail>>(`/orders/${id}/cancel`);
    return res.data.data;
  },
};
