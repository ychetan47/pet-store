import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save, Trash2, Plus, Star, Image as ImageIcon, Layers } from 'lucide-react';
import { adminProductsApi, adminCategoriesApi } from '../services/api';
import { Category, ProductDetail, ProductStatus } from '../types';

export const ProductEditPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const productId = Number(id);

  const [categories, setCategories] = useState<Category[]>([]);
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Form State
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const [brand, setBrand] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [price, setPrice] = useState<number | ''>('');
  const [stockQuantity, setStockQuantity] = useState<number | ''>(0);
  const [weight, setWeight] = useState('');
  const [status, setStatus] = useState<ProductStatus>('ACTIVE');

  // New Image sub-form
  const [newImageUrl, setNewImageUrl] = useState('');
  const [newImageIsPrimary, setNewImageIsPrimary] = useState(false);
  const [newImageOrder, setNewImageOrder] = useState(0);
  const [addingImage, setAddingImage] = useState(false);

  useEffect(() => {
    adminCategoriesApi.getCategories().then(setCategories).catch(console.error);
    loadProduct();
  }, [productId]);

  const loadProduct = async () => {
    try {
      setLoading(true);
      const data = await adminProductsApi.getProductById(productId);
      setProduct(data);
      setName(data.name);
      setSlug(data.slug);
      setDescription(data.description || '');
      setBrand(data.brand);
      setCategoryId(data.categoryId || '');
      setPrice(data.price);
      setStockQuantity(data.stockQuantity);
      setWeight(data.weight || '');
      setStatus(data.status);
    } catch (err: any) {
      console.error('Failed to load product', err);
      setError('Failed to fetch product details.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateDetails = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      setSuccessMsg(null);
      await adminProductsApi.updateProduct(productId, {
        name,
        slug,
        description: description || undefined,
        brand,
        categoryId: categoryId === '' ? undefined : Number(categoryId),
        price: Number(price),
        stockQuantity: Number(stockQuantity),
        weight: weight || undefined,
        status,
      });
      setSuccessMsg('Product details updated successfully!');
      loadProduct();
    } catch (err: any) {
      console.error('Failed to update product', err);
      setError(err.response?.data?.message || 'Failed to update product details.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddImage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newImageUrl.trim()) return;
    try {
      setAddingImage(true);
      await adminProductsApi.addImage(productId, newImageUrl.trim(), newImageIsPrimary, newImageOrder);
      setNewImageUrl('');
      setNewImageIsPrimary(false);
      setNewImageOrder(0);
      loadProduct();
    } catch (err: any) {
      console.error('Failed to add image', err);
      alert('Failed to add image.');
    } finally {
      setAddingImage(false);
    }
  };

  const handleDeleteImage = async (imageId: number) => {
    if (!window.confirm('Are you sure you want to remove this image?')) return;
    try {
      await adminProductsApi.deleteImage(productId, imageId);
      loadProduct();
    } catch (err) {
      console.error('Failed to remove image', err);
      alert('Failed to delete image.');
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        Loading product details...
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Link to="/products" className="btn btn-outline btn-sm">
            <ArrowLeft size={16} />
            <span>Back to Products</span>
          </Link>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Edit Product: #{productId}</h2>
        </div>
      </div>

      {error && (
        <div style={{ background: 'var(--danger-light)', color: '#991b1b', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', border: '1px solid #fecaca' }}>
          {error}
        </div>
      )}

      {successMsg && (
        <div style={{ background: 'var(--success-light)', color: '#065f46', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', border: '1px solid #a7f3d0' }}>
          {successMsg}
        </div>
      )}

      {/* Main Details Form */}
      <form onSubmit={handleUpdateDetails} style={{ background: 'white', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)', marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.25rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.75rem' }}>
          Basic Information
        </h3>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Product Name *</label>
            <input
              type="text"
              required
              className="form-control"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Slug</label>
            <input
              type="text"
              required
              className="form-control"
              value={slug}
              onChange={(e) => setSlug(e.target.value)}
            />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea
            rows={4}
            className="form-control"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Brand *</label>
            <input
              type="text"
              required
              className="form-control"
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Category</label>
            <select
              className="form-control"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">-- None --</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.parentName ? `${c.parentName} > ${c.name}` : c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Weight / Size</label>
            <input
              type="text"
              className="form-control"
              value={weight}
              onChange={(e) => setWeight(e.target.value)}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Price (₹) *</label>
            <input
              type="number"
              step="0.01"
              min="0"
              required
              className="form-control"
              value={price}
              onChange={(e) => setPrice(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Stock Quantity *</label>
            <input
              type="number"
              min="0"
              required
              className="form-control"
              value={stockQuantity}
              onChange={(e) => setStockQuantity(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Status</label>
            <select
              className="form-control"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProductStatus)}
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            <Save size={16} />
            <span>{saving ? 'Saving...' : 'Save Product Details'}</span>
          </button>
        </div>
      </form>

      {/* Image Gallery Management */}
      <div style={{ background: 'white', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.25rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <ImageIcon size={20} />
          <span>Product Image Gallery (PostgreSQL URLs)</span>
        </h3>

        {/* Existing Images */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
          {product?.images && product.images.length > 0 ? (
            product.images.map((img) => (
              <div
                key={img.id}
                style={{
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius-md)',
                  padding: '0.5rem',
                  position: 'relative',
                  backgroundColor: 'var(--bg-main)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.5rem',
                }}
              >
                <img
                  src={img.imageUrl}
                  alt="Product item"
                  style={{ width: '100%', height: '140px', objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
                />
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem' }}>
                  {img.isPrimary ? (
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem', color: 'var(--primary)', fontWeight: 700 }}>
                      <Star size={12} fill="currentColor" /> Primary
                    </span>
                  ) : (
                    <span style={{ color: 'var(--text-muted)' }}>Order: {img.displayOrder}</span>
                  )}
                  <button
                    onClick={() => handleDeleteImage(img.id)}
                    style={{ background: 'transparent', border: 'none', color: 'var(--danger)', cursor: 'pointer', padding: '0.2rem' }}
                    title="Remove image"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div style={{ gridColumn: '1 / -1', padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No images registered for this product.
            </div>
          )}
        </div>

        {/* Add New Image Form */}
        <div style={{ background: 'var(--bg-main)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '1rem' }}>Add Image URL</h4>
          <form onSubmit={handleAddImage} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div style={{ flex: 2, minWidth: '220px' }}>
              <label className="form-label">Image URL</label>
              <input
                type="url"
                required
                placeholder="https://images.unsplash.com/..."
                className="form-control"
                value={newImageUrl}
                onChange={(e) => setNewImageUrl(e.target.value)}
              />
            </div>
            <div style={{ width: '100px' }}>
              <label className="form-label">Display Order</label>
              <input
                type="number"
                min="0"
                className="form-control"
                value={newImageOrder}
                onChange={(e) => setNewImageOrder(Number(e.target.value))}
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.6rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 600 }}>
                <input
                  type="checkbox"
                  checked={newImageIsPrimary}
                  onChange={(e) => setNewImageIsPrimary(e.target.checked)}
                />
                Primary Image
              </label>
            </div>
            <div style={{ marginBottom: '0.35rem' }}>
              <button type="submit" className="btn btn-outline btn-sm" disabled={addingImage}>
                <Plus size={16} />
                <span>{addingImage ? 'Adding...' : 'Add Image'}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
