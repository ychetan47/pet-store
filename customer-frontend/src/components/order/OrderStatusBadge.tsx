import React from 'react';
import { OrderStatus } from '../../types';
import { Clock, CheckCircle2, Truck, PackageCheck, XCircle } from 'lucide-react';

interface OrderStatusBadgeProps {
  status: OrderStatus;
}

export const OrderStatusBadge: React.FC<OrderStatusBadgeProps> = ({ status }) => {
  switch (status) {
    case 'PLACED':
      return (
        <span className="badge badge-placed">
          <Clock size={13} /> Placed
        </span>
      );
    case 'CONFIRMED':
      return (
        <span className="badge badge-confirmed">
          <CheckCircle2 size={13} /> Confirmed
        </span>
      );
    case 'SHIPPED':
      return (
        <span className="badge badge-shipped">
          <Truck size={13} /> Shipped
        </span>
      );
    case 'DELIVERED':
      return (
        <span className="badge badge-delivered">
          <PackageCheck size={13} /> Delivered
        </span>
      );
    case 'CANCELLED':
      return (
        <span className="badge badge-cancelled">
          <XCircle size={13} /> Cancelled
        </span>
      );
    default:
      return <span className="badge">{status}</span>;
  }
};
