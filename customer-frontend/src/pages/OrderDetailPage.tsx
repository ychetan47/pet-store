import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { OrderDetail, OrderItem } from '../types';
import { orderService } from '../services/orderService';
import { useToast } from '../context/ToastContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { OrderStatusBadge } from '../components/order/OrderStatusBadge';
import { Modal } from '../components/common/Modal';
import { MapPin, CreditCard, ShieldAlert, ArrowLeft } from 'lucide-react';

export const OrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { showToast } = useToast();

  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [cancelling, setCancelling] = useState<boolean>(false);
  const [showCancelModal, setShowCancelModal] = useState<boolean>(false);

  const fetchOrder = async () => {
    if (!id) return;
    try {
      setLoading(true);
      const data = await orderService.getOrderById(Number(id));
      setOrder(data);
    } catch (err: any) {
      console.error('Failed to load order', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;
    if (order && order.orderStatus === 'PLACED') {
      interval = setInterval(async () => {
        try {
          const updated = await orderService.getOrderById(Number(id));
          setOrder(updated);
        } catch (e) {
          // ignore background polling error
        }
      }, 2500);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [order?.orderStatus, id]);

  const handleCancelOrder = async () => {
    if (!order) return;
    try {
      setCancelling(true);
      const updated = await orderService.cancelOrder(order.id);
      setOrder(updated);
      setShowCancelModal(false);
      showToast(`Order #${order.id} has been cancelled successfully`, 'info');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to cancel order';
      showToast(msg, 'error');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <div style={{ display: 'inline-block', width: '40px', height: '40px', border: '3px solid var(--border-color)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading order details...</p>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <h2>Order Not Found</h2>
        <p style={{ color: 'var(--text-muted)', margin: '1rem 0 2rem' }}>We couldn't find the requested order.</p>
        <Link to="/orders" className="btn btn-primary">
          Back to Orders
        </Link>
      </div>
    );
  }

  const dateStr = new Date(order.orderDate).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      <Breadcrumb items={[{ label: 'My Orders', path: '/orders' }, { label: `Order #${order.id}` }]} />

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.75rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.35rem' }}>
            <h2 style={{ fontSize: '1.75rem' }}>Order #{order.id}</h2>
            <OrderStatusBadge status={order.orderStatus} />
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Placed on {dateStr}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          {order.canCancel && (
            <button
              className="btn btn-danger-outline"
              onClick={() => setShowCancelModal(true)}
              disabled={cancelling}
            >
              Cancel Order
            </button>
          )}

          <Link to="/orders" className="btn btn-secondary btn-sm">
            <ArrowLeft size={16} /> All Orders
          </Link>
        </div>
      </div>

      {order.orderStatus === 'PLACED' && (
        <div
          style={{
            backgroundColor: '#eff6ff',
            border: '1px solid #bfdbfe',
            borderRadius: 'var(--radius-lg)',
            padding: '1rem 1.25rem',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.85rem',
            color: '#1e40af',
          }}
        >
          <div
            style={{
              display: 'inline-block',
              width: '20px',
              height: '20px',
              border: '2px solid #93c5fd',
              borderTopColor: '#2563eb',
              borderRadius: '50%',
              animation: 'spin 0.8s linear infinite',
              flexShrink: 0,
            }}
          />
          <div>
            <strong style={{ fontSize: '0.95rem', display: 'block', marginBottom: '0.15rem' }}>
              Order Placed — Confirming Inventory Reservation
            </strong>
            <span style={{ fontSize: '0.85rem', color: '#3b82f6' }}>
              We are reserving your selected items. This status will update to <strong>CONFIRMED</strong> in a few moments.
            </span>
          </div>
        </div>
      )}

      <div className="cart-page-layout">
        <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.75rem', border: '1px solid var(--border-color)' }}>
          <h3 style={{ marginBottom: '1.25rem', fontSize: '1.15rem' }}>Items Ordered</h3>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {order.items.map((item: OrderItem) => (
              <div
                key={item.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '1rem 0',
                  borderBottom: '1px solid var(--border-color)',
                  gap: '1rem',
                }}
              >
                <div>
                  <h4 style={{ fontSize: '1rem', marginBottom: '0.25rem' }}>{item.productName}</h4>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    Qty: {item.quantity} × ₹{item.price.toLocaleString('en-IN')}
                  </div>
                </div>

                <div style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-main)' }}>
                  ₹{item.totalPrice.toLocaleString('en-IN')}
                </div>
              </div>
            ))}
          </div>

          <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'flex-end' }}>
            <div style={{ width: '280px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>
                <span>Subtotal</span>
                <span>₹{order.totalAmount.toLocaleString('en-IN')}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>
                <span>Delivery</span>
                <span style={{ color: 'var(--success)', fontWeight: 600 }}>FREE</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem', fontWeight: 800, fontSize: '1.2rem' }}>
                <span>Total</span>
                <span style={{ color: 'var(--primary)' }}>₹{order.totalAmount.toLocaleString('en-IN')}</span>
              </div>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.5rem', border: '1px solid var(--border-color)' }}>
            <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.05rem', marginBottom: '0.75rem' }}>
              <MapPin size={18} color="var(--primary)" /> Delivery Address
            </h4>
            <p style={{ fontSize: '0.9rem', lineHeight: '1.6', color: 'var(--text-main)' }}>
              {order.shippingAddress}
            </p>
          </div>

          <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.5rem', border: '1px solid var(--border-color)' }}>
            <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.05rem', marginBottom: '0.75rem' }}>
              <CreditCard size={18} color="var(--primary)" /> Payment Details
            </h4>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Method:</span>
              <span style={{ fontWeight: 700 }}>Cash on Delivery (COD)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Status:</span>
              <span style={{ fontWeight: 700, color: order.paymentStatus === 'PAID' ? 'var(--success)' : 'var(--warning)' }}>
                {order.paymentStatus}
              </span>
            </div>
          </div>
        </div>
      </div>

      <Modal isOpen={showCancelModal} onClose={() => setShowCancelModal(false)} title="Cancel Order">
        <div style={{ textAlign: 'center', padding: '1rem 0' }}>
          <ShieldAlert size={48} color="var(--danger)" style={{ marginBottom: '1rem' }} />
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Are you sure you want to cancel?</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
            This will cancel Order #{order.id}. Items will be returned to inventory and you will not be charged.
          </p>
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
            <button className="btn btn-secondary" onClick={() => setShowCancelModal(false)}>
              Keep Order
            </button>
            <button className="btn btn-danger" onClick={handleCancelOrder} disabled={cancelling}>
              {cancelling ? 'Cancelling...' : 'Yes, Cancel Order'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
