import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit2, Trash2, FolderTree } from 'lucide-react';
import { adminCategoriesApi } from '../services/api';
import { Category } from '../types';

export const CategoriesPage: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const data = await adminCategoriesApi.getCategories();
      setCategories(data);
    } catch (err) {
      console.error('Failed to load categories', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number, name: string) => {
    if (!window.confirm(`Are you sure you want to delete category "${name}"? Subcategories or products linked to this category may prevent deletion.`)) {
      return;
    }
    try {
      await adminCategoriesApi.deleteCategory(id);
      loadCategories();
    } catch (err: any) {
      console.error('Failed to delete category', err);
      alert(err.response?.data?.message || 'Failed to delete category. It may have child subcategories or products.');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Categories</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Manage category hierarchy for Dogs, Cats, food, and accessories
          </p>
        </div>
        <Link to="/categories/new" className="btn btn-primary">
          <Plus size={18} />
          <span>Add New Category</span>
        </Link>
      </div>

      <div className="card-table-wrapper">
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading categories...
          </div>
        ) : categories.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No categories created yet.
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Category</th>
                <th>Slug</th>
                <th>Parent Hierarchy</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat) => (
                <tr key={cat.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      {cat.imageUrl ? (
                        <img
                          src={cat.imageUrl}
                          alt={cat.name}
                          style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 'var(--radius-md)' }}
                        />
                      ) : (
                        <div
                          style={{
                            width: 40,
                            height: 40,
                            background: 'var(--primary-light)',
                            color: 'var(--primary)',
                            borderRadius: 'var(--radius-md)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <FolderTree size={20} />
                        </div>
                      )}
                      <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>{cat.name}</span>
                    </div>
                  </td>
                  <td>
                    <code style={{ background: '#f1f5f9', padding: '0.2rem 0.4rem', borderRadius: '4px', fontSize: '0.825rem' }}>
                      {cat.slug}
                    </code>
                  </td>
                  <td>
                    {cat.parentName ? (
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                        {cat.parentName} &rarr; <strong>{cat.name}</strong>
                      </span>
                    ) : (
                      <span style={{ color: 'var(--primary)', fontWeight: 600, fontSize: '0.825rem' }}>
                        Top Level Root
                      </span>
                    )}
                  </td>
                  <td>
                    <span className={`badge ${cat.isActive ? 'badge-active' : 'badge-inactive'}`}>
                      {cat.isActive ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <Link to={`/categories/${cat.id}/edit`} className="btn btn-outline btn-sm">
                        <Edit2 size={14} />
                        <span>Edit</span>
                      </Link>
                      <button
                        onClick={() => handleDelete(cat.id, cat.name)}
                        className="btn btn-outline btn-sm"
                        style={{ color: 'var(--danger)' }}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};
