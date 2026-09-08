import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { Trash2, ShoppingBag, ArrowRight, ArrowLeft } from 'lucide-react';

export const CartPage: React.FC = () => {
  const { cart, updateQuantity, removeItem, clearCart, isLoading } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (!isAuthenticated) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <ShoppingBag size={56} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
        <h2>Your Cart is Waiting</h2>
        <p style={{ color: 'var(--text-muted)', margin: '1rem 0 2rem' }}>
          Please log in to your account to view or add items to your cart.
        </p>
        <Link to="/login" className="btn btn-primary btn-lg">
          Log In Now
        </Link>
      </div>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <ShoppingBag size={56} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
        <h2>Your Shopping Cart is Empty</h2>
        <p style={{ color: 'var(--text-muted)', margin: '1rem 0 2rem' }}>
          Looks like you haven't added any pet delicacies or goodies to your cart yet.
        </p>
        <Link to="/products" className="btn btn-primary btn-lg">
          Explore Products
        </Link>
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      <Breadcrumb items={[{ label: 'Cart' }]} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2>Your Shopping Cart ({cart.totalItems ?? cart.items.length} items)</h2>
        <button
          className="btn btn-secondary btn-sm"
          onClick={clearCart}
          disabled={isLoading}
          style={{ color: 'var(--danger)', borderColor: '#fca5a5' }}
        >
          Clear Cart
        </button>
      </div>

      <div className="cart-page-layout">
        <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.5rem', border: '1px solid var(--border-color)' }}>
          {cart.items.map((item) => {
            const price = item.unitPrice ?? item.price ?? 0;
            const subtotal = item.subtotal ?? (price * item.quantity);
            const image = item.productImage || item.imageUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=400&q=80';

            return (
              <div key={item.id} className="cart-item-row">
                <img
                  src={image}
                  alt={item.productName}
                  className="cart-item-img"
                />

                <div className="cart-item-details">
                  {item.productBrand && <div className="cart-item-brand">{item.productBrand}</div>}
                  <Link to={`/products/${item.productId}`}>
                    <div className="cart-item-title">{item.productName}</div>
                  </Link>
                  <div className="cart-item-price">₹{price.toLocaleString('en-IN')}</div>
                </div>

                <div className="quantity-picker">
                  <button
                    type="button"
                    className="qty-btn"
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    disabled={isLoading}
                  >
                    -
                  </button>
                  <span className="qty-value">{item.quantity}</span>
                  <button
                    type="button"
                    className="qty-btn"
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    disabled={isLoading || item.quantity >= (item.availableStock ?? 999)}
                  >
                    +
                  </button>
                </div>

                <div style={{ minWidth: '90px', textAlign: 'right', fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-main)' }}>
                  ₹{subtotal.toLocaleString('en-IN')}
                </div>

                <button
                  type="button"
                  onClick={() => removeItem(item.id)}
                  className="btn-danger-outline"
                  style={{ border: 'none', background: 'none', cursor: 'pointer', padding: '0.4rem', color: 'var(--danger)' }}
                  title="Remove item"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            );
          })}

          <div style={{ marginTop: '1.5rem' }}>
            <Link to="/products" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600, color: 'var(--primary)' }}>
              <ArrowLeft size={16} /> Continue Shopping
            </Link>
          </div>
        </div>

        <div>
          <div className="order-summary-card">
            <h3 style={{ marginBottom: '1.25rem', fontSize: '1.2rem' }}>Order Summary</h3>

            <div className="summary-row">
              <span>Items Total ({cart.totalItems ?? cart.items.length})</span>
              <span>₹{(cart.subtotal ?? 0).toLocaleString('en-IN')}</span>
            </div>

            <div className="summary-row">
              <span>Delivery Fee</span>
              <span style={{ color: 'var(--success)', fontWeight: 600 }}>FREE</span>
            </div>

            <div className="summary-row total">
              <span>Total Amount</span>
              <span style={{ color: 'var(--primary)' }}>₹{(cart.totalAmount ?? (cart.subtotal ?? 0)).toLocaleString('en-IN')}</span>
            </div>

            <div style={{ marginTop: '1.5rem' }}>
              <button
                className="btn btn-primary btn-lg"
                style={{ width: '100%' }}
                onClick={() => navigate('/checkout')}
              >
                Proceed to Checkout <ArrowRight size={18} />
              </button>
            </div>

            <div style={{ marginTop: '1.25rem', fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center' }}>
              🔒 Safe & Secure Cash On Delivery Checkout
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
