import React, { useEffect, useState } from 'react';
import { CategoryTree } from '../../types';
import { categoryService } from '../../services/categoryService';
import { productService } from '../../services/productService';
import { Filter, X, ChevronDown, ChevronRight } from 'lucide-react';

interface ProductFiltersProps {
  selectedPet?: string;
  selectedCategoryId?: number;
  selectedBrand?: string;
  minPrice?: number;
  maxPrice?: number;
  onFilterChange: (filters: {
    pet?: string;
    categoryId?: number;
    brand?: string;
    minPrice?: number;
    maxPrice?: number;
  }) => void;
  onReset: () => void;
}

export const ProductFilters: React.FC<ProductFiltersProps> = ({
  selectedPet,
  selectedCategoryId,
  selectedBrand,
  minPrice,
  maxPrice,
  onFilterChange,
  onReset,
}) => {
  const [categoriesTree, setCategoriesTree] = useState<CategoryTree[]>([]);
  const [brands, setBrands] = useState<string[]>([]);
  const [expandedCats, setExpandedCats] = useState<Record<number, boolean>>({});

  useEffect(() => {
    categoryService.getCategoryTree().then(setCategoriesTree).catch(console.error);
    productService.getBrands().then(setBrands).catch(console.error);
  }, []);

  const toggleExpand = (id: number) => {
    setExpandedCats((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <aside style={{ backgroundColor: '#fff', padding: '1.5rem', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', paddingBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
        <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1.1rem' }}>
          <Filter size={18} color="var(--primary)" /> Filters
        </h4>
        <button
          className="btn btn-secondary btn-sm"
          onClick={onReset}
          style={{ padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}
        >
          <X size={14} /> Clear
        </button>
      </div>

      {/* Pet Filter */}
      <div style={{ marginBottom: '1.75rem' }}>
        <h5 style={{ fontSize: '0.85rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.75rem', letterSpacing: '0.05em' }}>
          Pet
        </h5>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            className={`btn btn-sm ${!selectedPet ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => onFilterChange({ pet: undefined, categoryId: undefined })}
            style={{ flex: 1 }}
          >
            All
          </button>
          <button
            className={`btn btn-sm ${selectedPet === 'dogs' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => onFilterChange({ pet: 'dogs', categoryId: undefined })}
            style={{ flex: 1 }}
          >
            🐶 Dogs
          </button>
          <button
            className={`btn btn-sm ${selectedPet === 'cats' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => onFilterChange({ pet: 'cats', categoryId: undefined })}
            style={{ flex: 1 }}
          >
            🐱 Cats
          </button>
        </div>
      </div>

      {/* Categories Hierarchy */}
      <div style={{ marginBottom: '1.75rem' }}>
        <h5 style={{ fontSize: '0.85rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.75rem', letterSpacing: '0.05em' }}>
          Categories
        </h5>
        <div style={{ maxHeight: '280px', overflowY: 'auto', paddingRight: '0.5rem' }}>
          {categoriesTree
            .filter((c) => !selectedPet || c.slug === selectedPet)
            .map((petCat) => (
              <div key={petCat.id} style={{ marginBottom: '0.5rem' }}>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.35rem 0.5rem',
                    borderRadius: '6px',
                    backgroundColor: selectedCategoryId === petCat.id ? 'var(--primary-light)' : 'transparent',
                    cursor: 'pointer',
                    fontWeight: 600,
                  }}
                  onClick={() => onFilterChange({ categoryId: petCat.id })}
                >
                  <span>{petCat.name}</span>
                  {petCat.subcategories.length > 0 && (
                    <span onClick={(e) => { e.stopPropagation(); toggleExpand(petCat.id); }}>
                      {expandedCats[petCat.id] ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                    </span>
                  )}
                </div>

                {/* Subcategories */}
                {(expandedCats[petCat.id] || selectedPet === petCat.slug) && (
                  <div style={{ paddingLeft: '0.75rem', marginTop: '0.25rem' }}>
                    {petCat.subcategories.map((sub) => (
                      <div key={sub.id} style={{ marginBottom: '0.25rem' }}>
                        <div
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '0.3rem 0.5rem',
                            borderRadius: '6px',
                            fontSize: '0.85rem',
                            cursor: 'pointer',
                            color: selectedCategoryId === sub.id ? 'var(--primary)' : 'var(--text-main)',
                            fontWeight: selectedCategoryId === sub.id ? 700 : 500,
                            backgroundColor: selectedCategoryId === sub.id ? 'var(--primary-light)' : 'transparent',
                          }}
                          onClick={() => onFilterChange({ categoryId: sub.id })}
                        >
                          <span>{sub.name}</span>
                          {sub.subcategories?.length > 0 && (
                            <span onClick={(e) => { e.stopPropagation(); toggleExpand(sub.id); }}>
                              {expandedCats[sub.id] ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                            </span>
                          )}
                        </div>

                        {/* Level 3 subcategories */}
                        {expandedCats[sub.id] && sub.subcategories && (
                          <div style={{ paddingLeft: '0.75rem', marginTop: '0.2rem' }}>
                            {sub.subcategories.map((leaf) => (
                              <div
                                key={leaf.id}
                                style={{
                                  padding: '0.25rem 0.5rem',
                                  borderRadius: '4px',
                                  fontSize: '0.8rem',
                                  cursor: 'pointer',
                                  color: selectedCategoryId === leaf.id ? 'var(--primary)' : 'var(--text-muted)',
                                  fontWeight: selectedCategoryId === leaf.id ? 700 : 400,
                                  backgroundColor: selectedCategoryId === leaf.id ? 'var(--primary-light)' : 'transparent',
                                }}
                                onClick={() => onFilterChange({ categoryId: leaf.id })}
                              >
                                {leaf.name}
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
        </div>
      </div>

      {/* Brand Filter */}
      {brands.length > 0 && (
        <div style={{ marginBottom: '1.75rem' }}>
          <h5 style={{ fontSize: '0.85rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.75rem', letterSpacing: '0.05em' }}>
            Brand
          </h5>
          <div style={{ maxHeight: '180px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
            {brands.map((b) => (
              <label
                key={b}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontSize: '0.85rem',
                  cursor: 'pointer',
                  padding: '0.25rem 0.5rem',
                  borderRadius: '4px',
                  backgroundColor: selectedBrand === b ? 'var(--primary-light)' : 'transparent',
                }}
              >
                <input
                  type="radio"
                  name="brand_filter"
                  checked={selectedBrand === b}
                  onChange={() => onFilterChange({ brand: selectedBrand === b ? undefined : b })}
                  style={{ accentColor: 'var(--primary)' }}
                />
                <span style={{ fontWeight: selectedBrand === b ? 700 : 500 }}>{b}</span>
              </label>
            ))}
          </div>
        </div>
      )}

      {/* Price Range Filter */}
      <div>
        <h5 style={{ fontSize: '0.85rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.75rem', letterSpacing: '0.05em' }}>
          Price Range (₹)
        </h5>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <input
            type="number"
            placeholder="Min"
            className="form-control"
            style={{ padding: '0.4rem 0.6rem', fontSize: '0.85rem' }}
            value={minPrice || ''}
            onChange={(e) => onFilterChange({ minPrice: e.target.value ? Number(e.target.value) : undefined })}
          />
          <span style={{ color: 'var(--text-light)' }}>-</span>
          <input
            type="number"
            placeholder="Max"
            className="form-control"
            style={{ padding: '0.4rem 0.6rem', fontSize: '0.85rem' }}
            value={maxPrice || ''}
            onChange={(e) => onFilterChange({ maxPrice: e.target.value ? Number(e.target.value) : undefined })}
          />
        </div>
      </div>
    </aside>
  );
};
