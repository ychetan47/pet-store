import React from 'react';
import { useAdminAuth } from '../context/AdminAuthContext';
import { ShieldCheck } from 'lucide-react';

export const Header: React.FC<{ title: string }> = ({ title }) => {
  const { user } = useAdminAuth();

  return (
    <header className="admin-header">
      <h2>{title}</h2>

      <div className="header-user">
        <div className="user-badge">
          <div className="user-avatar">
            {user?.name ? user.name.charAt(0).toUpperCase() : 'A'}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.875rem' }}>{user?.name || 'Administrator'}</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: '#10b981' }}>
              <ShieldCheck size={14} />
              <span>ROLE_ADMIN</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
