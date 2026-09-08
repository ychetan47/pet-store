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
-- V3__insert_products.sql
-- Seed products and product images for Dogs and Cats

-- DOG PRODUCTS
INSERT INTO products (id, name, slug, description, brand, price, stock_quantity, category_id, weight, status) VALUES
-- 1. Dry Dog Food
(101, 'Royal Canin Maxi Adult Dry Dog Food', 'royal-canin-maxi-adult', 'Complete diet designed specifically for large adult dogs weighing 26 to 44 kg. Supports digestive health and joint vitality.', 'Royal Canin', 2850.00, 45, 11, '4 kg', 'ACTIVE'),
(102, 'Pedigree Pro Expert Nutrition Adult Dog Food', 'pedigree-pro-adult', 'Professional nutrition with high protein and active joint protection formula for active adult dogs.', 'Pedigree', 1450.00, 60, 11, '3 kg', 'ACTIVE'),
(103, 'Farmina N&D Grain-Free Pumpkin Lamb & Blueberry', 'farmina-nd-grain-free-lamb', 'Grain-free natural formula rich in animal protein, low glycemic index, with pumpkin and fresh blueberries.', 'Farmina', 3600.00, 25, 11, '2.5 kg', 'ACTIVE'),

-- 2. Wet Dog Food
(104, 'Pedigree Wet Dog Food Chicken & Liver Chunks in Gravy', 'pedigree-wet-chicken-liver-gravy', 'Tender meaty morsels in savory gravy, offering optimal moisture and taste that dogs instinctively love.', 'Pedigree', 480.00, 80, 12, '12 x 100g', 'ACTIVE'),
(105, 'Royal Canin Canine Care Nutrition Wet Food Pouch', 'royal-canin-wet-pouch', 'Precision nutrition wet food designed to support skin and coat health with balanced omega fatty acids.', 'Royal Canin', 950.00, 40, 12, '10 x 85g', 'ACTIVE'),

-- 3. Puppy Food
(106, 'Royal Canin Medium Puppy Dry Food', 'royal-canin-medium-puppy', 'Formulated for medium breed puppies (11-25 kg) up to 12 months. Enhanced immune system support and prebiotics.', 'Royal Canin', 3150.00, 30, 13, '4 kg', 'ACTIVE'),
(107, 'Drools Focus Starter Puppy Dog Food', 'drools-focus-starter-puppy', 'Nutrient-rich starter diet for weaning puppies up to 3 months of age and lactating mother dogs.', 'Drools', 1250.00, 50, 13, '2 kg', 'ACTIVE'),

-- 4. Dog Treats
(108, 'Pedigree Dentastix Daily Dental Chews (Large Dogs)', 'pedigree-dentastix-large', 'Scientifically proven to reduce tartar buildup by up to 80% when fed daily. Unique X-shape design.', 'Pedigree', 620.00, 95, 14, '28 Sticks', 'ACTIVE'),
(109, 'JerHigh Real Meat Chicken Jerky Dog Treats', 'jerhigh-chicken-jerky', 'Premium treats made from real chicken meat, rich in protein and low in fat, ideal for training rewards.', 'JerHigh', 340.00, 120, 14, '100g', 'ACTIVE'),

-- 5. Dog Collars & Leashes & Harnesses
(110, 'BarkBoutique Padded Reflective Dog Collar', 'barkboutique-padded-collar', 'Durable nylon collar featuring soft neoprene padding and 3M reflective stitching for night safety.', 'BarkBoutique', 699.00, 35, 21, 'Size L', 'ACTIVE'),
(111, 'KONG Heavy-Duty Hands-Free Bungee Leash', 'kong-heavy-duty-bungee-leash', 'Shock-absorbing bungee leash with traffic handle and reinforced zinc-alloy clasp for ultimate control.', 'KONG', 1299.00, 40, 22, '6 ft', 'ACTIVE'),
(112, 'Truelove No-Pull Ergonomic Dog Harness', 'truelove-no-pull-harness', 'Dual-clip ergonomic harness with breathable mesh lining that gently discourages pulling on walks.', 'Truelove', 1850.00, 25, 23, 'Size XL', 'ACTIVE'),

-- 6. Dog Beds & Bowls
(113, 'FurHaven Orthopedic Memory Foam Dog Bed', 'furhaven-orthopedic-bed', 'Therapeutic orthopedic convoluted foam mattress designed to soothe aching joints and pressure points.', 'FurHaven', 3499.00, 18, 24, 'Large (90x70cm)', 'ACTIVE'),
(114, 'Zee.Dog Elevated Stainless Steel Double Bowl', 'zeedog-elevated-double-bowl', 'Ergonomic raised feeder promotes healthy posture during meals. Includes two rust-resistant bowls.', 'Zee.Dog', 1599.00, 30, 25, '850 ml each', 'ACTIVE'),

