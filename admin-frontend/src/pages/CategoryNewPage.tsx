import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { adminCategoriesApi } from '../services/api';
import { Category } from '../types';

export const CategoryNewPage: React.FC = () => {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<Category[]>([]);
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [parentId, setParentId] = useState<number | ''>('');
  const [imageUrl, setImageUrl] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
    if (!name.trim()) return;

    try {
      setLoading(true);
      setError(null);
      await adminCategoriesApi.createCategory({
        name: name.trim(),
        slug: slug.trim() || undefined,
        parentId: parentId === '' ? undefined : Number(parentId),
        imageUrl: imageUrl.trim() || undefined,
        isActive,
      });
      navigate('/categories');
    } catch (err: any) {
      console.error('Failed to create category', err);
      setError(err.response?.data?.message || 'Failed to create category.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '650px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
        <Link to="/categories" className="btn btn-outline btn-sm">
          <ArrowLeft size={16} />
          <span>Back to Categories</span>
        </Link>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Create Category</h2>
      </div>

      {error && (
        <div style={{ background: 'var(--danger-light)', color: '#991b1b', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ background: 'white', padding: '2rem', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
        <div className="form-group">
          <label className="form-label">Category Name *</label>
          <input
            type="text"
            required
            className="form-control"
            placeholder="e.g. Dog Food, Puppy Toys"
            value={name}
            onChange={(e) => handleNameChange(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Slug</label>
          <input
            type="text"
            className="form-control"
            placeholder="e.g. dog-food"
            value={slug}
            onChange={(e) => setSlug(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Parent Category</label>
          <select
            className="form-control"
            value={parentId}
            onChange={(e) => setParentId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">-- None (Top Level Category) --</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.parentName ? `${c.parentName} > ${c.name}` : c.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Image URL</label>
          <input
            type="url"
            className="form-control"
            placeholder="https://images.unsplash.com/..."
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
          />
          {imageUrl && (
            <div style={{ marginTop: '0.75rem' }}>
              <img
                src={imageUrl}
                alt="Preview"
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
                style={{ width: 64, height: 64, objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
              />
            </div>
          )}
        </div>

        <div className="form-group" style={{ marginTop: '1.25rem' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontWeight: 600 }}>
            <input
              type="checkbox"
              checked={isActive}
              onChange={(e) => setIsActive(e.target.checked)}
            />
            Category is Active
          </label>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '2rem' }}>
          <button type="button" className="btn btn-outline" onClick={() => navigate('/categories')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            <Save size={16} />
            <span>{loading ? 'Creating...' : 'Create Category'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
