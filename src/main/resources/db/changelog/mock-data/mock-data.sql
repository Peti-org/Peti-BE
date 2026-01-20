-- liquibase formatted sql
SET search_path TO peti;
-- changeset lyndexter:1701096714332-1

-- 1. city
INSERT INTO city (longitude, latitude, country, country_code, city, location_info)
VALUES
  (30.5234, 50.4501, 'Ukraine', 'UA', 'Kyiv', 'Capital of Ukraine'),
  (24.0297, 49.8397, 'Ukraine', 'UA', 'Lviv', 'Western Ukraine cultural center'),
  (35.0462, 48.4647, 'Ukraine', 'UA', 'Dnipro', 'Eastern Ukraine industrial center'),
  (22.0462, 12.4647, 'Great Britain', 'GB', 'London', 'London is a capital of Great Britain'),
  (12.0462, 48.4647, 'USA', 'US', 'New York', 'New York is a city in the USA');

-- 2. location
INSERT INTO location (location_id, longitude, latitude, address)
VALUES
  (1, 30.5234, 50.4501, '{
    "street": "Khreshchatyk",
    "building": "22",
    "zip": "01001"
  }'),
  (2, 24.0297, 49.8397, '{
    "street": "Rynok Square",
    "building": "1",
    "zip": "79000"
  }'),
  (3, 35.0462, 48.4647, '{
    "street": "Yavornytskoho Ave",
    "building": "50",
    "zip": "49000"
  }');

-- 3. role
INSERT INTO "role" (role_id, role_name)
VALUES
  (1, 'ADMIN'),
  (2, 'CARETAKER'),
  (3, 'USER'),
  (4, 'UNKNOWN');

-- 4. user
-- Assume passwords here are placeholders (in reality, store hashes)
INSERT INTO "user" (user_id, first_name, last_name, email, birthday, password, location_id, city_id, user_is_deleted,
                    user_data_folder, role_id)
VALUES
  ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'Ivan', 'Petrenko', 'ivan.p@example.com', '1990-05-15',
   'hashed_password_1', 1, 1, false, '/data/users/ivan_p', 4),
  ('b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'Olena', 'Kovalenko', 'olena.k@example.com', '1988-11-20',
   'hashed_password_2', 2, 2, false, '/data/users/olena_k', 3),
  ('c3d4e5f6-a7b8-9012-3456-7890abcdef01', 'Serhiy', 'Melnyk', 'serhiy.m@example.com', '1995-02-10',
   'hashed_password_3', 1, 1, false, '/data/users/serhiy_m', 2),
  ('d4e5f6a7-b8c9-0123-4567-890abcdef012', 'Maria', 'Shevchenko', 'maria.s@example.com', '1992-09-01',
   'hashed_password_4', 3, 3, true, '/data/users/maria_s', 4), -- Deleted user
  ('7dc587a7-9ab7-472b-b3b6-6702c4f8a680', 'Tester', 'Testenov', 'tester@gmail.com', '2000-06-16',
   '$2a$10$DxZ/I/5twj09Mte7Q/W.ceJnZF5o3eg0PRUTMlucttSHAWysuN0Rm', 1, 1, false, 'default', 1),
  ('0a1b2c3d-4e5f-6789-0123-456789abcdef', 'Svitlana', 'Kozlova', 'svitlana.k@example.com', '1998-10-03',
   'hashed_password_7', 2, 2, false, '/data/users/svitlana_k', 2),
  ('1b2c3d4e-5f6a-7890-1234-56789abcdef0', 'Maksym', 'Hryhorov', 'maksym.h@example.com', '1991-01-25',
   'hashed_password_8', 3, 3, false, '/data/users/maksym_h', 2),
  ('2c3d4e5f-6a7b-8901-2345-67890abcdef0', 'Yana', 'Tkachenko', 'yana.t@example.com', '1996-04-12',
   'hashed_password_9', 3, 3, false, '/data/users/yana_t', 2),
  ('3d4e5f6a-7b8c-9012-3456-789abcdef012', 'Oleh', 'Kravchenko', 'oleh.k@example.com', '1987-12-05',
   'hashed_password_10', 1, 1, false, '/data/users/oleh_k', 2);

-- 5. breed
INSERT INTO breed (pet_type, breed_name)
VALUES
  ('Dog', 'Labrador Retriever'),
  ('Dog', 'German Shepherd'),
  ('Cat', 'Siamese'),
  ('Cat', 'British Shorthair'),
  ('Parrot', 'Cockatiel');

