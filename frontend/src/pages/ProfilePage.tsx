import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { Package, MapPin, Mail, Phone, Calendar } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();

  if (!user) return null;

  const joinDate = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-IN', {
        month: 'long',
        year: 'numeric',
      })
    : '';

  return (
    <div className="container" style={{ paddingBottom: '4rem', maxWidth: '760px' }}>
      <Breadcrumb items={[{ label: 'Profile' }]} />

      <h2 style={{ marginBottom: '1.75rem' }}>My Account</h2>

      <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '2rem', border: '1px solid var(--border-color)', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', marginBottom: '1.5rem' }}>
          <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '1.5rem' }}>
            {user.name.charAt(0).toUpperCase()}
          </div>
          <div>
            <h3 style={{ fontSize: '1.3rem' }}>{user.name}</h3>
            <span className="badge badge-placed" style={{ fontSize: '0.75rem' }}>CUSTOMER</span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Mail size={18} color="var(--primary)" />
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Email</div>
              <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{user.email}</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Phone size={18} color="var(--primary)" />
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Phone</div>
              <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{user.phone || 'Not provided'}</div>
            </div>
          </div>

          {joinDate && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Calendar size={18} color="var(--primary)" />
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Member Since</div>
                <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{joinDate}</div>
              </div>
            </div>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        <Link
          to="/orders"
          style={{
            backgroundColor: '#fff',
            borderRadius: 'var(--radius-xl)',
            padding: '1.75rem',
            border: '1px solid var(--border-color)',
            display: 'flex',
            alignItems: 'center',
            gap: '1rem',
            transition: 'var(--transition)',
          }}
          className="card-hover"
        >
          <div style={{ padding: '0.75rem', background: 'var(--primary-light)', borderRadius: '12px', color: 'var(--primary)' }}>
            <Package size={24} />
          </div>
          <div>
            <h4 style={{ fontSize: '1.1rem' }}>Order History</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>View past orders and tracking details</p>
          </div>
        </Link>

        <Link
          to="/profile/addresses"
          style={{
            backgroundColor: '#fff',
            borderRadius: 'var(--radius-xl)',
            padding: '1.75rem',
            border: '1px solid var(--border-color)',
            display: 'flex',
            alignItems: 'center',
            gap: '1rem',
            transition: 'var(--transition)',
          }}
          className="card-hover"
        >
          <div style={{ padding: '0.75rem', background: 'var(--secondary-light)', borderRadius: '12px', color: 'var(--secondary)' }}>
            <MapPin size={24} />
          </div>
          <div>
            <h4 style={{ fontSize: '1.1rem' }}>Delivery Addresses</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Manage addresses for delivery</p>
          </div>
        </Link>
      </div>
    </div>
  );
};
