import React from 'react';
import { ProductSummary } from '../../types';
import { ProductCard } from './ProductCard';
import { PackageOpen } from 'lucide-react';

interface ProductGridProps {
  products: ProductSummary[];
  loading?: boolean;
}

export const ProductGrid: React.FC<ProductGridProps> = ({ products, loading }) => {
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 0' }}>
        <div style={{ display: 'inline-block', width: '40px', height: '40px', border: '3px solid var(--border-color)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Fetching products...</p>
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '5rem 1rem', background: '#fff', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)' }}>
        <PackageOpen size={54} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
        <h3>No Products Found</h3>
        <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>
          Try selecting a different category or clearing search filters.
        </p>
      </div>
    );
  }

  return (
    <div className="product-grid">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};
