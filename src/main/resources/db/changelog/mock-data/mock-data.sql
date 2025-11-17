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

-- 9. event
INSERT INTO event (event_id, event_time, user_id, event_name, caretaker_id, event_context, event_is_deleted)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567890abcdef', '2023-10-27 10:00:00', 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
   'Dog Walking for Buddy', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '{
    "pet_id": "e5f6a7b8-c9d0-1234-5678-90abcdef0123",
    "duration_hours": 1,
    "location": "Client home pickup"
  }', false),
  ('f2a3b4c5-d6e7-8901-2345-67890abcdef0', '2023-11-05 14:00:00', 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
   'Dog training session for Max', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '{
    "pet_id": "a7b8c9d0-e1f2-3456-7890-bcdef0123456",
    "duration_hours": 1.5,
    "focus": "Obedience"
  }', false),
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef01', '2023-11-10 09:00:00', 'b2c3d4e5-f6a7-8901-2345-67890abcdef0',
   'Cat Sitting for Luna', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '{
    "pet_id": "f6a7b8c9-d0e1-2345-6789-0abcdef01234",
    "duration_days": 3,
    "location": "Caretaker home"
  }', false),
  ('b4c5d6e7-f8a9-0123-4567-890abcdef012', '2023-10-28 12:00:00', 'c3d4e5f6-a7b8-9012-3456-7890abcdef01',
   'Parrot checkup for Kesha', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '{
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567",
    "notes": "Client will bring the pet"
  }', false);
-- Event without caretaker initially

-- 10. "order"
-- Orders related to the events above
INSERT INTO "order" (order_id, creation_time, price, currency, client_id, caretaker_id, event_id, order_is_deleted)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', '2023-10-26 15:30:00', 200.00, 'UAH',
   'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'e1f2a3b4-c5d6-7890-1234-567890abcdef', false),
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', '2023-11-01 11:00:00', 350.00, 'UAH',
   'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'f2a3b4c5-d6e7-8901-2345-67890abcdef0', false),
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', '2023-11-02 18:00:00', 1000.00, 'UAH',
   'b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'c1d2e3f4-a5b6-7890-1234-567890abcdef',
   'a3b4c5d6-e7f8-9012-3456-7890abcdef01', false);
-- Note: Event 'b4c5d6e7-f8a9-0123-4567-890abcdef012' doesn't have a caretaker, so no order can be created for it based on the schema (caretaker_id NOT NULL in order).

-- 11. order_modification
INSERT INTO order_modification (order_id, modification_id, type, time)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', 1, 'TIME_CHANGE', '2023-10-27 08:00:00'), -- Changed time for first order
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', 1, 'NOTES_ADDED', '2023-11-03 10:00:00'), -- Added notes to third order
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', 2, 'DURATION_EXTENDED', '2023-11-04 12:00:00');
-- Extended duration for third order