-- 6. pet
INSERT INTO pet (pet_id, user_id, name, birthday, breed_id, context, pet_data_folder)
VALUES
  ('e5f6a7b8-c9d0-1234-5678-90abcdef0123', 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'Buddy', '2020-01-10', 1, '{
    "color": "Golden",
    "notes": "Loves fetch"
  }', '/data/pets/buddy'),
  ('f6a7b8c9-d0e1-2345-6789-0abcdef01234', 'b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'Luna', '2019-06-25', 4, '{
    "color": "Grey",
    "notes": "Likes to sleep"
  }', '/data/pets/luna'),
  ('a7b8c9d0-e1f2-3456-7890-bcdef0123456', 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'Max', '2021-11-01', 2, '{
    "color": "Black and Tan",
    "notes": "Very active"
  }', '/data/pets/max'),
  ('b8c9d0e1-f2a3-4567-8901-cdef01234567', 'c3d4e5f6-a7b8-9012-3456-7890abcdef01', 'Kesha', '2022-03-15', 5, '{
    "color": "Yellow and Grey",
    "notes": "Sings often"
  }', '/data/pets/kesha');

-- 7. caretaker
-- Let's make Olena and Serhiy caretakers
INSERT INTO caretaker (caretaker_id, user_id, caretaker_preference, caretaker_is_deleted, rating)
VALUES
  ('c1d2e3f4-a5b6-7890-1234-567890abcdef', 'b2c3d4e5-f6a7-8901-2345-67890abcdef0', '{
    "preferred_pets": [
      "Cat"
    ],
    "availability": "Weekends",
    "rate_per_hour": 150.00
  }', false, 50),
  ('d2e3f4a5-b6c7-8901-2345-67890abcdef0', 'c3d4e5f6-a7b8-9012-3456-7890abcdef01', '{
    "preferred_pets": [
      "Dog",
      "Parrot"
    ],
    "availability": "Weekdays 9-18",
    "experience_years": 3
  }', false, 50),
  ('12345678-90ab-cdef-1234-567890abcdef', 'b2c3d4e5-f6a7-8901-2345-67890abcdef0', '{
    "preferred_pets": [
      "Dog"
    ],
    "availability": "Evenings",
    "rate_per_hour": 200.00
  }', false, 45),
  ('abcdef01-2345-6789-abcd-ef0123456789', '0a1b2c3d-4e5f-6789-0123-456789abcdef', '{
    "preferred_pets": [
      "Reptile"
    ],
    "availability": "Flexible",
    "specialty": "Exotic pets"
  }', false, 48),
  ('fedcba98-7654-3210-fedc-ba9876543210', '3d4e5f6a-7b8c-9012-3456-789abcdef012', '{
    "preferred_pets": [
      "Bird"
    ],
    "availability": "Mornings",
    "rate_per_hour": 100.00
  }', false, 42),
  ('98765432-10fe-dcba-9876-543210abcdef', '2c3d4e5f-6a7b-8901-2345-67890abcdef0', '{
    "preferred_pets": [
      "Cat", "Dog"
    ],
    "availability": "Weekends and Holidays",
    "experience_years": 5
  }', false, 50);

-- 8. payment_settings
INSERT INTO payment_settings (user_id, payment_id, card_number, cvv, pin)
VALUES
  ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 1, '4111********1111', '123', '1111'), -- Ivan's card
  ('b2c3d4e5-f6a7-8901-2345-67890abcdef0', 1, '5222********2222', '456', '2222'), -- Olena's card
  ('b2c3d4e5-f6a7-8901-2345-67890abcdef0', 2, '4333********3333', '789', '3333'), -- Olena's second card
  ('c3d4e5f6-a7b8-9012-3456-7890abcdef01', 1, '4444********4444', '101', '4444');
-- Serhiy's card

-- 11. caretaker_slot
INSERT INTO "caretaker_slot" (slot_id, caretaker_id, date, time_from, time_to, type, price, currency, additional_data, creation_time, is_available)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2023-10-26', '15:30:00',
   '16:30:00', 'вигул', 200.00, 'UAH', '{
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567",
    "notes": "Client will bring the pet"
  }', '2025-07-27 10:34:00', true),
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2023-11-01', '11:00:00',
   '12:30:00', 'вигул', 350.00, 'UAH', '{
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567",
    "notes": "Client will bring the pet"
  }', '2025-07-27 10:26:00', true),
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2023-11-02', '18:00:00',
   '19:30:00', 'вигул', 1000.00, 'UAH', '{
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567",
    "notes": "Client will bring the pet"
  }', '2025-07-27 10:30:00', true);

-- 12. event
INSERT INTO event (event_id, user_id, caretaker_id, slot_id, price, status, created_at, event_is_deleted)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567890abcdef', 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
   'd2e3f4a5-b6c7-8901-2345-67890abcdef0', 'e1f2a3b4-c5d6-7890-1234-567340abcdef', '{
    "price": "22.44",
    "currency": "UAH",
    "priceBreakdown": [{"type":"discount", "price":"12.4"}]
  }', 'created', '2023-11-05 14:00:00', false);

-- 9. "order"
-- Orders related to the events above
INSERT INTO "order" (order_id, creation_time, price, currency, client_id, caretaker_id, event_id, order_is_deleted)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', '2023-10-26 15:30:00', 200.00, 'UAH',
   'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'e1f2a3b4-c5d6-7890-1234-567890abcdef', false),
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', '2023-11-01 11:00:00', 350.00, 'UAH',
   'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'e1f2a3b4-c5d6-7890-1234-567890abcdef', false),
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', '2023-11-02 18:00:00', 1000.00, 'UAH',
   'b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'c1d2e3f4-a5b6-7890-1234-567890abcdef',
   'e1f2a3b4-c5d6-7890-1234-567890abcdef', false);
-- Note: Event 'b4c5d6e7-f8a9-0123-4567-890abcdef012' doesn't have a caretaker, so no order can be created for it based on the schema (caretaker_id NOT NULL in order).

-- 10. order_modification
INSERT INTO order_modification (order_id, modification_id, type, time)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', 1, 'TIME_CHANGE', '2023-10-27 08:00:00'), -- Changed time for first order
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', 1, 'NOTES_ADDED', '2023-11-03 10:00:00'), -- Added notes to third order
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', 2, 'DURATION_EXTENDED', '2023-11-04 12:00:00');
-- Extended duration for third order
