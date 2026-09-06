import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ProductDetail, ProductImage } from '../types';
import { productService } from '../services/productService';
import { useCart } from '../context/CartContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { ShoppingBag, Check, ShieldCheck, Truck, RefreshCw, AlertCircle } from 'lucide-react';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { addToCart } = useCart();

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [quantity, setQuantity] = useState<number>(1);
  const [adding, setAdding] = useState<boolean>(false);
  const [justAdded, setJustAdded] = useState<boolean>(false);

  useEffect(() => {
    if (!id) return;
    const fetchDetail = async () => {
      try {
        setLoading(true);
        const data = await productService.getProductById(Number(id));
        setProduct(data);
        if (data.images && data.images.length > 0) {
          const primary = data.images.find((img: ProductImage) => img.primary) || data.images[0];
          setSelectedImage(primary.imageUrl);
        }
      } catch (err) {
        console.error('Failed to load product detail', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  const handleAddToCart = async () => {
    if (!product || !product.inStock || adding) return;
    try {
      setAdding(true);
      await addToCart(product.id, quantity);
      setJustAdded(true);
      setTimeout(() => setJustAdded(false), 2000);
    } catch (e) {
      // toast handled in context
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <div style={{ display: 'inline-block', width: '45px', height: '45px', border: '3px solid var(--border-color)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading product details...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <AlertCircle size={48} color="var(--danger)" style={{ marginBottom: '1rem' }} />
        <h2>Product Not Found</h2>
        <p style={{ color: 'var(--text-muted)', margin: '1rem 0' }}>
          The product you are looking for might be unavailable or removed.
        </p>
        <Link to="/products" className="btn btn-primary">
          Back to Catalog
        </Link>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'Products', path: '/products' },
    ...product.categoryBreadcrumbs.map((crumb: string) => ({ label: crumb })),
    { label: product.name },
  ];

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      <Breadcrumb items={breadcrumbItems} />

      <div className="product-detail-layout">
        {/* Left: Gallery */}
        <div className="gallery-container">
          <div className="main-image-wrap">
            <img
              src={selectedImage || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=1000&q=80'}
              alt={product.name}
            />
          </div>

          {/* Thumbnails */}
          {product.images && product.images.length > 1 && (
            <div className="thumbnail-strip">
              {product.images.map((img: ProductImage) => (
                <div
                  key={img.id}
                  className={`thumbnail-item ${selectedImage === img.imageUrl ? 'active' : ''}`}
                  onClick={() => setSelectedImage(img.imageUrl)}
                >
                  <img src={img.imageUrl} alt="" />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right: Product Details & Purchase Form */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span className="detail-brand">{product.brand}</span>
          <h1 className="detail-title">{product.name}</h1>

          {/* Category path badge */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Category:</span>
            <span style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--primary)', backgroundColor: 'var(--primary-light)', padding: '0.2rem 0.6rem', borderRadius: '4px' }}>
              {product.categoryBreadcrumbs.join(' > ')}
            </span>
          </div>

          <div className="detail-price">₹{product.price.toLocaleString('en-IN')}</div>

          <p className="detail-description">{product.description}</p>

          <div className="detail-meta-grid">
            <div className="meta-item">
              <div className="meta-label">Stock Status</div>
              <div
                className="meta-value"
                style={{ color: product.inStock ? 'var(--success)' : 'var(--danger)' }}
              >
                {product.inStock ? `In Stock (${product.stockQuantity} available)` : 'Out of Stock'}
              </div>
            </div>

            {product.weight && (
              <div className="meta-item">
                <div className="meta-label">Package Weight / Size</div>
                <div className="meta-value">{product.weight}</div>
              </div>
            )}
          </div>

          {/* Quantity selector & Add to Cart action */}
          {product.inStock ? (
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '2.5rem' }}>
              <div className="quantity-picker">
                <button
                  type="button"
                  className="qty-btn"
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                >
                  -
                </button>
                <span className="qty-value">{quantity}</span>
                <button
                  type="button"
                  className="qty-btn"
                  onClick={() => setQuantity((q) => Math.min(product.stockQuantity, q + 1))}
                >
                  +
                </button>
              </div>

              <button
                className="btn btn-primary btn-lg"
                style={{ flex: 1 }}
                disabled={adding}
                onClick={handleAddToCart}
              >
                {justAdded ? (
                  <>
                    <Check size={20} /> Added to Cart!
                  </>
                ) : (
                  <>
                    <ShoppingBag size={20} /> Add to Cart
                  </>
                )}
              </button>
            </div>
          ) : (
            <div
              style={{
                backgroundColor: 'var(--danger-light)',
                color: 'var(--danger)',
                padding: '1rem',
                borderRadius: 'var(--radius-md)',
                fontWeight: 600,
                marginBottom: '2rem',
              }}
            >
              This item is currently out of stock. Please check back later!
            </div>
          )}

          {/* Delivery & Service Info */}
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '0.85rem',
              padding: '1.25rem',
              backgroundColor: '#fff',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--border-color)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.9rem' }}>
              <Truck size={18} color="var(--primary)" />
              <span><strong>Free Delivery:</strong> Ships within 24-48 hours.</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.9rem' }}>
              <RefreshCw size={18} color="var(--primary)" />
              <span><strong>Cash on Delivery (COD)</strong> available on all orders.</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.9rem' }}>
              <ShieldCheck size={18} color="var(--primary)" />
              <span><strong>100% Genuine Guaranteed:</strong> Sourced directly from manufacturers.</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