-- 12. caretaker_slot
INSERT INTO "caretaker_slot" (slot_id, caretaker_id, date, time_from, time_to, type, price, currency, additional_data, creation_time)
VALUES
  ('e1f2a3b4-c5d6-7890-1234-567340abcdef', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-15', '09:00:00', '10:30:00', 'walking', 250.00, 'UAH', '{
    "notes": "Client will bring the pet",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-27 10:34:00'),
  ('e1f2a3b4-c5d6-7890-1234-567890a56def', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-20', '14:00:00', '15:30:00', 'sitting', 450.00, 'UAH', '{
    "notes": "Client will bring the pet",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-27 10:26:00'),
  ('a3b4c5d6-e7f8-9012-3456-7890abcdef03', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-11-22', '18:00:00', '19:30:00', 'grooming', 1200.00, 'UAH', '{
    "notes": "Client will bring the pet",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-27 10:30:00'),
  ('b5c6d7e8-f9a0-1234-5678-90abcdef0123', '12345678-90ab-cdef-1234-567890abcdef', '2025-11-25', '10:00:00', '11:00:00', 'training', 750.00, 'UAH', '{
    "notes": "One-on-one session",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-28 09:15:00'),
  ('c7d8e9f0-a1b2-3456-7890-1234567890ab', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-01', '16:00:00', '17:30:00', 'veterinary', 5000.00, 'UAH', '{
    "notes": "Check-up and vaccinations",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-28 11:40:00'),
  ('d9e0f1a2-b3c4-5678-9012-34567890abcd', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-05', '08:30:00', '09:30:00', 'daycare', 300.00, 'UAH', '{
    "notes": "Full day care",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-07-28 13:00:00'),
  ('e1f2a3b4-c5d6-7890-1234-567890ab0001', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-10', '19:00:00', '20:00:00', 'other', 150.00, 'UAH', '{
    "notes": "Evening play session",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-29 08:00:00'),
  ('f3a4b5c6-d7e8-9012-3456-7890abcdef02', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-12', '11:00:00', '12:00:00', 'walking', 200.00, 'UAH', '{
    "notes": "Short walk in the park",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-29 09:30:00'),
  ('a5b6c7d8-e9f0-1234-5678-90abcdef0124', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-18', '09:00:00', '10:00:00', 'grooming', 1500.00, 'UAH', '{
    "notes": "Full grooming session",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-29 10:45:00'),
  ('b7c8d9e0-f1a2-3456-7890-1234567890ac', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-20', '13:00:00', '14:00:00', 'training', 800.00, 'UAH', '{
    "notes": "Advanced obedience training",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-29 11:00:00'),
  ('c9d0e1f2-a3b4-5678-9012-34567890abce', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-24', '17:00:00', '18:00:00', 'veterinary', 6000.00, 'UAH', '{
    "notes": "Emergency consultation",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-29 12:30:00'),
  ('dbce0f1a-2b3c-4567-8901-234567890abc', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-26', '10:00:00', '11:00:00', 'daycare', 400.00, 'UAH', '{
    "notes": "Half day care",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-07-29 13:45:00'),
  ('f5a6b7c8-d9e0-1234-5678-90abcdef0126', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-21', '10:00:00', '11:00:00', 'other', 300.00, 'UAH', '{
    "notes": "Pet transportation",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-30 14:00:00'),
  ('a7b8c9d0-e1f2-3456-7890-1234567890ae', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-11-23', '16:00:00', '17:00:00', 'walking', 220.00, 'UAH', '{
    "notes": "Afternoon walk",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-30 15:00:00'),
  ('b9c0d1e2-f3a4-5678-9012-34567890ace0', '12345678-90ab-cdef-1234-567890abcdef', '2025-11-24', '19:00:00', '20:00:00', 'sitting', 550.00, 'UAH', '{
    "notes": "Evening pet sitting",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-30 16:00:00'),
  ('cbd0e1f2-a3b4-5678-9012-34567890afff', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-11-27', '08:00:00', '10:00:00', 'grooming', 1000.00, 'UAH', '{
    "notes": "Nail clipping and ear cleaning",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-30 17:00:00'),
  ('d1e2f3a4-b5c6-7890-1234-567890abce00', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-11-28', '15:00:00', '16:00:00', 'training', 600.00, 'UAH', '{
    "notes": "Potty training",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-07-30 18:00:00'),
  ('e3f4a5b6-c7d8-9012-3456-7890abcdef05', '98765432-10fe-dcba-9876-543210abcdef', '2025-11-30', '11:00:00', '12:00:00', 'veterinary', 5500.00, 'UAH', '{
    "notes": "Follow-up check-up",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-30 19:00:00'),
  ('f5a6b7c8-d9e0-1234-5678-90abcdef0127', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-02', '14:00:00', '18:00:00', 'daycare', 800.00, 'UAH', '{
    "notes": "Extended daycare",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-30 20:00:00'),
  ('a7b8c9d0-e1f2-3456-7890-1234567890af', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-04', '20:00:00', '21:00:00', 'other', 400.00, 'UAH', '{
    "notes": "Overnight pet sitting",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-30 21:00:00'),
  ('b9c0d1e2-f3a4-5678-9012-34567890ace1', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-06', '08:00:00', '09:00:00', 'walking', 150.00, 'UAH', '{
    "notes": "Morning walk",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-30 22:00:00'),
  ('cbd0e1f2-a3b4-5678-9012-34567890aff1', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-08', '10:00:00', '11:00:00', 'sitting', 350.00, 'UAH', '{
    "notes": "Quick drop-in visit",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-30 23:00:00'),
  ('d1e2f3a4-b5c6-7890-1234-567890abce01', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-09', '16:00:00', '17:00:00', 'grooming', 950.00, 'UAH', '{
    "notes": "Brush and de-shedding",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-07-31 08:00:00'),
  ('e3f4a5b6-c7d8-9012-3456-7890abcdef06', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-11', '13:00:00', '14:00:00', 'training', 700.00, 'UAH', '{
    "notes": "Leash training",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-31 09:00:00'),
  ('f5a6b7c8-d9e0-1234-5678-90abcdef0128', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-13', '09:00:00', '10:00:00', 'veterinary', 6500.00, 'UAH', '{
    "notes": "Emergency vet visit",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-31 10:00:00'),
  ('a7b8c9d0-e1f2-3456-7890-1234567890a0', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-15', '11:00:00', '15:00:00', 'daycare', 900.00, 'UAH', '{
    "notes": "Half day care",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-31 11:00:00'),
  ('b9c0d1e2-f3a4-5678-9012-34567890ace2', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-16', '18:00:00', '19:00:00', 'other', 450.00, 'UAH', '{
    "notes": "Administering medication",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-31 12:00:00'),
  ('cbd0e1f2-a3b4-5678-9012-34567890aff2', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-19', '20:00:00', '21:00:00', 'walking', 280.00, 'UAH', '{
    "notes": "Late night walk",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-31 13:00:00'),
  ('d1e2f3a4-b5c6-7890-1234-567890abce02', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-21', '08:00:00', '09:00:00', 'sitting', 400.00, 'UAH', '{
    "notes": "Morning drop-in",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-07-31 14:00:00'),
  ('e3f4a5b6-c7d8-9012-3456-7890abcdef07', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-23', '12:00:00', '13:00:00', 'grooming', 1100.00, 'UAH', '{
    "notes": "Quick brush and tidy",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-31 15:00:00'),
  ('f5a6b7c8-d9e0-1234-5678-90abcdef0129', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-27', '17:00:00', '18:00:00', 'training', 720.00, 'UAH', '{
    "notes": "Advanced trick training",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-31 16:00:00'),
  ('a7b8c9d0-e1f2-3456-7890-1234567890a7', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-28', '09:00:00', '10:00:00', 'veterinary', 7500.00, 'UAH', '{
    "notes": "Urgent consultation",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-31 17:00:00'),
  ('b9c0d1e2-f3a4-5678-9012-34567890ace3', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-30', '14:00:00', '16:00:00', 'daycare', 950.00, 'UAH', '{
    "notes": "Full day play",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-31 18:00:00'),
  ('c1d2e3f4-a5b6-7890-1234-567890aff312', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-11-12', '10:00:00', '11:00:00', 'walking', 150.00, 'UAH', '{
    "notes": "Morning walk in the park",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-08-01 08:00:00'),
  ('d3e4f5a6-b7c8-9012-3456-7890abce0323', '98765432-10fe-dcba-9876-543210abcdef', '2025-11-13', '15:00:00', '16:00:00', 'sitting', 400.00, 'UAH', '{
    "notes": "Afternoon pet sitting at home",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 09:00:00'),
  ('e5f6a7b8-c9d0-1234-5678-90abcdef0834', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-14', '18:00:00', '19:30:00', 'grooming', 1300.00, 'UAH', '{
    "notes": "Full grooming with special shampoo",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 10:00:00'),
  ('f7a8b9c0-d1e2-3456-7890-1234567890ad', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-11-15', '09:00:00', '10:00:00', 'training', 850.00, 'UAH', '{
    "notes": "Basic obedience for puppy",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-08-01 11:00:00'),
  ('a9b0c1d2-e3f4-5678-9012-34567890ace4', '12345678-90ab-cdef-1234-567890abcdef', '2025-11-16', '11:00:00', '12:00:00', 'veterinary', 6800.00, 'UAH', '{
    "notes": "Annual check-up",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-08-01 12:00:00'),
  ('bbd0e1f2-a3b4-5678-9012-34567890aff4', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-11-17', '14:00:00', '18:00:00', 'daycare', 900.00, 'UAH', '{
    "notes": "Afternoon daycare",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-08-01 13:00:00'),
  ('cde1f2a3-b4c5-6789-0123-456789abce04', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-11-18', '20:00:00', '21:00:00', 'other', 300.00, 'UAH', '{
    "notes": "Evening play session at home",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-08-01 14:00:00'),
  ('ef2a3b4c-5d6e-7890-1234-567890abcdef', '98765432-10fe-dcba-9876-543210abcdef', '2025-11-19', '08:00:00', '09:00:00', 'walking', 160.00, 'UAH', '{
    "notes": "Early morning brisk walk",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 15:00:00'),
  ('01112131-4154-6789-0123-456789abce05', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-20', '13:00:00', '14:00:00', 'sitting', 380.00, 'UAH', '{
    "notes": "Midday check-in",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 16:00:00'),
  ('5a6b7c8d-9e0f-1234-5678-90abcdef0122', '98765432-10fe-dcba-9876-543210abcdef', '2025-11-25', '19:00:00', '20:00:00', 'other', 200.00, 'UAH', '{
    "notes": "Evening feeding and litter box change",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 21:00:00'),
  ('6e7f8a9b-0c1d-2345-6789-0abcdef01233', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-26', '08:00:00', '09:00:00', 'walking', 170.00, 'UAH', '{
    "notes": "Morning walk",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-01 22:00:00'),
  ('7c8d9e0f-1a2b-3456-7890-1234567890ac', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-11-27', '21:00:00', '22:00:00', 'sitting', 600.00, 'UAH', '{
    "notes": "Late evening pet sitting",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-08-01 23:00:00'),
  ('8b9c0d1e-2f3a-4567-8901-234567890af5', '12345678-90ab-cdef-1234-567890abcdef', '2025-11-28', '14:00:00', '15:30:00', 'grooming', 1700.00, 'UAH', '{
    "notes": "Special breed grooming",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-08-02 08:00:00'),
  ('9d0e1f2a-3b4c-5678-9012-3456790abce8', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-11-29', '11:00:00', '12:00:00', 'training', 950.00, 'UAH', '{
    "notes": "Advanced agility training",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-08-02 09:00:00'),
  ('a0b1c2d3-e4f5-6789-0123-456789abcdef', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-11-30', '16:00:00', '17:00:00', 'veterinary', 7200.00, 'UAH', '{
    "notes": "Specialist follow-up",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-08-02 10:00:00'),
  ('b2c3d4e5-f6a7-8901-2345-67890abcdef2', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-01', '09:00:00', '13:00:00', 'daycare', 980.00, 'UAH', '{
    "notes": "Full morning daycare",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-02 11:00:00'),
  ('c4d5e6f7-a8b9-0123-4567-890abce09141', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-02', '14:00:00', '15:00:00', 'other', 320.00, 'UAH', '{
    "notes": "Pet photosession",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-02 12:00:00'),
  ('e6f7a8b9-c0d1-2345-6789-0abcdef01234', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-03', '10:00:00', '11:00:00', 'walking', 210.00, 'UAH', '{
    "notes": "Morning walk and play",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-08-02 13:00:00'),
  ('f8a9b0c1-d2e3-4567-8901-234567890af6', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-04', '17:00:00', '18:00:00', 'sitting', 480.00, 'UAH', '{
    "notes": "Evening pet sitting",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-08-02 14:00:00'),
  ('10616203-5445-6789-0123-456789abce10', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-05', '08:00:00', '09:30:00', 'grooming', 1400.00, 'UAH', '{
    "notes": "Bath, brush, and nail trim",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-08-02 15:00:00'),
  ('12134455-7687-8901-2345-67890abcdef3', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-06', '12:00:00', '13:00:00', 'training', 780.00, 'UAH', '{
    "notes": "Socialization training",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-08-02 16:00:00'),
  ('a1111111-2222-3333-4444-555555555555', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-07', '15:00:00', '16:00:00', 'veterinary', 6900.00, 'UAH', '{
    "notes": "Second opinion for diagnosis",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-02 17:00:00'),
  ('b2222222-3333-4444-5555-666666666666', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-08', '10:00:00', '14:00:00', 'daycare', 920.00, 'UAH', '{
    "notes": "Full morning daycare with walks",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-02 18:00:00'),
  ('c3333333-4444-5555-6666-777777777777', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-09', '20:00:00', '21:00:00', 'other', 420.00, 'UAH', '{
    "notes": "Evening medication administration",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-08-02 19:00:00'),
  ('d4444444-5555-6666-7777-888888888888', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-10', '08:00:00', '09:00:00', 'walking', 190.00, 'UAH', '{
    "notes": "Early morning power walk",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-08-02 20:00:00'),
  ('e5555555-6666-7777-8888-999999999999', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-11', '13:00:00', '14:00:00', 'sitting', 390.00, 'UAH', '{
    "notes": "Midday feeding and play",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-08-02 21:00:00'),
  ('f6666666-7777-8888-9999-aaaaaaaaaaaa', 'fedcba98-7654-3210-fedc-ba9876543210', '2025-12-12', '16:00:00', '17:30:00', 'grooming', 1600.00, 'UAH', '{
    "notes": "Full bath and blow dry",
    "pet_id": "f4a5b6c7-d8e9-0123-4567-890abcdef012"
  }', '2025-08-02 22:00:00'),
  ('47777777-8888-9999-aaaa-bbbbbbbbbbbb', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-14', '11:00:00', '12:00:00', 'training', 700.00, 'UAH', '{
    "notes": "Behavior modification session",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-02 23:00:00'),
  ('38888888-9999-aaaa-bbbb-cccccccccccc', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-12-17', '19:00:00', '20:00:00', 'veterinary', 7400.00, 'UAH', '{
    "notes": "Emergency after-hours visit",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-08-03 08:00:00'),
  ('29999999-aaaa-bbbb-cccc-dddddddddddd', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-12-20', '08:00:00', '12:00:00', 'daycare', 1000.00, 'UAH', '{
    "notes": "Morning daycare with individual attention",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-08-03 09:00:00'),
  ('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', '12345678-90ab-cdef-1234-567890abcdef', '2025-12-22', '14:00:00', '15:00:00', 'other', 280.00, 'UAH', '{
    "notes": "Pet nail trimming",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-08-03 10:00:00'),
  ('bbbbbbbb-cccc-dddd-eeee-ffffffffffff', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-12-25', '10:00:00', '11:00:00', 'walking', 240.00, 'UAH', '{
    "notes": "Christmas morning walk",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-08-03 11:00:00'),
  ('e3f4a5b6-c7d8-9012-3456-7890abcdef03', '98765432-10fe-dcba-9876-543210abcdef', '2025-12-29', '15:00:00', '16:00:00', 'other', 250.00, 'UAH', '{
    "notes": "Pet sitting at client''s home",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-29 14:00:00'),
  ('f5a6b7c8-d9e0-1234-5678-90abcdef0125', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0', '2025-11-12', '08:00:00', '09:00:00', 'walking', 180.00, 'UAH', '{
    "notes": "Early morning walk",
    "pet_id": "b8c9d0e1-f2a3-4567-8901-cdef01234567"
  }', '2025-07-30 08:00:00'),
  ('a7b8c9d0-e1f2-3456-7890-1234567890ad', 'c1d2e3f4-a5b6-7890-1234-567890abcdef', '2025-11-13', '21:00:00', '22:00:00', 'sitting', 600.00, 'UAH', '{
    "notes": "Late night sitting",
    "pet_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
  }', '2025-07-30 09:00:00'),
  ('b9c0d1e2-f3a4-5678-9012-34567890acdf', '12345678-90ab-cdef-1234-567890abcdef', '2025-11-14', '14:00:00', '16:00:00', 'grooming', 1800.00, 'UAH', '{
    "notes": "Haircut and bath",
    "pet_id": "cdef0123-4567-8901-2345-67890abcdef0"
  }', '2025-07-30 10:00:00'),
  ('cbd0e1f2-a3b4-5678-9012-34567890adfe', 'abcdef01-2345-6789-abcd-ef0123456789', '2025-11-16', '11:00:00', '12:00:00', 'training', 900.00, 'UAH', '{
    "notes": "Basic obedience",
    "pet_id": "e2f3a4b5-c6d7-8901-2345-67890abcdef1"
  }', '2025-07-30 11:00:00');
