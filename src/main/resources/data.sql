-- Тестовая категория
INSERT INTO categories (name) VALUES ('Молочные продукты');
INSERT INTO categories (name) VALUES ('Бакалея');

-- Тестовое описание продуктов
INSERT INTO product_info (name, barcode, min_quantity, category_id)
VALUES ('Молоко 3.2% Простоквашино', '4601234567890', 2.0, 1);

INSERT INTO product_info (name, barcode, min_quantity, category_id)
VALUES ('Макароны Макфа', '4609876543210', 1.0, 2);