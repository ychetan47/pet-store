import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save, Image as ImageIcon } from 'lucide-react';
import { adminProductsApi, adminCategoriesApi } from '../services/api';
import { Category, ProductStatus } from '../types';

export const ProductNewPage: React.FC = () => {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const [brand, setBrand] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [price, setPrice] = useState<number | ''>('');
  const [stockQuantity, setStockQuantity] = useState<number | ''>(10);
  const [weight, setWeight] = useState('');
  const [status, setStatus] = useState<ProductStatus>('ACTIVE');
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    adminCategoriesApi.getCategories().then(setCategories).catch(console.error);
  }, []);

  const handleNameChange = (val: string) => {
    setName(val);
    const generated = val
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9\s-]/g, '')
      .replace(/\s+/g, '-');
    setSlug(generated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !brand || price === '' || stockQuantity === '') {
      setError('Please fill in all mandatory fields (Name, Brand, Price, Stock).');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await adminProductsApi.createProduct({
        name,
        slug: slug || undefined,
        description: description || undefined,
        brand,
        categoryId: categoryId === '' ? undefined : Number(categoryId),
        price: Number(price),
        stockQuantity: Number(stockQuantity),
        weight: weight || undefined,
        status,
        imageUrl: imageUrl || undefined,
      });

      navigate('/products');
    } catch (err: any) {
      console.error('Failed to create product', err);
      setError(err.response?.data?.message || 'Failed to create product. Check that slug or name is unique.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
        <Link to="/products" className="btn btn-outline btn-sm">
          <ArrowLeft size={16} />
          <span>Back to Products</span>
        </Link>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Create New Product</h2>
      </div>

      {error && (
        <div style={{ background: 'var(--danger-light)', color: '#991b1b', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', border: '1px solid #fecaca' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ background: 'white', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Product Name *</label>
            <input
              type="text"
              required
              className="form-control"
              placeholder="e.g. Royal Canin Maxi Adult Dry Dog Food"
              value={name}
              onChange={(e) => handleNameChange(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Slug (URL identifier)</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. royal-canin-maxi-adult-dog-food"
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
            placeholder="Detailed product information, nutritional values, usage guidelines..."
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
              placeholder="e.g. Royal Canin, Pedigree, Whiskas"
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
              <option value="">-- Select Category --</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.parentName ? `${c.parentName} > ${c.name}` : c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Weight / Package Size</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. 3 kg, 500 g, Large"
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
              placeholder="999.00"
              value={price}
              onChange={(e) => setPrice(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Initial Stock Quantity *</label>
            <input
              type="number"
              min="0"
              required
              className="form-control"
              placeholder="50"
              value={stockQuantity}
              onChange={(e) => setStockQuantity(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Product Status</label>
            <select
              className="form-control"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProductStatus)}
            >
              <option value="ACTIVE">ACTIVE (Visible to customers)</option>
              <option value="INACTIVE">INACTIVE (Hidden in store)</option>
            </select>
          </div>
        </div>

        {/* Primary Image URL */}
        <div className="form-group">
          <label className="form-label">Primary Image URL</label>
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            <input
              type="url"
              className="form-control"
              placeholder="https://images.unsplash.com/photo-..."
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
            />
          </div>
          {imageUrl && (
            <div style={{ marginTop: '0.75rem', display: 'flex', alignItems: 'center', gap: '1rem', background: 'var(--bg-main)', padding: '0.75rem', borderRadius: 'var(--radius-md)' }}>
              <img
                src={imageUrl}
                alt="Preview"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
                style={{ width: 64, height: 64, objectFit: 'cover', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}
              />
              <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>Image preview</span>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '2rem' }}>
          <button
            type="button"
            className="btn btn-outline"
            onClick={() => navigate('/products')}
          >
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            <Save size={18} />
            <span>{loading ? 'Creating Product...' : 'Publish Product'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
