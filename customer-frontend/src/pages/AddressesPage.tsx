import React, { useEffect, useState } from 'react';
import { Address, CreateAddressRequest } from '../types';
import { addressService } from '../services/addressService';
import { useToast } from '../context/ToastContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { Modal } from '../components/common/Modal';
import { MapPin, Plus, Trash2, CheckCircle2 } from 'lucide-react';

export const AddressesPage: React.FC = () => {
  const { showToast } = useToast();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  const [form, setForm] = useState<CreateAddressRequest>({
    name: '',
    phone: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    pincode: '',
    isDefault: false,
  });

  const loadAddresses = async () => {
    try {
      setLoading(true);
      const data = await addressService.getAddresses();
      setAddresses(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAddresses();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await addressService.createAddress(form);
      showToast('Address added successfully!', 'success');
      setIsModalOpen(false);
      setForm({
        name: '',
        phone: '',
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        pincode: '',
        isDefault: false,
      });
      loadAddresses();
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to add address', 'error');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this address?')) return;
    try {
      await addressService.deleteAddress(id);
      showToast('Address removed', 'info');
      loadAddresses();
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to remove address', 'error');
    }
  };

  return (
    <div className="container" style={{ paddingBottom: '4rem', maxWidth: '800px' }}>
      <Breadcrumb items={[{ label: 'Saved Addresses' }]} />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h2>Delivery Addresses</h2>
          <p style={{ color: 'var(--text-muted)' }}>Manage locations for fast Cash On Delivery checkout</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} /> Add Address
        </button>
      </div>

      {loading ? (
        <p>Loading addresses...</p>
      ) : addresses.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem 1rem', background: '#fff', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)' }}>
          <MapPin size={48} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
          <h3>No Addresses Saved</h3>
          <p style={{ color: 'var(--text-muted)', margin: '1rem 0' }}>Add your first delivery address below.</p>
          <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
            Add Address
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {addresses.map((addr) => (
            <div
              key={addr.id}
              style={{
                backgroundColor: '#fff',
                borderRadius: 'var(--radius-lg)',
                padding: '1.5rem',
                border: '1px solid var(--border-color)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
                  <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>{addr.name}</span>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>({addr.phone})</span>
                  {addr.default && (
                    <span className="badge badge-delivered" style={{ fontSize: '0.75rem' }}>
                      <CheckCircle2 size={12} /> Default
                    </span>
                  )}
                </div>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-main)' }}>
                  {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}
                </p>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {addr.city}, {addr.state} - {addr.pincode}
                </p>
              </div>

              <button
                className="btn btn-danger-outline btn-sm"
                onClick={() => handleDelete(addr.id)}
                title="Delete address"
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))}
        </div>
      )}

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add Delivery Address">
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label className="form-label">Full Name *</label>
            <input
              type="text"
              className="form-control"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number *</label>
            <input
              type="tel"
              className="form-control"
              required
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address Line 1 *</label>
            <input
              type="text"
              className="form-control"
              required
              value={form.addressLine1}
              onChange={(e) => setForm({ ...form, addressLine1: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address Line 2 (Area/Landmark)</label>
            <input
              type="text"
              className="form-control"
              value={form.addressLine2 || ''}
              onChange={(e) => setForm({ ...form, addressLine2: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label">City *</label>
              <input
                type="text"
                className="form-control"
                required
                value={form.city}
                onChange={(e) => setForm({ ...form, city: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">State *</label>
              <input
                type="text"
                className="form-control"
                required
                value={form.state}
                onChange={(e) => setForm({ ...form, state: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Pincode *</label>
              <input
                type="text"
                className="form-control"
                required
                value={form.pincode}
                onChange={(e) => setForm({ ...form, pincode: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.9rem' }}>
              <input
                type="checkbox"
                checked={form.isDefault}
                onChange={(e) => setForm({ ...form, isDefault: e.target.checked })}
                style={{ accentColor: 'var(--primary)' }}
              />
              <span>Set as default address</span>
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Save Address
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
