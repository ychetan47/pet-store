import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Address, CreateAddressRequest, OrderDetail } from '../types';
import { addressService } from '../services/addressService';
import { orderService } from '../services/orderService';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { Modal } from '../components/common/Modal';
import { MapPin, Plus, CheckCircle2, ShieldCheck, Banknote, ShoppingBag } from 'lucide-react';

export const CheckoutPage: React.FC = () => {
  const { cart, refreshCart } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [placedOrder, setPlacedOrder] = useState<OrderDetail | null>(null);

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [newAddress, setNewAddress] = useState<CreateAddressRequest>({
    name: '',
    phone: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    pincode: '',
    isDefault: true,
  });

  useEffect(() => {
    loadAddresses();
  }, []);

  const loadAddresses = async () => {
    try {
      setLoading(true);
      const data = await addressService.getAddresses();
      setAddresses(data);
      if (data.length > 0) {
        const defaultAddr = data.find((a) => a.default) || data[0];
        setSelectedAddressId(defaultAddr.id);
      }
    } catch (err) {
      console.error('Failed to load addresses', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newAddress.name || !newAddress.phone || !newAddress.addressLine1 || !newAddress.city || !newAddress.state || !newAddress.pincode) {
      showToast('Please fill all required address fields', 'warning');
      return;
    }

    try {
      const created = await addressService.createAddress(newAddress);
      showToast('Address added successfully!', 'success');
      setIsModalOpen(false);
      setNewAddress({
        name: '',
        phone: '',
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        pincode: '',
        isDefault: false,
      });
      await loadAddresses();
      setSelectedAddressId(created.id);
    } catch (err: any) {
      showToast(err.response?.data?.message || 'Failed to save address', 'error');
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      showToast('Please select a delivery address', 'warning');
      return;
    }

    if (!cart || cart.items.length === 0) {
      showToast('Your cart is empty', 'warning');
      navigate('/cart');
      return;
    }

    try {
      setSubmitting(true);
      const order = await orderService.createOrder({
        addressId: selectedAddressId,
        paymentMethod: 'COD',
      });
      await refreshCart();
      setPlacedOrder(order);
      showToast('Order placed successfully!', 'success');
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to place order';
      showToast(msg, 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (placedOrder) {
    return (
      <div className="container" style={{ padding: '4rem 0', maxWidth: '640px', textAlign: 'center' }}>
        <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '3rem 2rem', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-lg)' }}>
          <div style={{ display: 'inline-flex', padding: '1rem', background: 'var(--success-light)', borderRadius: '50%', color: 'var(--success)', marginBottom: '1.5rem' }}>
            <CheckCircle2 size={56} />
          </div>

          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Order Placed Successfully!</h1>
          <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
            Thank you for pampering your pet with Paws & Claws. We are preparing your order!
          </p>

          <div style={{ backgroundColor: 'var(--bg-alt)', borderRadius: 'var(--radius-lg)', padding: '1.5rem', textAlign: 'left', marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Order ID:</span>
              <span style={{ fontWeight: 800, color: 'var(--primary)' }}>#{placedOrder.id}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Payment Method:</span>
              <span style={{ fontWeight: 700 }}>Cash on Delivery (COD)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Total Amount:</span>
              <span style={{ fontWeight: 800, fontSize: '1.1rem' }}>₹{placedOrder.totalAmount.toLocaleString('en-IN')}</span>
            </div>
            <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem', marginTop: '0.75rem' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Delivery To:</span>
              <p style={{ fontSize: '0.9rem', fontWeight: 600, marginTop: '0.25rem' }}>{placedOrder.shippingAddress}</p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <Link to={`/orders/${placedOrder.id}`} className="btn btn-primary btn-lg">
              View Order Details
            </Link>
            <Link to="/products" className="btn btn-secondary btn-lg">
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '6rem 0' }}>
        <ShoppingBag size={56} color="var(--text-light)" style={{ marginBottom: '1rem' }} />
        <h2>Your Cart is Empty</h2>
        <p style={{ color: 'var(--text-muted)', margin: '1rem 0 2rem' }}>
          Please add items to your cart before proceeding to checkout.
        </p>
        <Link to="/products" className="btn btn-primary btn-lg">
          Browse Products
        </Link>
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      <Breadcrumb items={[{ label: 'Cart', path: '/cart' }, { label: 'Checkout' }]} />

      <h2 style={{ marginBottom: '1.75rem' }}>Checkout & Order Confirmation</h2>

      <div className="cart-page-layout">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
          {/* Step 1: Delivery Address */}
          <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.75rem', border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.2rem' }}>
                <MapPin size={20} color="var(--primary)" /> 1. Select Delivery Address
              </h3>
              <button className="btn btn-outline btn-sm" onClick={() => setIsModalOpen(true)}>
                <Plus size={14} /> Add New Address
              </button>
            </div>

            {loading ? (
              <p style={{ color: 'var(--text-muted)' }}>Loading addresses...</p>
            ) : addresses.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: 'var(--bg-alt)', borderRadius: 'var(--radius-md)' }}>
                <p style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>You don't have any saved delivery addresses yet.</p>
                <button className="btn btn-primary btn-sm" onClick={() => setIsModalOpen(true)}>
                  Add Delivery Address
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                {addresses.map((addr) => (
                  <label
                    key={addr.id}
                    style={{
                      display: 'flex',
                      gap: '1rem',
                      padding: '1rem',
                      borderRadius: 'var(--radius-md)',
                      border: selectedAddressId === addr.id ? '2px solid var(--primary)' : '1px solid var(--border-color)',
                      backgroundColor: selectedAddressId === addr.id ? 'var(--primary-light)' : '#fff',
                      cursor: 'pointer',
                      transition: 'var(--transition)',
                    }}
                  >
                    <input
                      type="radio"
                      name="delivery_address"
                      checked={selectedAddressId === addr.id}
                      onChange={() => setSelectedAddressId(addr.id)}
                      style={{ accentColor: 'var(--primary)', marginTop: '0.2rem' }}
                    />
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                        <span style={{ fontWeight: 700 }}>{addr.name}</span>
                        <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>({addr.phone})</span>
                        {addr.default && (
                          <span style={{ fontSize: '0.7rem', backgroundColor: '#e2e8f0', padding: '0.15rem 0.4rem', borderRadius: '4px', fontWeight: 600 }}>
                            DEFAULT
                          </span>
                        )}
                      </div>
                      <p style={{ fontSize: '0.9rem', color: 'var(--text-main)', lineHeight: '1.4' }}>
                        {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}, {addr.city}, {addr.state} - {addr.pincode}
                      </p>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* Step 2: Payment Method */}
          <div style={{ backgroundColor: '#fff', borderRadius: 'var(--radius-xl)', padding: '1.75rem', border: '1px solid var(--border-color)' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.2rem', marginBottom: '1.25rem' }}>
              <Banknote size={20} color="var(--primary)" /> 2. Payment Method
            </h3>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '1rem',
                padding: '1.25rem',
                borderRadius: 'var(--radius-md)',
                border: '2px solid var(--primary)',
                backgroundColor: 'var(--primary-light)',
              }}
            >
              <input
                type="radio"
                name="payment_method"
                checked={true}
                readOnly
                style={{ accentColor: 'var(--primary)' }}
              />
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 700, fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span>Cash on Delivery (COD)</span>
                  <span className="badge badge-confirmed" style={{ fontSize: '0.75rem' }}>AVAILABLE</span>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Pay cash or UPI scan at your doorstep when the delivery partner arrives.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div className="order-summary-card">
            <h3 style={{ marginBottom: '1.25rem', fontSize: '1.2rem' }}>Order Review</h3>

            <div style={{ maxHeight: '240px', overflowY: 'auto', marginBottom: '1.25rem', paddingRight: '0.5rem' }}>
              {cart.items.map((item) => (
                <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', marginBottom: '0.75rem' }}>
                  <div style={{ maxWidth: '200px' }}>
                    <div style={{ fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {item.productName}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Qty: {item.quantity} × ₹{item.unitPrice}
                    </div>
                  </div>
                  <span style={{ fontWeight: 700 }}>₹{item.subtotal.toLocaleString('en-IN')}</span>
                </div>
              ))}
            </div>

            <div className="summary-row">
              <span>Items Subtotal</span>
              <span>₹{cart.subtotal.toLocaleString('en-IN')}</span>
            </div>

            <div className="summary-row">
              <span>Delivery Fee</span>
              <span style={{ color: 'var(--success)', fontWeight: 600 }}>FREE</span>
            </div>

            <div className="summary-row total">
              <span>Total Payable</span>
              <span style={{ color: 'var(--primary)' }}>₹{cart.totalAmount.toLocaleString('en-IN')}</span>
            </div>

            <div style={{ marginTop: '1.5rem' }}>
              <button
                className="btn btn-primary btn-lg"
                style={{ width: '100%' }}
                disabled={submitting || !selectedAddressId}
                onClick={handlePlaceOrder}
              >
                {submitting ? 'Placing Order...' : 'Place Order (COD)'}
              </button>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem', marginTop: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              <ShieldCheck size={14} color="var(--success)" />
              <span>Free returns & replacement guarantee</span>
            </div>
          </div>
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add New Delivery Address">
        <form onSubmit={handleCreateAddress}>
          <div className="form-group">
            <label className="form-label">Full Name *</label>
            <input
              type="text"
              className="form-control"
              required
              value={newAddress.name}
              onChange={(e) => setNewAddress({ ...newAddress, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number *</label>
            <input
              type="tel"
              className="form-control"
              required
              placeholder="10-digit mobile number"
              value={newAddress.phone}
              onChange={(e) => setNewAddress({ ...newAddress, phone: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address Line 1 (Flat, House no., Building) *</label>
            <input
              type="text"
              className="form-control"
              required
              value={newAddress.addressLine1}
              onChange={(e) => setNewAddress({ ...newAddress, addressLine1: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address Line 2 (Area, Street, Landmark)</label>
            <input
              type="text"
              className="form-control"
              value={newAddress.addressLine2 || ''}
              onChange={(e) => setNewAddress({ ...newAddress, addressLine2: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label">City *</label>
              <input
                type="text"
                className="form-control"
                required
                value={newAddress.city}
                onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">State *</label>
              <input
                type="text"
                className="form-control"
                required
                value={newAddress.state}
                onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Pincode *</label>
              <input
                type="text"
                className="form-control"
                required
                value={newAddress.pincode}
                onChange={(e) => setNewAddress({ ...newAddress, pincode: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
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
