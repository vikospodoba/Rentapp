-- SQL скрипт для удаления клиентов John Doe и Jane Smith
-- Внимание: это удалит всех связанных данных (аренды, отзывы, платежи)

-- Удаление отзывов, связанных с арендами этих клиентов
DELETE FROM reviews WHERE rental_id IN (
    SELECT id FROM rentals WHERE user_id IN (
        SELECT id FROM users WHERE username IN ('client1', 'client2')
    )
);

-- Удаление платежей, связанных с арендами этих клиентов
DELETE FROM payments WHERE rental_id IN (
    SELECT id FROM rentals WHERE user_id IN (
        SELECT id FROM users WHERE username IN ('client1', 'client2')
    )
);

-- Удаление аренд этих клиентов (каскадное удаление должно автоматически удалить связанные данные)
DELETE FROM rentals WHERE user_id IN (
    SELECT id FROM users WHERE username IN ('client1', 'client2')
);

-- Удаление самих клиентов
DELETE FROM users WHERE username IN ('client1', 'client2');

-- Обновление статусов автомобилей, которые были арендованы
UPDATE cars SET status = 'AVAILABLE' WHERE status = 'RENTED' AND id IN (
    SELECT car_id FROM rentals WHERE user_id IN (
        SELECT id FROM users WHERE username IN ('client1', 'client2')
    )
);





