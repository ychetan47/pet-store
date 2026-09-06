import api from './api';
import { Address, ApiResponse, CreateAddressRequest } from '../types';

export const addressService = {
  async getAddresses(): Promise<Address[]> {
    const res = await api.get<ApiResponse<Address[]>>('/addresses');
    return res.data.data;
  },

  async getAddressById(id: number): Promise<Address> {
    const res = await api.get<ApiResponse<Address>>(`/addresses/${id}`);
    return res.data.data;
  },

  async createAddress(data: CreateAddressRequest): Promise<Address> {
    const res = await api.post<ApiResponse<Address>>('/addresses', data);
    return res.data.data;
  },

  async updateAddress(id: number, data: Partial<CreateAddressRequest>): Promise<Address> {
    const res = await api.put<ApiResponse<Address>>(`/addresses/${id}`, data);
    return res.data.data;
  },

  async deleteAddress(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/addresses/${id}`);
  },
};
