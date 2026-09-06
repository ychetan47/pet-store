import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, ArrowRight } from 'lucide-react';
import { ProductSummary, Category } from '../types';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { ProductCard } from '../components/product/ProductCard';

export const HomePage: React.FC = () => {
  const [featuredProducts, setFeaturedProducts] = useState<ProductSummary[]>([]);
  const [popularProducts, setPopularProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadHomeData = async () => {
      try {
        setLoading(true);
        const [featRes, popRes, catRes] = await Promise.all([
          productService.getProducts({ size: 4, sortBy: 'createdAt', sortDirection: 'desc' }),
          productService.getProducts({ size: 8, sortBy: 'price', sortDirection: 'desc' }),
          categoryService.getCategories(),
        ]);
        setFeaturedProducts(featRes.content);
        setPopularProducts(popRes.content);
        setCategories(catRes);
      } catch (err) {
        console.error('Failed to load homepage data', err);
      } finally {
        setLoading(false);
      }
    };
    loadHomeData();
  }, []);

  const dogCategories = categories.filter((c) => c.parentId === 1).slice(0, 4);
  const catCategories = categories.filter((c) => c.parentId === 2).slice(0, 4);

  return (
    <div className="home-page container">
      {/* Hero Section */}
      <div className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">
            <Sparkles size={16} /> Premium Nutrition & Accessories for Pets
          </div>
          <h1 className="hero-title">
            Healthy Bites & Happy Tails for <span>Dogs & Cats</span>
          </h1>
          <p className="hero-desc">
            Explore veterinarian-approved diets, comfortable orthopedic beds, durable toys, and grooming essentials.
            Enjoy convenient Cash on Delivery and fast nationwide doorstep delivery!
          </p>
          <div className="hero-actions">
            <Link to="/products?pet=dogs" className="btn btn-primary btn-lg">
              Shop For Dogs <ArrowRight size={18} />
            </Link>
            <Link to="/products?pet=cats" className="btn btn-secondary btn-lg" style={{ backgroundColor: '#fff' }}>
              Shop For Cats
            </Link>
          </div>
        </div>

        <div className="hero-visual">
          <img
            src="https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=1000&q=80"
            alt="Happy dog and companion"
            className="hero-img"
          />
        </div>
      </div>

      {/* Pet Quick Banner */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '3.5rem' }}>
        <Link
          to="/products?pet=dogs"
          style={{
            background: 'linear-gradient(135deg, #fef3c7 0%, #fff7ed 100%)',
            border: '1px solid #fde68a',
            borderRadius: 'var(--radius-xl)',
            padding: '2rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            transition: 'var(--transition)',
          }}
          className="card-hover"
        >
          <div>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#b45309', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Canine Essentials
            </span>
            <h3 style={{ fontSize: '1.75rem', marginTop: '0.25rem', marginBottom: '0.5rem' }}>For Dogs</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>
              Royal Canin, Pedigree, KONG chew toys & orthopedic beds
            </p>
            <span style={{ fontWeight: 700, color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              Browse Dogs <ArrowRight size={16} />
            </span>
          </div>
          <span style={{ fontSize: '3.5rem' }}>🐕</span>
        </Link>

        <Link
          to="/products?pet=cats"
          style={{
            background: 'linear-gradient(135deg, #ede9fe 0%, #f5f3ff 100%)',
            border: '1px solid #ddd6fe',
            borderRadius: 'var(--radius-xl)',
            padding: '2rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            transition: 'var(--transition)',
          }}
          className="card-hover"
        >
          <div>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#6d28d9', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Feline Specialties
            </span>
            <h3 style={{ fontSize: '1.75rem', marginTop: '0.25rem', marginBottom: '0.5rem' }}>For Cats</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>
              Whiskas, Sheba delicacies, scratching toys & plush beds
            </p>
            <span style={{ fontWeight: 700, color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              Browse Cats <ArrowRight size={16} />
            </span>
          </div>
          <span style={{ fontSize: '3.5rem' }}>🐈</span>
        </Link>
      </div>

      {/* Dog Categories */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div className="category-section-title">
          <div>
            <h2>Dog Categories</h2>
            <p style={{ color: 'var(--text-muted)' }}>Top picks formulated specifically for dog wellbeing</p>
          </div>
          <Link to="/products?pet=dogs" className="btn btn-secondary btn-sm">
            View All Dogs <ArrowRight size={14} />
          </Link>
        </div>

        <div className="category-grid">
          {dogCategories.map((cat) => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`} className="category-card">
              <div className="category-img-wrap">
                <img src={cat.imageUrl} alt={cat.name} />
              </div>
              <div className="category-name">{cat.name}</div>
            </Link>
          ))}
        </div>
      </section>

      {/* Cat Categories */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div className="category-section-title">
          <div>
            <h2>Cat Categories</h2>
            <p style={{ color: 'var(--text-muted)' }}>Curated nutrition, play, and care for inquisitive cats</p>
          </div>
          <Link to="/products?pet=cats" className="btn btn-secondary btn-sm">
            View All Cats <ArrowRight size={14} />
          </Link>
        </div>

        <div className="category-grid">
          {catCategories.map((cat) => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`} className="category-card">
              <div className="category-img-wrap">
                <img src={cat.imageUrl} alt={cat.name} />
              </div>
              <div className="category-name">{cat.name}</div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div className="category-section-title">
          <div>
            <h2>Featured Products</h2>
            <p style={{ color: 'var(--text-muted)' }}>Hand-picked favorites by our veterinary and nutrition team</p>
          </div>
          <Link to="/products" className="btn btn-secondary btn-sm">
            Browse All <ArrowRight size={14} />
          </Link>
        </div>

        <div className="product-grid">
          {featuredProducts.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>

      {/* Popular Products */}
      <section style={{ marginBottom: '3rem' }}>
        <div className="category-section-title">
          <div>
            <h2>Popular Products</h2>
            <p style={{ color: 'var(--text-muted)' }}>Best sellers pet parents are loving right now</p>
          </div>
        </div>

        <div className="product-grid">
          {popularProducts.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>
    </div>
  );
};
