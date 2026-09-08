import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ProductSummary, PageResponse } from '../types';
import { productService, ProductFilters as FilterParams } from '../services/productService';
import { ProductGrid } from '../components/product/ProductGrid';
import { ProductFilters } from '../components/product/ProductFilters';
import { Pagination } from '../components/common/Pagination';
import { Breadcrumb } from '../components/common/Breadcrumb';
import { ArrowUpDown } from 'lucide-react';

export const ProductListingPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [productsData, setProductsData] = useState<PageResponse<ProductSummary> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const petParam = searchParams.get('pet') || undefined;
  const categoryIdParam = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined;
  const brandParam = searchParams.get('brand') || undefined;
  const searchParam = searchParams.get('search') || undefined;
  const minPriceParam = searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined;
  const maxPriceParam = searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined;
  const pageParam = searchParams.get('page') ? Number(searchParams.get('page')) : 0;
  const sortParam = searchParams.get('sort') || 'createdAt,desc';

  const [sortBy, sortDirection] = sortParam.split(',') as [string, 'asc' | 'desc'];

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const filters: FilterParams = {
          pet: petParam,
          categoryId: categoryIdParam,
          brand: brandParam,
          search: searchParam,
          minPrice: minPriceParam,
          maxPrice: maxPriceParam,
          page: pageParam,
          size: 12,
          sortBy: sortBy || 'createdAt',
          sortDirection: sortDirection || 'desc',
        };
        const res = await productService.getProducts(filters);
        setProductsData(res);
      } catch (err) {
        console.error('Failed to load products', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, [petParam, categoryIdParam, brandParam, searchParam, minPriceParam, maxPriceParam, pageParam, sortParam]);

  const updateFilters = (newFilters: Partial<FilterParams>) => {
    const newParams = new URLSearchParams(searchParams);

    if (newFilters.pet !== undefined) {
      if (newFilters.pet) newParams.set('pet', newFilters.pet);
      else newParams.delete('pet');
    }

    if (newFilters.categoryId !== undefined) {
      if (newFilters.categoryId) newParams.set('categoryId', String(newFilters.categoryId));
      else newParams.delete('categoryId');
    }

    if (newFilters.brand !== undefined) {
      if (newFilters.brand) newParams.set('brand', newFilters.brand);
      else newParams.delete('brand');
    }

    if (newFilters.minPrice !== undefined) {
      if (newFilters.minPrice) newParams.set('minPrice', String(newFilters.minPrice));
      else newParams.delete('minPrice');
    }

    if (newFilters.maxPrice !== undefined) {
      if (newFilters.maxPrice) newParams.set('maxPrice', String(newFilters.maxPrice));
      else newParams.delete('maxPrice');
    }

    newParams.set('page', '0');
    setSearchParams(newParams);
  };

  const handleResetFilters = () => {
    setSearchParams({});
  };

  const handleSortChange = (value: string) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('sort', value);
    newParams.set('page', '0');
    setSearchParams(newParams);
  };

  const handlePageChange = (page: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', String(page));
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  let petTitle = 'All Products';
  if (petParam === 'dogs') petTitle = 'Dogs Products';
  if (petParam === 'cats') petTitle = 'Cats Products';
  if (searchParam) petTitle = `Search: "${searchParam}"`;

  return (
    <div className="container" style={{ paddingBottom: '3rem' }}>
      <Breadcrumb items={[{ label: 'Products', path: '/products' }, { label: petTitle }]} />

      <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: '2rem', alignItems: 'start' }}>
        <ProductFilters
          selectedPet={petParam}
          selectedCategoryId={categoryIdParam}
          selectedBrand={brandParam}
          minPrice={minPriceParam}
          maxPrice={maxPriceParam}
          onFilterChange={updateFilters}
          onReset={handleResetFilters}
        />

        <div>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: '#fff',
              padding: '1rem 1.5rem',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--border-color)',
              marginBottom: '1.5rem',
              flexWrap: 'wrap',
              gap: '1rem',
            }}
          >
            <div>
              <h2 style={{ fontSize: '1.4rem' }}>{petTitle}</h2>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Showing {productsData?.totalElements || 0} products
              </span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <ArrowUpDown size={16} color="var(--text-muted)" />
              <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>Sort By:</span>
              <select
                className="form-control"
                style={{ padding: '0.4rem 0.75rem', width: 'auto', fontSize: '0.9rem' }}
                value={sortParam}
                onChange={(e) => handleSortChange(e.target.value)}
              >
                <option value="createdAt,desc">Newest Arrivals</option>
                <option value="price,asc">Price: Low to High</option>
                <option value="price,desc">Price: High to Low</option>
                <option value="name,asc">Name: A to Z</option>
              </select>
            </div>
          </div>

          <ProductGrid products={productsData?.content || []} loading={loading} />

          {productsData && (
            <Pagination
              currentPage={productsData.page ?? productsData.pageNumber ?? 0}
              totalPages={productsData.totalPages}
              onPageChange={handlePageChange}
            />
          )}
        </div>
      </div>
    </div>
  );
};
