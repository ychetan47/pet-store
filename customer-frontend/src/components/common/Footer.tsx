import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, Truck, RefreshCw, PhoneCall } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="footer-wrapper">
      <div className="container">
        {/* Trust Badges */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '1.5rem',
            paddingBottom: '3rem',
            marginBottom: '3rem',
            borderBottom: '1px solid #1e293b',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', background: '#1e293b', borderRadius: '12px', color: 'var(--primary)' }}>
              <Truck size={24} />
            </div>
            <div>
              <h4 style={{ color: '#fff', fontSize: '0.95rem' }}>Free & Fast Delivery</h4>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>On all orders across the nation</p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', background: '#1e293b', borderRadius: '12px', color: 'var(--primary)' }}>
              <ShieldCheck size={24} />
            </div>
            <div>
              <h4 style={{ color: '#fff', fontSize: '0.95rem' }}>100% Authentic</h4>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Direct from verified pet brands</p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', background: '#1e293b', borderRadius: '12px', color: 'var(--primary)' }}>
              <RefreshCw size={24} />
            </div>
            <div>
              <h4 style={{ color: '#fff', fontSize: '0.95rem' }}>Cash on Delivery</h4>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Pay easily when doorstep arrives</p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', background: '#1e293b', borderRadius: '12px', color: 'var(--primary)' }}>
              <PhoneCall size={24} />
            </div>
            <div>
              <h4 style={{ color: '#fff', fontSize: '0.95rem' }}>Pet Parent Support</h4>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Support available 7 days a week</p>
            </div>
          </div>
        </div>

        {/* Links Grid */}
        <div className="footer-grid">
          <div>
            <div className="footer-brand">
              🐾 Paws<span>&</span>Claws
            </div>
            <p style={{ fontSize: '0.9rem', lineHeight: '1.6', maxWidth: '320px' }}>
              Your companion's favorite food and accessories store. Delivering wholesome nutrition, comfortable beds,
              and engaging playthings for dogs and cats.
            </p>
          </div>

          <div>
            <h4 className="footer-heading">Dogs</h4>
            <ul className="footer-links">
              <li><Link to="/products?pet=dogs">Dog Food</Link></li>
              <li><Link to="/products?pet=dogs">Dry Dog Food</Link></li>
              <li><Link to="/products?pet=dogs">Dog Accessories</Link></li>
              <li><Link to="/products?pet=dogs">Dog Toys</Link></li>
              <li><Link to="/products?pet=dogs">Dog Grooming</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="footer-heading">Cats</h4>
            <ul className="footer-links">
              <li><Link to="/products?pet=cats">Cat Food</Link></li>
              <li><Link to="/products?pet=cats">Wet Cat Food</Link></li>
              <li><Link to="/products?pet=cats">Cat Accessories</Link></li>
              <li><Link to="/products?pet=cats">Cat Toys</Link></li>
              <li><Link to="/products?pet=cats">Cat Grooming</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="footer-heading">Customer Care</h4>
            <ul className="footer-links">
              <li><Link to="/orders">Track My Order</Link></li>
              <li><Link to="/profile/addresses">Delivery Addresses</Link></li>
              <li><Link to="/products">Browse All Catalog</Link></li>
              <li><span>Email: support@pawsandclaws.store</span></li>
              <li><span>Toll Free: 1800-PET-LOVE</span></li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <p>© {new Date().getFullYear()} Paws & Claws Pet Store V1. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};
