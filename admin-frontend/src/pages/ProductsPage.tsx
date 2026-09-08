import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Plus,
  Search,
  Edit2,
  Trash2,
  Layers,
  ChevronLeft,
  ChevronRight,
  X,
  Check,
} from 'lucide-react';
import { adminProductsApi, adminCategoriesApi } from '../services/api';
import { Product, Category, PageResponse } from '../types';

export const ProductsPage: React.FC = () => {
  const [productsPage, setProductsPage] = useState<PageResponse<Product>>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>(undefined);
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);

  // Quick Stock Modal
  const [stockModalProduct, setStockModalProduct] = useState<Product | null>(null);
  const [newStockVal, setNewStockVal] = useState<number>(0);
  const [updatingStock, setUpdatingStock] = useState(false);

  useEffect(() => {
    adminCategoriesApi.getCategories().then(setCategories).catch(console.error);
  }, []);

  useEffect(() => {
    loadProducts();
  }, [currentPage, selectedCategory, selectedStatus]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const res = await adminProductsApi.getProducts({
        page: currentPage,
        size: 10,
        categoryId: selectedCategory,
        status: selectedStatus || undefined,
        search: searchTerm || undefined,
      });
      setProductsPage(res);
    } catch (err) {
      console.error('Failed to load products', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    loadProducts();
  };

  const handleDelete = async (productId: number, name: string) => {
    if (!window.confirm(`Are you sure you want to delete product "${name}"?`)) {
      return;
    }
    try {
      await adminProductsApi.deleteProduct(productId);
      loadProducts();
    } catch (err) {
      console.error('Failed to delete product', err);
      alert('Failed to delete product. It may have associated orders.');
    }
  };

  const handleOpenStockModal = (prod: Product) => {
    setStockModalProduct(prod);
    setNewStockVal(prod.stockQuantity);
  };

  const handleSaveStock = async () => {
    if (!stockModalProduct) return;
    try {
      setUpdatingStock(true);
      await adminProductsApi.updateStock(stockModalProduct.id, newStockVal);
      setStockModalProduct(null);
      loadProducts();
    } catch (err) {
      console.error('Failed to update stock', err);
      alert('Failed to update stock quantity.');
    } finally {
      setUpdatingStock(false);
    }
  };

  return (
    <div>
      {/* Header bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Products</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Manage inventory, pricing, status, and images across the catalog
          </p>
        </div>
        <Link to="/products/new" className="btn btn-primary">
          <Plus size={18} />
          <span>Add New Product</span>
        </Link>
      </div>

      {/* Filter toolbar */}
      <div style={{ background: 'white', padding: '1rem 1.25rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', marginBottom: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', flex: 1, minWidth: '240px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <input
              type="text"
              placeholder="Search by title, brand, or SKU..."
              className="form-control"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ paddingLeft: '2.5rem' }}
            />
            <Search size={18} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          </div>
          <button type="submit" className="btn btn-outline">
            Filter
          </button>
        </form>

        <select
          className="form-control"
          style={{ width: 'auto', minWidth: '180px' }}
          value={selectedCategory || ''}
          onChange={(e) => {
            setSelectedCategory(e.target.value ? Number(e.target.value) : undefined);
            setCurrentPage(0);
          }}
        >
          <option value="">All Categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.parentName ? `${c.parentName} > ${c.name}` : c.name}
            </option>
          ))}
        </select>

        <select
          className="form-control"
          style={{ width: 'auto', minWidth: '140px' }}
          value={selectedStatus}
          onChange={(e) => {
            setSelectedStatus(e.target.value);
            setCurrentPage(0);
          }}
        >
          <option value="">All Statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>

      {/* Table */}
      <div className="card-table-wrapper">
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading products...
          </div>
        ) : productsPage.content.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No products match the selected criteria.
          </div>
        ) : (
          <>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {productsPage.content.map((prod) => (
                  <tr key={prod.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <img
                          src={prod.primaryImageUrl || 'https://via.placeholder.com/48'}
                          alt={prod.name}
                          style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)' }}
                        />
                        <div>
                          <div style={{ fontWeight: 700, color: 'var(--text-main)' }}>{prod.name}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            Brand: {prod.brand} {prod.weight ? `• ${prod.weight}` : ''}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.85rem', color: 'var(--text-main)', fontWeight: 500 }}>
                        {prod.categoryName || '—'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 700 }}>₹{prod.price.toFixed(2)}</span>
                    </td>
                    <td>
                      <button
                        onClick={() => handleOpenStockModal(prod)}
                        title="Click to quickly update stock"
                        style={{
                          background: prod.stockQuantity <= 5 ? 'var(--danger-light)' : 'var(--bg-main)',
                          color: prod.stockQuantity <= 5 ? 'var(--danger)' : 'var(--text-main)',
                          border: '1px solid var(--border)',
                          borderRadius: 'var(--radius-sm)',
                          padding: '0.2rem 0.6rem',
                          fontSize: '0.825rem',
                          fontWeight: 700,
                          cursor: 'pointer',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                        }}
                      >
                        <Layers size={13} />
                        <span>{prod.stockQuantity} units</span>
                      </button>
                    </td>
                    <td>
                      <span className={`badge ${prod.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}`}>
                        {prod.status}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                        <Link
                          to={`/products/${prod.id}/edit`}
                          className="btn btn-outline btn-sm"
                          title="Edit Details & Images"
                        >
                          <Edit2 size={14} />
                          <span>Edit</span>
                        </Link>
                        <button
                          onClick={() => handleDelete(prod.id, prod.name)}
                          className="btn btn-outline btn-sm"
                          style={{ color: 'var(--danger)', borderColor: 'var(--border)' }}
                          title="Delete Product"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Pagination */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.5rem', borderTop: '1px solid var(--border)' }}>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Showing page <strong>{productsPage.page + 1}</strong> of <strong>{productsPage.totalPages || 1}</strong> (
                {productsPage.totalElements} total products)
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
                  disabled={productsPage.last}
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

      {/* Quick Stock Modal */}
      {stockModalProduct && (
        <div className="modal-overlay" onClick={() => setStockModalProduct(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Quick Update Stock</h3>
              <button
                onClick={() => setStockModalProduct(null)}
                style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
              >
                <X size={20} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ marginBottom: '1rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                Adjust current inventory count for <strong>{stockModalProduct.name}</strong>:
              </p>
              <div className="form-group">
                <label className="form-label">Available Stock Quantity</label>
                <input
                  type="number"
                  min="0"
                  className="form-control"
                  value={newStockVal}
                  onChange={(e) => setNewStockVal(Number(e.target.value))}
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline btn-sm" onClick={() => setStockModalProduct(null)}>
                Cancel
              </button>
              <button
                className="btn btn-primary btn-sm"
                disabled={updatingStock}
                onClick={handleSaveStock}
              >
                <Check size={16} />
                <span>{updatingStock ? 'Saving...' : 'Update Inventory'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
