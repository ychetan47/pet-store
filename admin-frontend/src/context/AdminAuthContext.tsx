import React, { createContext, useContext, useState, useEffect } from 'react';
import { AdminUser } from '../types';
import { authApi } from '../services/api';

interface AdminAuthContextType {
  user: AdminUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AdminAuthContext = createContext<AdminAuthContextType | undefined>(undefined);

export const AdminAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initAuth = () => {
      const storedToken = localStorage.getItem('petstore_admin_token');
      const storedUser = localStorage.getItem('petstore_admin_user');

      if (storedToken && storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser);
          if (parsedUser.role === 'ADMIN') {
            setUser(parsedUser);
          } else {
            localStorage.removeItem('petstore_admin_token');
            localStorage.removeItem('petstore_admin_user');
          }
        } catch {
          localStorage.removeItem('petstore_admin_token');
          localStorage.removeItem('petstore_admin_user');
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string) => {
    const authData = await authApi.login(email, password);

    if (authData.role !== 'ADMIN') {
      throw new Error('Access denied: You do not have administrator privileges.');
    }

    const adminUser: AdminUser = {
      id: authData.id,
      name: authData.name,
      email: authData.email,
      role: authData.role,
    };

    localStorage.setItem('petstore_admin_token', authData.token);
    localStorage.setItem('petstore_admin_user', JSON.stringify(adminUser));
    setUser(adminUser);
  };

  const logout = () => {
    localStorage.removeItem('petstore_admin_token');
    localStorage.removeItem('petstore_admin_user');
    setUser(null);
  };

  return (
    <AdminAuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout }}>
      {children}
    </AdminAuthContext.Provider>
  );
};

export const useAdminAuth = () => {
  const context = useContext(AdminAuthContext);
  if (!context) {
    throw new Error('useAdminAuth must be used within an AdminAuthProvider');
  }
  return context;
};
