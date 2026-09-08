import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

export const AdminLayout: React.FC = () => {
  const location = useLocation();

  const getPageTitle = (path: string) => {
    if (path.startsWith('/dashboard')) return 'Dashboard Overview';
    if (path.startsWith('/products/new')) return 'Add New Product';
    if (path.includes('/edit')) return 'Edit Product';
    if (path.startsWith('/products')) return 'Product Management';
    if (path.startsWith('/categories/new')) return 'Add New Category';
    if (path.startsWith('/categories')) return 'Category Management';
    if (path.startsWith('/orders/')) return 'Order Details';
    if (path.startsWith('/orders')) return 'Order Management';
    return 'Admin Portal';
  };

  return (
    <div className="admin-container">
      <Sidebar />
      <div className="admin-main">
        <Header title={getPageTitle(location.pathname)} />
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
