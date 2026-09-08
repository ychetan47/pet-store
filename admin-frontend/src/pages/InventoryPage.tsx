import React, { useEffect, useState } from 'react';
import {
  Search,
  RefreshCw,
  Edit2,
  Boxes,
  AlertTriangle,
  CheckCircle2,
  Clock,
  X,
  Check,
} from 'lucide-react';
import { adminInventoryApi, adminProductsApi } from '../services/api';
import { InventoryItem, Product } from '../types';

export const InventoryPage: React.FC = () => {
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([]);
  const [productsMap, setProductsMap] = useState<Record<number, Product>>({});
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [stockFilter, setStockFilter] = useState<'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'RESERVED'>('ALL');

  // Edit Stock Modal
  const [editingItem, setEditingItem] = useState<InventoryItem | null>(null);
  const [newTotalStock, setNewTotalStock] = useState<number>(0);
  const [updating, setUpdating] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [items, productsPage] = await Promise.all([
        adminInventoryApi.getInventory(),
        adminProductsApi.getProducts({ size: 100 }),
      ]);
      setInventoryItems(items);

      const map: Record<number, Product> = {};
      if (productsPage && productsPage.content) {
        productsPage.content.forEach((p) => {
          map[p.id] = p;
        });
      }
      setProductsMap(map);
    } catch (err) {
      console.error('Failed to load inventory data', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  const handleOpenEdit = (item: InventoryItem) => {
    setEditingItem(item);
    setNewTotalStock(item.totalQuantity);
    setErrorMsg(null);
  };

  const handleSaveStock = async () => {
    if (!editingItem) return;
    if (newTotalStock < editingItem.reservedQuantity) {
      setErrorMsg(`Total stock cannot be less than currently reserved quantity (${editingItem.reservedQuantity})`);
      return;
    }

    try {
      setUpdating(true);
      const updated = await adminInventoryApi.updateStock(editingItem.productId, newTotalStock);
      setInventoryItems((prev) =>
        prev.map((it) => (it.productId === updated.productId ? updated : it))
      );
      setEditingItem(null);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to update stock';
      setErrorMsg(msg);
    } finally {
      setUpdating(false);
    }
  };

  // Metrics
  const totalSkus = inventoryItems.length;
  const totalStockInWarehouse = inventoryItems.reduce((acc, it) => acc + it.totalQuantity, 0);
  const totalReserved = inventoryItems.reduce((acc, it) => acc + it.reservedQuantity, 0);
  const lowStockCount = inventoryItems.filter((it) => it.availableQuantity > 0 && it.availableQuantity <= 5).length;
  const outOfStockCount = inventoryItems.filter((it) => it.availableQuantity === 0).length;

  // Filtered List
  const filteredItems = inventoryItems.filter((item) => {
    const product = productsMap[item.productId];
    const nameMatch = product ? product.name.toLowerCase().includes(searchTerm.toLowerCase()) : false;
    const idMatch = item.productId.toString().includes(searchTerm);

    if (searchTerm && !nameMatch && !idMatch) {
      return false;
    }

    if (stockFilter === 'OUT_OF_STOCK') return item.availableQuantity === 0;
    if (stockFilter === 'LOW_STOCK') return item.availableQuantity > 0 && item.availableQuantity <= 5;
    if (stockFilter === 'IN_STOCK') return item.availableQuantity > 5;
    if (stockFilter === 'RESERVED') return item.reservedQuantity > 0;

    return true;
  });

  return (
    <div className="admin-page">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: 0 }}>Inventory Management</h1>
          <p style={{ color: '#64748b', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            Real-time multi-service stock tracking with transactional reservation &amp; KRaft Kafka sync.
          </p>
        </div>

        <button
          className="btn btn-secondary"
          onClick={handleRefresh}
          disabled={refreshing || loading}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
          {refreshing ? 'Refreshing...' : 'Refresh Stock'}
        </button>
      </div>

      {/* Metrics Row */}
      <div className="stats-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <div className="stat-card" style={{ background: '#fff', padding: '1.25rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#64748b', fontSize: '0.85rem' }}>
            <Boxes size={18} color="#3b82f6" />
            <span>Tracked SKUs</span>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 700, marginTop: '0.5rem', color: '#0f172a' }}>{totalSkus}</div>
        </div>

        <div className="stat-card" style={{ background: '#fff', padding: '1.25rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#64748b', fontSize: '0.85rem' }}>
            <CheckCircle2 size={18} color="#10b981" />
            <span>Total Units In Stock</span>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 700, marginTop: '0.5rem', color: '#10b981' }}>{totalStockInWarehouse}</div>
        </div>

        <div className="stat-card" style={{ background: '#fff', padding: '1.25rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#64748b', fontSize: '0.85rem' }}>
            <Clock size={18} color="#f59e0b" />
            <span>Active Reservations</span>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 700, marginTop: '0.5rem', color: '#f59e0b' }}>{totalReserved}</div>
        </div>

        <div className="stat-card" style={{ background: '#fff', padding: '1.25rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#64748b', fontSize: '0.85rem' }}>
            <AlertTriangle size={18} color="#ef4444" />
            <span>Low / Out of Stock</span>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 700, marginTop: '0.5rem', color: outOfStockCount > 0 ? '#ef4444' : '#64748b' }}>
            {lowStockCount + outOfStockCount} <span style={{ fontSize: '0.85rem', fontWeight: 400 }}>({outOfStockCount} out of stock)</span>
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="table-controls" style={{ background: '#fff', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0', marginBottom: '1rem', display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: '1 1 300px' }}>
          <div style={{ position: 'relative', width: '100%' }}>
            <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
            <input
              type="text"
              placeholder="Search by product name or product ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                width: '100%',
                padding: '0.55rem 1rem 0.55rem 2.4rem',
                border: '1px solid #cbd5e1',
                borderRadius: '8px',
                fontSize: '0.9rem',
              }}
            />
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          {(['ALL', 'IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'RESERVED'] as const).map((filter) => (
            <button
              key={filter}
              onClick={() => setStockFilter(filter)}
              style={{
                padding: '0.45rem 0.85rem',
                borderRadius: '6px',
                border: '1px solid',
                borderColor: stockFilter === filter ? '#2563eb' : '#cbd5e1',
                backgroundColor: stockFilter === filter ? '#eff6ff' : '#fff',
                color: stockFilter === filter ? '#2563eb' : '#475569',
                fontSize: '0.85rem',
                fontWeight: stockFilter === filter ? 600 : 500,
                cursor: 'pointer',
              }}
            >
              {filter.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Inventory Table */}
      <div className="table-container" style={{ background: '#fff', borderRadius: '10px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '4rem 0', color: '#64748b' }}>
            <RefreshCw size={28} className="animate-spin" style={{ margin: '0 auto 1rem' }} />
            <p>Loading inventory records from Inventory Service...</p>
          </div>
        ) : filteredItems.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '4rem 0', color: '#64748b' }}>
            <Boxes size={40} style={{ margin: '0 auto 1rem', color: '#94a3b8' }} />
            <p style={{ fontSize: '1.05rem', fontWeight: 600 }}>No inventory records match criteria</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <th style={{ padding: '0.85rem 1.25rem' }}>Product ID</th>
                <th style={{ padding: '0.85rem 1.25rem' }}>Product Name</th>
                <th style={{ padding: '0.85rem 1.25rem' }}>Available Stock</th>
                <th style={{ padding: '0.85rem 1.25rem' }}>Reserved Stock</th>
                <th style={{ padding: '0.85rem 1.25rem' }}>Total Stock</th>
                <th style={{ padding: '0.85rem 1.25rem' }}>Status</th>
                <th style={{ padding: '0.85rem 1.25rem', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.map((item) => {
                const product = productsMap[item.productId];
                const isOut = item.availableQuantity === 0;
                const isLow = item.availableQuantity > 0 && item.availableQuantity <= 5;

                return (
                  <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background 0.15s' }}>
                    <td style={{ padding: '1rem 1.25rem', fontWeight: 600, color: '#334155' }}>
                      #{item.productId}
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <div style={{ fontWeight: 600, color: '#0f172a' }}>
                        {product ? product.name : `Product #${item.productId}`}
                      </div>
                      {product && (
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>
                          {product.brand} &bull; ₹{product.price.toLocaleString('en-IN')}
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        style={{
                          fontWeight: 700,
                          fontSize: '1rem',
                          color: isOut ? '#ef4444' : isLow ? '#f59e0b' : '#10b981',
                        }}
                      >
                        {item.availableQuantity}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        style={{
                          fontWeight: 600,
                          color: item.reservedQuantity > 0 ? '#f59e0b' : '#94a3b8',
                        }}
                      >
                        {item.reservedQuantity}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', fontWeight: 600, color: '#334155' }}>
                      {item.totalQuantity}
                    </td>
                    <td style={{ padding: '1rem 1.25rem' }}>
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '0.25rem 0.6rem',
                          borderRadius: '9999px',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          backgroundColor: isOut ? '#fee2e2' : isLow ? '#fef3c7' : '#dcfce7',
                          color: isOut ? '#b91c1c' : isLow ? '#b45309' : '#15803d',
                        }}
                      >
                        {isOut ? 'Out of Stock' : isLow ? 'Low Stock' : 'In Stock'}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => handleOpenEdit(item)}
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}
                      >
                        <Edit2 size={13} />
                        Adjust Stock
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Quick Stock Adjustment Modal */}
      {editingItem && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
        >
          <div
            style={{
              backgroundColor: '#fff',
              borderRadius: '12px',
              width: '100%',
              maxWidth: '440px',
              padding: '1.75rem',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ margin: 0, fontSize: '1.2rem', fontWeight: 700 }}>Adjust Warehouse Stock</h3>
              <button
                onClick={() => setEditingItem(null)}
                style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#94a3b8' }}
              >
                <X size={20} />
              </button>
            </div>

            <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '1.25rem' }}>
              Updating total inventory for <strong>{productsMap[editingItem.productId]?.name || `Product #${editingItem.productId}`}</strong>.
            </p>

            <div style={{ backgroundColor: '#f8fafc', padding: '1rem', borderRadius: '8px', marginBottom: '1.25rem', fontSize: '0.85rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                <span style={{ color: '#64748b' }}>Currently Available:</span>
                <span style={{ fontWeight: 600 }}>{editingItem.availableQuantity}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem' }}>
                <span style={{ color: '#64748b' }}>Currently Reserved:</span>
                <span style={{ fontWeight: 600, color: '#f59e0b' }}>{editingItem.reservedQuantity}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #e2e8f0', paddingTop: '0.4rem' }}>
                <span style={{ color: '#64748b' }}>Current Total:</span>
                <span style={{ fontWeight: 700 }}>{editingItem.totalQuantity}</span>
              </div>
            </div>

            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#334155', marginBottom: '0.5rem' }}>
                New Total Stock
              </label>
              <input
                type="number"
                min={editingItem.reservedQuantity}
                value={newTotalStock}
                onChange={(e) => setNewTotalStock(Math.max(0, parseInt(e.target.value) || 0))}
                style={{
                  width: '100%',
                  padding: '0.65rem 0.85rem',
                  border: '1px solid #cbd5e1',
                  borderRadius: '8px',
                  fontSize: '1rem',
                  fontWeight: 600,
                }}
              />
              <span style={{ display: 'block', fontSize: '0.75rem', color: '#64748b', marginTop: '0.35rem' }}>
                New available will be: {Math.max(0, newTotalStock - editingItem.reservedQuantity)} units
              </span>
            </div>

            {errorMsg && (
              <div style={{ backgroundColor: '#fee2e2', color: '#b91c1c', padding: '0.75rem', borderRadius: '6px', fontSize: '0.85rem', marginBottom: '1rem' }}>
                {errorMsg}
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                className="btn btn-secondary"
                onClick={() => setEditingItem(null)}
                disabled={updating}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleSaveStock}
                disabled={updating}
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
              >
                {updating ? 'Saving...' : <><Check size={16} /> Update Stock</>}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
