import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { OrderSummary } from '../types';
import { orderService } from '../services/orderService';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { OrderStatusBadge } from '../components/order/OrderStatusBadge';
import { Package, Calendar, ChevronRight } from 'lucide-react';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    orderService.getOrders()
      .then(setOrders)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <div style={{ display: 'inline-block', width: '40px', height: '40px', border: '3px solid var(--border-color)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading your orders...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      <Breadcrumb items={[{ label: 'My Orders' }]} />

      <h2 style={{ marginBottom: '1.75rem' }}>My Orders</h2>

      {orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '5rem 1rem', background: '#fff', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)' }}>
          <Package size={56} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
          <h3>No Orders Yet</h3>
          <p style={{ color: 'var(--text-muted)', margin: '1rem 0 2rem' }}>
            You haven't placed any orders yet. Spoil your furry friend today!
          </p>
          <Link to="/products" className="btn btn-primary btn-lg">
            Start Shopping
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {orders.map((order) => {
            const dateStr = new Date(order.orderDate).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'short',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            });

            return (
              <div
                key={order.id}
                style={{
                  backgroundColor: '#fff',
                  borderRadius: 'var(--radius-lg)',
                  padding: '1.5rem',
                  border: '1px solid var(--border-color)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '1.5rem',
                  boxShadow: 'var(--shadow-sm)',
                }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.35rem' }}>
                    <h3 style={{ fontSize: '1.15rem' }}>Order #{order.id}</h3>
                    <OrderStatusBadge status={order.orderStatus} />
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Calendar size={14} /> {dateStr}
                    </span>
                    <span>•</span>
                    <span>Payment: {order.paymentMethod}</span>
                    <span>•</span>
                    <span>{order.itemCount} items</span>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block' }}>Total Amount</span>
                    <span style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary)' }}>
                      ₹{order.totalAmount.toLocaleString('en-IN')}
                    </span>
                  </div>

                  <Link to={`/orders/${order.id}`} className="btn btn-secondary btn-sm" style={{ gap: '0.3rem' }}>
                    View Details <ChevronRight size={14} />
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
