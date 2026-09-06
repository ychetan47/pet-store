-- V2__insert_categories.sql
-- Seed categories for Dogs and Cats hierarchy

-- Root Categories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(1, 'Dogs', 'dogs', NULL, 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80', TRUE),
(2, 'Cats', 'cats', NULL, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80', TRUE);

-- Dog Main Categories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(10, 'Dog Food', 'dog-food', 1, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE),
(20, 'Dog Accessories', 'dog-accessories', 1, 'https://images.unsplash.com/photo-1576201836106-db1758fd1c97?auto=format&fit=crop&w=800&q=80', TRUE),
(30, 'Dog Toys', 'dog-toys', 1, 'https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?auto=format&fit=crop&w=800&q=80', TRUE),
(40, 'Dog Grooming', 'dog-grooming', 1, 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=800&q=80', TRUE);

-- Dog Food Subcategories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(11, 'Dry Dog Food', 'dog-dry-food', 10, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE),
(12, 'Wet Dog Food', 'dog-wet-food', 10, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', TRUE),
(13, 'Puppy Food', 'dog-puppy-food', 10, 'https://images.unsplash.com/photo-1591768575198-88dac53fbd0a?auto=format&fit=crop&w=800&q=80', TRUE),
(14, 'Dog Treats', 'dog-treats', 10, 'https://images.unsplash.com/photo-1582798358481-d199fb7347bb?auto=format&fit=crop&w=800&q=80', TRUE);

-- Dog Accessories Subcategories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(21, 'Dog Collars', 'dog-collars', 20, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?auto=format&fit=crop&w=800&q=80', TRUE),
(22, 'Dog Leashes', 'dog-leashes', 20, 'https://images.unsplash.com/photo-1535294435445-d7249524ef2e?auto=format&fit=crop&w=800&q=80', TRUE),
(23, 'Dog Harnesses', 'dog-harnesses', 20, 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=800&q=80', TRUE),
(24, 'Dog Beds', 'dog-beds', 20, 'https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?auto=format&fit=crop&w=800&q=80', TRUE),
(25, 'Dog Bowls', 'dog-bowls', 20, 'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80', TRUE);

-- Cat Main Categories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(50, 'Cat Food', 'cat-food', 2, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE),
(60, 'Cat Accessories', 'cat-accessories', 2, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE),
(70, 'Cat Toys', 'cat-toys', 2, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE),
(80, 'Cat Grooming', 'cat-grooming', 2, 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80', TRUE);

-- Cat Food Subcategories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(51, 'Dry Cat Food', 'cat-dry-food', 50, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE),
(52, 'Wet Cat Food', 'cat-wet-food', 50, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', TRUE),
(53, 'Kitten Food', 'cat-kitten-food', 50, 'https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=800&q=80', TRUE),
(54, 'Cat Treats', 'cat-treats', 50, 'https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=800&q=80', TRUE);

-- Cat Accessories Subcategories
INSERT INTO categories (id, name, slug, parent_id, image_url, is_active) VALUES
(61, 'Litter Boxes', 'cat-litter-boxes', 60, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE),
(62, 'Cat Bowls', 'cat-bowls', 60, 'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80', TRUE),
(63, 'Cat Beds', 'cat-beds', 60, 'https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?auto=format&fit=crop&w=800&q=80', TRUE),
(64, 'Cat Carriers', 'cat-carriers', 60, 'https://images.unsplash.com/photo-1561948955-570b270e7c36?auto=format&fit=crop&w=800&q=80', TRUE);

-- Adjust sequence to continue after highest ID
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
