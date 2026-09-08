import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Eye, ChevronLeft, ChevronRight, Calendar, User, CreditCard } from 'lucide-react';
import { adminOrdersApi } from '../services/api';
import { Order, OrderStatus, PageResponse } from '../types';

const STATUS_TABS: { label: string; value?: OrderStatus }[] = [
  { label: 'All Orders' },
  { label: 'Placed', value: 'PLACED' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Shipped', value: 'SHIPPED' },
  { label: 'Delivered', value: 'DELIVERED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

export const OrdersPage: React.FC = () => {
  const [ordersPage, setOrdersPage] = useState<PageResponse<Order>>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const [activeTab, setActiveTab] = useState<OrderStatus | undefined>(undefined);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, [activeTab, currentPage]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const res = await adminOrdersApi.getOrders({
        status: activeTab,
        page: currentPage,
        size: 10,
      });
      setOrdersPage(res);
    } catch (err) {
      console.error('Failed to load orders', err);
    } finally {
      setLoading(false);
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

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Order Management</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          Track, fulfill, and update order statuses across the customer base
        </p>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', overflowX: 'auto', paddingBottom: '0.25rem' }}>
        {STATUS_TABS.map((tab) => {
          const isActive = activeTab === tab.value;
          return (
            <button
              key={tab.label}
              onClick={() => {
                setActiveTab(tab.value);
                setCurrentPage(0);
              }}
              style={{
                padding: '0.6rem 1.1rem',
                borderRadius: 'var(--radius-full)',
                border: '1px solid',
                borderColor: isActive ? 'var(--primary)' : 'var(--border)',
                background: isActive ? 'var(--primary)' : 'white',
                color: isActive ? 'white' : 'var(--text-main)',
                fontWeight: 600,
                fontSize: '0.85rem',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
              }}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Orders Table */}
      <div className="card-table-wrapper">
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading orders...
          </div>
        ) : ordersPage.content.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <ShoppingCart size={36} style={{ color: 'var(--text-light)', marginBottom: '0.75rem', display: 'inline-block' }} />
            <div>No orders found for this status.</div>
          </div>
        ) : (
          <>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Date & Time</th>
                  <th>Customer</th>
                  <th>Items</th>
                  <th>Total Amount</th>
                  <th>Payment</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {ordersPage.content.map((ord) => (
                  <tr key={ord.id}>
                    <td>
                      <span style={{ fontWeight: 800, color: 'var(--primary)' }}>#{ord.id}</span>
                    </td>
                    <td>
                      <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                        <Calendar size={13} />
                        <span>{new Date(ord.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>
                      </div>
                    </td>
                    <td>
                      <div>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{ord.shippingName}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{ord.shippingPhone}</div>
                      </div>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>
                        {ord.items?.length || 0} {ord.items?.length === 1 ? 'item' : 'items'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 800 }}>₹{ord.totalAmount.toFixed(2)}</span>
                    </td>
                    <td>
                      <div style={{ fontSize: '0.825rem' }}>
                        <span style={{ fontWeight: 600 }}>{ord.paymentMethod}</span>
                        <div style={{ fontSize: '0.725rem', color: ord.paymentStatus === 'COMPLETED' ? 'var(--success)' : 'var(--text-muted)' }}>
                          {ord.paymentStatus}
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${getStatusBadgeClass(ord.orderStatus)}`}>
                        {ord.orderStatus}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <Link to={`/orders/${ord.id}`} className="btn btn-outline btn-sm">
                        <Eye size={14} />
                        <span>Details</span>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Pagination */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.5rem', borderTop: '1px solid var(--border)' }}>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Showing page <strong>{ordersPage.page + 1}</strong> of <strong>{ordersPage.totalPages || 1}</strong> (
                {ordersPage.totalElements} total orders)
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  className="btn btn-outline btn-sm"
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                >
                  <ChevronLeft size={16} />
                  <span>Prev</span>
                </button>
                <button
                  className="btn btn-outline btn-sm"
                  disabled={ordersPage.last}
                  onClick={() => setCurrentPage((prev) => prev + 1)}
                >
                  <span>Next</span>
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
