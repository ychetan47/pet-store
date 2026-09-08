import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, Check } from 'lucide-react';
import { ProductSummary } from '../../types';
import { useCart } from '../../context/CartContext';

interface ProductCardProps {
  product: ProductSummary;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { addToCart } = useCart();
  const [adding, setAdding] = useState(false);
  const [justAdded, setJustAdded] = useState(false);

  const isAvailable = product.inStock !== undefined ? product.inStock : (product.stockQuantity > 0);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (!isAvailable || adding) return;

    try {
      setAdding(true);
      await addToCart(product.id, 1);
      setJustAdded(true);
      setTimeout(() => setJustAdded(false), 1500);
    } catch (e) {
      // toast shown in context
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="product-card">
      <Link to={`/products/${product.id}`} className="product-img-wrap">
        <img
          src={product.primaryImageUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80'}
          alt={product.name}
          loading="lazy"
        />
        <span className="product-brand-tag">{product.brand}</span>
        <span
          className={`product-stock-badge ${
            isAvailable ? 'badge-in-stock' : 'badge-out-stock'
          }`}
        >
          {isAvailable ? 'In Stock' : 'Out of Stock'}
        </span>
      </Link>

      <div className="product-info">
        <span className="product-category-name">{product.categoryName}</span>
        <Link to={`/products/${product.id}`}>
          <h3 className="product-title" title={product.name}>
            {product.name}
          </h3>
        </Link>
        {product.weight && <div className="product-weight">Weight: {product.weight}</div>}

        <div className="product-footer">
          <div className="product-price">₹{product.price.toLocaleString('en-IN')}</div>
          <button
            className={`btn btn-sm ${justAdded ? 'btn-secondary' : 'btn-primary'}`}
            disabled={!isAvailable || adding}
            onClick={handleAddToCart}
            style={{ minWidth: '95px' }}
          >
            {justAdded ? (
              <>
                <Check size={14} color="var(--success)" /> Added
              </>
            ) : (
              <>
                <ShoppingBag size={14} /> Add
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
