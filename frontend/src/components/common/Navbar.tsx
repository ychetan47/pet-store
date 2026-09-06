import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingBag, Search, User as UserIcon, LogOut, Package, MapPin, Heart } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { totalItems } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchTerm, setSearchTerm] = useState('');
  const [showDropdown, setShowDropdown] = useState(false);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchTerm.trim())}`);
    } else {
      navigate('/products');
    }
  };

  return (
    <header className="navbar-header">
      <div className="container navbar-container">
        {/* Brand Logo */}
        <Link to="/" className="nav-brand">
          <span style={{ fontSize: '1.8rem' }}>🐾</span>
          <div>
            Paws<span>&</span>Claws
          </div>
        </Link>

        {/* Global Search */}
        <form className="nav-search-bar" onSubmit={handleSearch}>
          <Search className="nav-search-icon" size={18} />
          <input
            type="text"
            placeholder="Search food, treats, toys, collars..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </form>

        {/* Navigation links */}
        <nav className="nav-links">
          <Link
            to="/products?pet=dogs"
            className={`nav-link ${location.search.includes('pet=dogs') ? 'active' : ''}`}
          >
            <span className="nav-pet-badge-dog">🐶 Dogs</span>
          </Link>

          <Link
            to="/products?pet=cats"
            className={`nav-link ${location.search.includes('pet=cats') ? 'active' : ''}`}
          >
            <span className="nav-pet-badge-cat">🐱 Cats</span>
          </Link>

          <Link to="/products" className="nav-link">
            All Products
          </Link>

          {/* Cart button */}
          <Link to="/cart" className="cart-icon-btn" title="View Cart">
            <ShoppingBag size={22} />
            {totalItems > 0 && <span className="cart-badge">{totalItems}</span>}
          </Link>

          {/* User Profile / Auth */}
          {isAuthenticated ? (
            <div style={{ position: 'relative' }}>
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => setShowDropdown(!showDropdown)}
                style={{ gap: '0.4rem' }}
              >
                <UserIcon size={16} />
                <span>{user?.name?.split(' ')[0] || 'Account'}</span>
              </button>

              {showDropdown && (
                <div
                  style={{
                    position: 'absolute',
                    right: 0,
                    top: 'calc(100% + 8px)',
                    backgroundColor: '#fff',
                    borderRadius: '12px',
                    boxShadow: 'var(--shadow-xl)',
                    border: '1px solid var(--border-color)',
                    width: '210px',
                    padding: '0.5rem',
                    zIndex: 200,
                  }}
                  onClick={() => setShowDropdown(false)}
                >
                  <div style={{ padding: '0.65rem 0.85rem', borderBottom: '1px solid var(--border-color)' }}>
                    <p style={{ fontWeight: 700, fontSize: '0.9rem' }}>{user?.name}</p>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</p>
                  </div>

                  <Link
                    to="/orders"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.6rem',
                      padding: '0.65rem 0.85rem',
                      borderRadius: '6px',
                      fontSize: '0.9rem',
                      fontWeight: 500,
                    }}
                    className="dropdown-item"
                  >
                    <Package size={16} /> My Orders
                  </Link>

                  <Link
                    to="/profile/addresses"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.6rem',
                      padding: '0.65rem 0.85rem',
                      borderRadius: '6px',
                      fontSize: '0.9rem',
                      fontWeight: 500,
                    }}
                    className="dropdown-item"
                  >
                    <MapPin size={16} /> Saved Addresses
                  </Link>

                  <button
                    onClick={logout}
                    style={{
                      width: '100%',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.6rem',
                      padding: '0.65rem 0.85rem',
                      borderRadius: '6px',
                      fontSize: '0.9rem',
                      fontWeight: 500,
                      color: 'var(--danger)',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                      textAlign: 'left',
                    }}
                  >
                    <LogOut size={16} /> Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <Link to="/login" className="btn btn-secondary btn-sm">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Register
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
};
