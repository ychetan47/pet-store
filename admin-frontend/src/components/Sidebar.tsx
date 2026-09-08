import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Package, Boxes, FolderTree, ShoppingCart, LogOut, Store } from 'lucide-react';
import { useAdminAuth } from '../context/AdminAuthContext';

export const Sidebar: React.FC = () => {
  const { logout } = useAdminAuth();

  return (
    <aside className="admin-sidebar">
      <div className="sidebar-header">
        <div className="sidebar-logo-icon">🐾</div>
        <div>
          <div className="sidebar-title">
            Paws & Claws
            <span className="sidebar-badge">Admin</span>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/dashboard"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <LayoutDashboard size={20} />
          <span>Dashboard</span>
        </NavLink>

        <NavLink
          to="/products"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <Package size={20} />
          <span>Products</span>
        </NavLink>

        <NavLink
          to="/inventory"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <Boxes size={20} />
          <span>Inventory</span>
        </NavLink>

        <NavLink
          to="/categories"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <FolderTree size={20} />
          <span>Categories</span>
        </NavLink>

        <NavLink
          to="/orders"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <ShoppingCart size={20} />
          <span>Orders</span>
        </NavLink>
      </nav>

      <div className="sidebar-footer">
        <a
          href="http://localhost:5173"
          target="_blank"
          rel="noopener noreferrer"
          className="nav-item"
          style={{ marginBottom: '0.5rem', color: '#94a3b8' }}
        >
          <Store size={18} />
          <span>View Storefront</span>
        </a>
        <button
          onClick={logout}
          className="nav-item"
          style={{ width: '100%', background: 'transparent', border: 'none', cursor: 'pointer', color: '#f87171' }}
        >
          <LogOut size={18} />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
