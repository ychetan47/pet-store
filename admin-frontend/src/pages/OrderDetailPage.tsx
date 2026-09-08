import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  User,
  Phone,
  MapPin,
  CreditCard,
  Package,
  CheckCircle,
  Truck,
  AlertCircle,
  Save,
} from 'lucide-react';
import { adminOrdersApi } from '../services/api';
import { Order, OrderStatus } from '../types';

export const OrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const orderId = Number(id);

  const [order, setOrder] = useState<Order | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus>('PLACED');
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    loadOrder();
  }, [orderId]);

  const loadOrder = async () => {
    try {
      setLoading(true);
      const data = await adminOrdersApi.getOrderById(orderId);
      setOrder(data);
      setSelectedStatus(data.orderStatus);
    } catch (err) {
      console.error('Failed to load order', err);
      setError('Failed to fetch order details.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async () => {
    if (!order || selectedStatus === order.orderStatus) return;
    try {
      setUpdating(true);
      setError(null);
      setSuccessMsg(null);
      const updated = await adminOrdersApi.updateStatus(orderId, selectedStatus);
      setOrder(updated);
      setSelectedStatus(updated.orderStatus);
      setSuccessMsg(`Order status successfully updated to ${updated.orderStatus}!`);
    } catch (err: any) {
      console.error('Failed to update status', err);
      setError(err.response?.data?.message || 'Failed to update order status.');
    } finally {
      setUpdating(false);
    }
  };

  const getStatusBadgeClass = (status: OrderStatus) => {
    switch (status) {
      case 'PLACED':
        return 'badge-placed';
      case 'CONFIRMED':
        return 'badge-confirmed';
      case 'SHIPPED':
        return 'badge-shipped';
      case 'DELIVERED':
        return 'badge-delivered';
      case 'CANCELLED':
        return 'badge-cancelled';
      default:
        return 'badge-inactive';
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        Loading order #{orderId}...
      </div>
    );
  }

  if (!order) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center' }}>
        <p style={{ color: 'var(--danger)', marginBottom: '1rem' }}>Order not found.</p>
        <Link to="/orders" className="btn btn-outline btn-sm">
          Return to Orders
        </Link>
      </div>
    );
  }

  const isTerminal = order.orderStatus === 'DELIVERED' || order.orderStatus === 'CANCELLED';

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Link to="/orders" className="btn btn-outline btn-sm">
            <ArrowLeft size={16} />
            <span>Back to Orders</span>
          </Link>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Order #{order.id}</h2>
            <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Calendar size={13} />
              <span>
                Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', {
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </span>
            </div>
          </div>
        </div>

        <span className={`badge ${getStatusBadgeClass(order.orderStatus)}`} style={{ fontSize: '0.875rem', padding: '0.4rem 1rem' }}>
          {order.orderStatus}
        </span>
      </div>

      {error && (
        <div style={{ background: 'var(--danger-light)', color: '#991b1b', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      {successMsg && (
        <div style={{ background: 'var(--success-light)', color: '#065f46', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem' }}>
          {successMsg}
        </div>
      )}

      {/* Status Management Bar */}
      <div style={{ background: 'white', padding: '1.25rem 1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>Fulfillment Workflow Status</h4>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {isTerminal
              ? 'This order has reached a terminal state and cannot be modified.'
              : 'Transition order state to update customer status and manage inventory.'}
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <select
            className="form-control"
            style={{ width: 'auto', minWidth: '180px' }}
            disabled={isTerminal || updating}
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value as OrderStatus)}
          >
            <option value="PLACED">PLACED</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="SHIPPED">SHIPPED</option>
            <option value="DELIVERED">DELIVERED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
          <button
            className="btn btn-primary btn-sm"
            disabled={isTerminal || updating || selectedStatus === order.orderStatus}
            onClick={handleStatusUpdate}
          >
            <Save size={16} />
            <span>{updating ? 'Updating...' : 'Update Status'}</span>
          </button>
        </div>
      </div>

      {/* Two Column Details Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Shipping Information */}
        <div style={{ background: 'white', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-main)' }}>
            <MapPin size={18} style={{ color: 'var(--primary)' }} />
            <span>Shipping Details Snapshot</span>
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.875rem' }}>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Recipient: </span>
              <strong>{order.shippingName}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Phone: </span>
              <strong>{order.shippingPhone}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Address: </span>
              <div>
                {order.shippingAddressLine1}
                {order.shippingAddressLine2 ? `, ${order.shippingAddressLine2}` : ''}
              </div>
              <div>
                {order.shippingCity}, {order.shippingState} - <strong>{order.shippingPincode}</strong>
              </div>
            </div>
          </div>
        </div>

        {/* Payment & Order Summary */}
        <div style={{ background: 'white', padding: '1.5rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-main)' }}>
            <CreditCard size={18} style={{ color: 'var(--primary)' }} />
            <span>Payment & Summary</span>
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.875rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Payment Method:</span>
              <strong>{order.paymentMethod === 'COD' ? 'Cash on Delivery (COD)' : order.paymentMethod}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Payment Status:</span>
              <strong style={{ color: order.paymentStatus === 'COMPLETED' ? 'var(--success)' : 'inherit' }}>
                {order.paymentStatus}
              </strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Customer ID:</span>
              <span>User #{order.userId}</span>
            </div>
            <div style={{ borderTop: '1px solid var(--border)', paddingTop: '0.75rem', marginTop: '0.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontWeight: 700, fontSize: '1rem' }}>Total Amount:</span>
              <span style={{ fontWeight: 800, fontSize: '1.3rem', color: 'var(--primary)' }}>
                ₹{order.totalAmount.toFixed(2)}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Order Line Items */}
      <div className="card-table-wrapper">
        <div className="card-table-header">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Package size={18} />
            <span>Purchased Items ({order.items?.length || 0})</span>
          </h3>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Product ID</th>
              <th>Quantity</th>
              <th>Unit Price</th>
              <th style={{ textAlign: 'right' }}>Line Total</th>
            </tr>
          </thead>
          <tbody>
            {order.items?.map((item) => (
              <tr key={item.id}>
                <td>
                  <strong style={{ color: 'var(--text-main)' }}>{item.productName}</strong>
                </td>
                <td>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.825rem' }}>#{item.productId}</span>
                </td>
                <td>{item.quantity}</td>
                <td>₹{item.price.toFixed(2)}</td>
                <td style={{ textAlign: 'right', fontWeight: 700 }}>₹{item.totalPrice.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
