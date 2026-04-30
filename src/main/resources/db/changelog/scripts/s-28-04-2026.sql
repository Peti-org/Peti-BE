-- liquibase formatted sql

-- changeset lyndexter:1745856000-8-update-pet-context-mock-data
-- comment: Update existing pet context fields with PetProfile JSON structure
UPDATE peti.pet SET context = '{
  "weightKg": 25.0,
  "sex": "MALE",
  "sterilized": "YES",
  "vaccinated": "YES",
  "getsAlongWithDogs": "YES",
  "getsAlongWithCats": "UNKNOWN",
  "getsAlongWithKids": "YES",
  "allergies": null,
  "medicineSchedule": null,
  "vetInfo": "VetClinic Kyiv, вул. Хрещатик 10, +380441112233",
  "additionalDetails": "Loves long walks in the park",
  "description": "Friendly and energetic golden retriever"
}'::jsonb WHERE context::text = '{"test": "test"}' OR context::text = '{"test":"test"}';

-- changeset lyndexter:1745856000-9-insert-pet-mock-with-profile
-- comment: Insert additional pets with full PetProfile data
INSERT INTO peti.pet (pet_id, name, birthday, context, pet_data_folder, user_id, breed_id) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'Барсик', '2021-06-15', '{
    "weightKg": 4.5,
    "sex": "MALE",
    "sterilized": "YES",
    "vaccinated": "YES",
    "getsAlongWithDogs": "NO",
    "getsAlongWithCats": "YES",
    "getsAlongWithKids": "YES",
    "allergies": "Chicken protein",
    "medicineSchedule": null,
    "vetInfo": "Ветклініка Мурчик, +380501234567",
    "additionalDetails": "Indoor cat, afraid of loud noises",
    "description": "Calm British Shorthair, loves napping on windowsills"
  }'::jsonb, 'default', 'a1b2c3d4-e5f6-7890-1234-567892abcdef', 1),
  ('e0000000-0000-0000-0000-000000000002', 'Рекс', '2019-03-20', '{
    "weightKg": 32.0,
    "sex": "MALE",
    "sterilized": "NO",
    "vaccinated": "YES",
    "getsAlongWithDogs": "YES",
    "getsAlongWithCats": "NO",
    "getsAlongWithKids": "YES",
    "allergies": null,
    "medicineSchedule": "Apoquel 16mg daily at 8am for skin allergy",
    "vetInfo": "VetLife Clinic, вул. Шевченка 5, +380671234567",
    "additionalDetails": "Needs muzzle in public transport",
    "description": "Strong and loyal German Shepherd, well trained"
  }'::jsonb, 'default', 'b2c3d4e5-f6a7-8901-2345-67890abcdef7', 2),
  ('e0000000-0000-0000-0000-000000000003', 'Мілка', '2023-01-10', '{
    "weightKg": 3.2,
    "sex": "FEMALE",
    "sterilized": "UNKNOWN",
    "vaccinated": "NO",
    "getsAlongWithDogs": "UNKNOWN",
    "getsAlongWithCats": "YES",
    "getsAlongWithKids": "UNKNOWN",
    "allergies": null,
    "medicineSchedule": null,
    "vetInfo": null,
    "additionalDetails": "Recently adopted, still getting used to new home",
    "description": "Shy young tabby cat, warming up slowly"
  }'::jsonb, 'default', 'c3d4e5f6-a7b8-9012-3456-7890abcdef31', 1),
  ('e0000000-0000-0000-0000-000000000004', 'Чарлі', '2022-08-05', '{
    "weightKg": 8.0,
    "sex": "MALE",
    "sterilized": "YES",
    "vaccinated": "YES",
    "getsAlongWithDogs": "YES",
    "getsAlongWithCats": "YES",
    "getsAlongWithKids": "YES",
    "allergies": "Grain-free diet required",
    "medicineSchedule": "Joint supplement daily with food",
    "vetInfo": "Happy Paws, вул. Франка 22, +380931234567",
    "additionalDetails": null,
    "description": "Playful beagle mix, very food motivated"
  }'::jsonb, 'default', 'a1b2c3d4-e5f6-7890-1234-567892abcdef', 2);