-- 7. Dog Toys
(115, 'KONG Classic Durable Rubber Chew Toy', 'kong-classic-rubber-toy', 'The gold standard of dog toys. Super-bouncy red natural rubber compound for erratic bounces and treat stuffing.', 'KONG', 890.00, 75, 30, 'Large', 'ACTIVE'),
(116, 'Chuckit! Ultra Ball High-Bounce Fetch Toy', 'chuckit-ultra-ball', 'High-bounce, floating natural rubber balls engineered for demanding games of fetch.', 'Chuckit!', 750.00, 60, 30, '2-Pack Medium', 'ACTIVE'),

-- 8. Dog Grooming
(117, 'FURminator Undercoat Deshedding Tool for Dogs', 'furminator-undercoat-deshedding-dog', 'Reaches deep beneath topcoat to safely and easily remove loose hair and undercoat without damaging skin.', 'FURminator', 1999.00, 22, 40, 'Large Long Hair', 'ACTIVE'),
(118, 'Bio-Groom So-Gentle Hypoallergenic Dog Shampoo', 'biogroom-hypoallergenic-shampoo', 'Tearless, dye-free, and fragrance-free conditioning shampoo for sensitive pets.', 'Bio-Groom', 850.00, 50, 40, '355 ml', 'ACTIVE'),

-- CAT PRODUCTS
-- 9. Dry Cat Food
(201, 'Whiskas Ocean Fish Adult Dry Cat Food', 'whiskas-ocean-fish-adult', 'Crunchy pockets with real fish flavor, enriched with vitamins, minerals, and taurine for eyes and heart.', 'Whiskas', 820.00, 65, 51, '3 kg', 'ACTIVE'),
(202, 'Royal Canin Hairball Care Adult Cat Food', 'royal-canin-hairball-care', 'Formulated with specific blend of dietary fibres, including psyllium, to naturally stimulate intestinal transit.', 'Royal Canin', 2490.00, 35, 51, '2 kg', 'ACTIVE'),
(203, 'Purina Pro Plan Adult Salmon Sensitive Skin & Stomach', 'purina-pro-plan-salmon-cat', 'High-protein diet crafted with real salmon as the first ingredient and live prebiotics for digestion.', 'Purina', 2950.00, 28, 51, '2.5 kg', 'ACTIVE'),

-- 10. Wet Cat Food
(204, 'Sheba Delicacy Tuna & Salmon Fillet in Gravy', 'sheba-tuna-salmon-fillet', 'Gourmet cat wet food crafted from premium flaked fish cuts gently simmered in exquisite sauce.', 'Sheba', 650.00, 70, 52, '12 x 70g', 'ACTIVE'),
(205, 'Whiskas Wet Cat Food Chicken & Salmon in Gravy', 'whiskas-wet-chicken-salmon', 'Complete and balanced meal providing hydration and essential zinc for lustrous coat health.', 'Whiskas', 490.00, 85, 52, '12 x 85g', 'ACTIVE'),

-- 11. Kitten Food
(206, 'Royal Canin Mother & Babycat Ultra Soft Mousse', 'royal-canin-babycat-mousse', 'Ultra-soft mousse texture facilitates transition to solid food for kittens under 4 months and queens.', 'Royal Canin', 1150.00, 45, 53, '6 x 195g', 'ACTIVE'),
(207, 'Whiskas Junior Ocean Fish Kitten Dry Food', 'whiskas-junior-ocean-fish', 'Formulated with DHA and colostrum nutrients to support healthy brain and vision development in kittens.', 'Whiskas', 580.00, 55, 53, '1.1 kg', 'ACTIVE'),

-- 12. Cat Treats
(208, 'Temptations Creamy Purrrr-ee Chicken Cat Treats', 'temptations-creamy-puree-chicken', 'Irresistible creamy puree lickable treat made with real chicken and vitamin E, served by hand or topping.', 'Temptations', 240.00, 110, 54, '4 x 12g', 'ACTIVE'),
(209, 'Cat-Man-Doo Extra Large Dried Bonito Flakes', 'catmandoo-bonito-flakes', 'Single ingredient human-grade freeze-dried wild bonito flakes that cats go crazy for.', 'Cat-Man-Doo', 590.00, 45, 54, '50g', 'ACTIVE'),

