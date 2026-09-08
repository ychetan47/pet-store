-- V2__seed_v2_inventory.sql
-- Seed initial inventory for the 35 products from V2 so existing catalog stock is preserved

INSERT INTO inventory_items (product_id, available_quantity, reserved_quantity) VALUES
-- Dog Products
(101, 45, 0),
(102, 60, 0),
(103, 25, 0),
(104, 80, 0),
(105, 40, 0),
(106, 30, 0),
(107, 50, 0),
(108, 95, 0),
(109, 120, 0),
(110, 35, 0),
(111, 40, 0),
(112, 25, 0),
(113, 18, 0),
(114, 30, 0),
(115, 75, 0),
(116, 60, 0),
(117, 22, 0),
(118, 50, 0),
-- Cat Products
(201, 65, 0),
(202, 35, 0),
(203, 28, 0),
(204, 70, 0),
(205, 85, 0),
(206, 45, 0),
(207, 55, 0),
(208, 110, 0),
(209, 45, 0),
(210, 15, 0),
(211, 30, 0),
(212, 40, 0),
(213, 12, 0),
(214, 65, 0),
(215, 90, 0),
(216, 25, 0),
(217, 35, 0)
ON CONFLICT (product_id) DO NOTHING;
