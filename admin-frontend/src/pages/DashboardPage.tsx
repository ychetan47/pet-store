import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Package,
  AlertTriangle,
  ShoppingCart,
  DollarSign,
  Clock,
  Truck,
  CheckCircle,
  PlusCircle,
  ArrowRight,
  ExternalLink,
} from 'lucide-react';
import { adminProductsApi, adminOrdersApi } from '../services/api';
import { Product } from '../types';

export const DashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [productStats, setProductStats] = useState<{
    totalProducts: number;
    lowStockCount: number;
    lowStockProducts: Product[];
  }>({ totalProducts: 0, lowStockCount: 0, lowStockProducts: [] });

  const [orderStats, setOrderStats] = useState<{
    totalOrders: number;
    pendingOrders: number;
    shippedOrders: number;
    deliveredOrders: number;
    cancelledOrders: number;
    totalRevenue: number;
  }>({
    totalOrders: 0,
    pendingOrders: 0,
    shippedOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0,
    totalRevenue: 0,
  });

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [prodRes, ordRes] = await Promise.all([
        adminProductsApi.getDashboardStats().catch(() => ({
          totalProducts: 0,
          lowStockCount: 0,
          lowStockProducts: [],
        })),
        adminOrdersApi.getDashboardStats().catch(() => ({
          totalOrders: 0,
          pendingOrders: 0,
          shippedOrders: 0,
          deliveredOrders: 0,
          cancelledOrders: 0,
          totalRevenue: 0,
        })),
      ]);
      setProductStats(prodRes);
      setOrderStats(ordRes);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '300px' }}>
        <div style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>Loading dashboard analytics...</div>
      </div>
    );
  }

  return (
    <div>
      {/* Quick Action Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Store Overview</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Real-time catalog and order metrics across microservices</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/products/new" className="btn btn-primary btn-sm">
            <PlusCircle size={16} />
            <span>Add Product</span>
          </Link>
          <Link to="/categories/new" className="btn btn-outline btn-sm">
            <PlusCircle size={16} />
            <span>Add Category</span>
          </Link>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-info">
            <h4>Total Products</h4>
            <div className="stat-value">{productStats.totalProducts}</div>
          </div>
          <div className="stat-icon-wrapper stat-icon-indigo">
            <Package size={24} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <h4>Low Stock Alert</h4>
            <div className="stat-value" style={{ color: productStats.lowStockCount > 0 ? 'var(--danger)' : 'inherit' }}>
              {productStats.lowStockCount}
            </div>
          </div>
          <div className="stat-icon-wrapper stat-icon-rose">
            <AlertTriangle size={24} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <h4>Total Orders</h4>
            <div className="stat-value">{orderStats.totalOrders}</div>
          </div>
          <div className="stat-icon-wrapper stat-icon-amber">
            <ShoppingCart size={24} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <h4>Total Revenue</h4>
            <div className="stat-value">₹{orderStats.totalRevenue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
          </div>
          <div className="stat-icon-wrapper stat-icon-emerald">
            <DollarSign size={24} />
          </div>
        </div>
      </div>

      {/* Order Status Breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Clock size={24} style={{ color: 'var(--info)' }} />
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>PENDING ORDERS</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{orderStats.pendingOrders}</div>
          </div>
        </div>
        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Truck size={24} style={{ color: 'var(--warning)' }} />
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>IN TRANSIT</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{orderStats.shippedOrders}</div>
          </div>
        </div>
        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <CheckCircle size={24} style={{ color: 'var(--success)' }} />
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>DELIVERED</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{orderStats.deliveredOrders}</div>
          </div>
        </div>
        <div style={{ background: 'white', padding: '1.25rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <AlertTriangle size={24} style={{ color: 'var(--danger)' }} />
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>CANCELLED</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{orderStats.cancelledOrders}</div>
          </div>
        </div>
      </div>

      {/* Low Stock Products Section */}
      <div className="card-table-wrapper">
        <div className="card-table-header">
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Low Stock Alert (&le; 5 units)</h3>
            <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>Items requiring immediate restocking to avoid checkout failures</p>
          </div>
          <Link to="/products" className="btn btn-outline btn-sm">
            <span>View All Products</span>
            <ArrowRight size={14} />
          </Link>
        </div>

        {productStats.lowStockProducts && productStats.lowStockProducts.length > 0 ? (
          <table className="data-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {productStats.lowStockProducts.map((prod) => (
                <tr key={prod.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <img
                        src={prod.primaryImageUrl || 'https://via.placeholder.com/40'}
                        alt={prod.name}
                        style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
                      />
                      <div>
                        <div style={{ fontWeight: 600 }}>{prod.name}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Brand: {prod.brand}</div>
                      </div>
                    </div>
                  </td>
                  <td>{prod.categoryName || '—'}</td>
                  <td>₹{prod.price.toFixed(2)}</td>
                  <td>
                    <span style={{ fontWeight: 700, color: 'var(--danger)' }}>
                      {prod.stockQuantity} left
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${prod.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}`}>
                      {prod.status}
                    </span>
                  </td>
                  <td>
                    <Link to={`/products/${prod.id}/edit`} className="btn btn-outline btn-sm">
                      <ExternalLink size={14} />
                      <span>Manage</span>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <CheckCircle size={36} style={{ color: 'var(--success)', marginBottom: '0.75rem', display: 'inline-block' }} />
            <div>All products are adequately stocked! No inventory warnings.</div>
          </div>
        )}
      </div>
    </div>
  );
};