-- 13. Cat Accessories: Litter Boxes, Bowls, Beds, Carriers
(210, 'Modkat Flip High-Sided Anti-Scatter Litter Box', 'modkat-flip-litter-box', 'Modern 3-position lid litter box that eliminates mess and litter tracking with seamless reusable liner.', 'Modkat', 3899.00, 15, 61, 'Standard', 'ACTIVE'),
(211, 'Catit Multi-Level Stainless Steel Flower Fountain & Bowl', 'catit-flower-fountain-bowl', 'Triple-action filtered fresh running water fountain encouraging healthy feline hydration.', 'Catit', 1890.00, 30, 62, '3 Litres', 'ACTIVE'),
(212, 'MeowHut Cozy Plush Donut Round Cat Bed', 'meowhut-plush-donut-bed', 'Self-warming ultra-soft faux fur donut bolster bed engineered for burrowing and deep sleep.', 'MeowHut', 1250.00, 40, 63, '50cm Diameter', 'ACTIVE'),
(213, 'Sleepypod Air Airline-Approved Luxury Cat Carrier', 'sleepypod-air-luxury-carrier', 'Collapsible cabin-certified pet carrier with padded shoulder strap and luggage trolley sleeve.', 'Sleepypod', 4999.00, 12, 64, 'Up to 8 kg', 'ACTIVE'),

-- 14. Cat Toys
(214, 'KONG Cat Laser Pointer & Teaser Wand Combo', 'kong-laser-wand-combo', 'Interactive play duo to trigger the natural stalking and pouncing instincts of indoor cats.', 'KONG', 650.00, 65, 70, 'One Size', 'ACTIVE'),
(215, 'Cat Dancer 101 Original Wire Cat Toy', 'cat-dancer-original-wire', 'Simple yet mesmerizing springboard wire action with cardboard rolls that creates erratic, lifelike flight.', 'Cat Dancer', 320.00, 90, 70, 'Standard', 'ACTIVE'),

-- 15. Cat Grooming
(216, 'FURminator Undercoat Deshedding Tool for Small Cats', 'furminator-deshedding-cat', 'Reduces shedding and hairball formation by gently removing loose undercoat hair.', 'FURminator', 1850.00, 25, 80, 'Small Short Hair', 'ACTIVE'),
(217, 'Burt''s Bees for Cats Natural Dander Reducing Spray', 'burts-bees-cat-dander-spray', 'Infused with colloidal oat flour and aloe vera to soothe dry skin and reduce airborne pet dander.', 'Burt''s Bees', 799.00, 35, 80, '296 ml', 'ACTIVE');

-- PRODUCT IMAGES (Multiple images per product with primary flag)
INSERT INTO product_images (product_id, image_url, is_primary, display_order) VALUES
-- 101 Royal Canin Maxi
(101, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE, 1),
(101, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', FALSE, 2),

-- 102 Pedigree Pro
(102, 'https://images.unsplash.com/photo-1591768575198-88dac53fbd0a?auto=format&fit=crop&w=800&q=80', TRUE, 1),
(102, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', FALSE, 2),

-- 103 Farmina N&D
(103, 'https://images.unsplash.com/photo-1582798358481-d199fb7347bb?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 104 Pedigree Wet
(104, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 105 Royal Canin Wet
(105, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 106 Royal Canin Medium Puppy
(106, 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80', TRUE, 1),
(106, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', FALSE, 2),

-- 107 Drools Focus Starter
(107, 'https://images.unsplash.com/photo-1591768575198-88dac53fbd0a?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 108 Pedigree Dentastix
(108, 'https://images.unsplash.com/photo-1582798358481-d199fb7347bb?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 109 JerHigh
(109, 'https://images.unsplash.com/photo-1582798358481-d199fb7347bb?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 110 BarkBoutique Collar
(110, 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 111 KONG Bungee Leash
(111, 'https://images.unsplash.com/photo-1535294435445-d7249524ef2e?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 112 Truelove Harness
(112, 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 113 FurHaven Orthopedic Bed
(113, 'https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 114 ZeeDog Bowls
(114, 'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 115 KONG Rubber Toy
(115, 'https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 116 Chuckit Ball
(116, 'https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 117 FURminator Dog
(117, 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 118 Bio-Groom Shampoo
(118, 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 201 Whiskas Ocean Fish
(201, 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 202 Royal Canin Hairball
(202, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 203 Purina Pro Plan Salmon
(203, 'https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 204 Sheba Delicacy
(204, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 205 Whiskas Wet
(205, 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 206 Royal Canin Babycat Mousse
(206, 'https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 207 Whiskas Junior
(207, 'https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 208 Temptations Creamy Puree
(208, 'https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 209 Cat-Man-Doo Bonito Flakes
(209, 'https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 210 Modkat Flip Litter Box
(210, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 211 Catit Flower Fountain
(211, 'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 212 MeowHut Plush Bed
(212, 'https://images.unsplash.com/photo-1598133894008-61f7fdb8cc3a?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 213 Sleepypod Carrier
(213, 'https://images.unsplash.com/photo-1561948955-570b270e7c36?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 214 KONG Laser Pointer
(214, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 215 Cat Dancer Toy
(215, 'https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 216 FURminator Cat
(216, 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80', TRUE, 1),

-- 217 Burt''s Bees Cat Spray
(217, 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=800&q=80', TRUE, 1);

-- Adjust sequence to continue after highest ID
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
