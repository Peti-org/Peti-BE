-- liquibase formatted sql

-- changeset lyndexter:1701096714332-1

-- tables
-- Table: breed
CREATE TABLE breed
(
  breed_id   INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  pet_type   varchar(50)                      NOT NULL,
  breed_name varchar(50)                      NOT NULL,
  CONSTRAINT breed_pk PRIMARY KEY (breed_id)
);

-- Table: caretacker_slots
CREATE TABLE caretaker_slot
(
  slot_id         uuid           NOT NULL,
  caretaker_id    uuid           NOT NULL,
  date            date           NOT NULL,
  time_from       time           NOT NULL,
  time_to         time           NOT NULL,
  type            varchar(30)    NOT NULL,
  price           decimal(10, 2) NOT NULL,
  currency        varchar(5)     NOT NULL,
  additional_data jsonb          NOT NULL,
  CONSTRAINT caretacker_slot_pk PRIMARY KEY (slot_id)
);


-- Table: caretaker
CREATE TABLE caretaker
(
  caretaker_id         uuid    NOT NULL,
  user_id              uuid    NOT NULL,
  caretaker_preference jsonb   NOT NULL,
  caretaker_is_deleted boolean NOT NULL,
  rating               int     NOT NULL,
  CONSTRAINT caretaker_pk PRIMARY KEY (caretaker_id)
);

-- Table: event
CREATE TABLE event
(
  event_id         uuid         NOT NULL,
  event_time       timestamp    NOT NULL,
  user_id          uuid         NOT NULL,
  event_name       varchar(100) NOT NULL,
  caretaker_id     uuid         NOT NULL,
  event_context    jsonb        NOT NULL,
  event_is_deleted boolean      NOT NULL,
  CONSTRAINT event_pk PRIMARY KEY (event_id)
);

-- Table: city
CREATE TABLE city
(
  city_id       BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
  longitude     decimal(18, 8)                      NULL,
  latitude      decimal(18, 8)                      NULL,
  country       varchar(20)                         NOT NULL,
  country_code  varchar(2)                          NOT NULL,
  city          varchar(40)                         NOT NULL,
  location_info varchar(100)                        NULL,
  CONSTRAINT city_pk PRIMARY KEY (city_id)
);

-- Table: location
CREATE TABLE location
(
  location_id int            NOT NULL,
  longitude   decimal(18, 8) NULL,
  latitude    decimal(18, 8) NULL,
  address     jsonb          NOT NULL,
  CONSTRAINT location_pk PRIMARY KEY (location_id)
);

-- Table: order
CREATE TABLE "order"
(
  order_id         uuid           NOT NULL,
  creation_time    timestamp      NOT NULL,
  price            numeric(12, 2) NOT NULL,
  currency         varchar(5)     NOT NULL,
  client_id        uuid           NOT NULL,
  caretaker_id     uuid           NOT NULL,
  event_id         uuid           NOT NULL,
  order_is_deleted boolean        NOT NULL,
  CONSTRAINT order_pk PRIMARY KEY (order_id)
);

-- Table: order_modification
CREATE TABLE order_modification
(
  order_id        uuid        NOT NULL,
  modification_id int         NOT NULL,
  type            varchar(20) NOT NULL,
  time            timestamp   NOT NULL,
  CONSTRAINT order_modification_pk PRIMARY KEY (order_id, modification_id)
);

-- Table: payment_settings
CREATE TABLE payment_settings
(
  user_id     uuid        NOT NULL,
  payment_id  int         NOT NULL,
  card_number varchar(20) NOT NULL,
  cvv         varchar(4)  NOT NULL,
  pin         varchar(4)  NOT NULL,
  CONSTRAINT payment_settings_pk PRIMARY KEY (user_id, payment_id)
);

-- Table: pet
CREATE TABLE pet
(
  pet_id          uuid        NOT NULL,
  user_id         uuid        NOT NULL,
  name            varchar(50) NOT NULL,
  birthday        date        NOT NULL,
  breed_id        INT         NOT NULL,
  context         jsonb       NOT NULL,
  pet_data_folder varchar(50) NOT NULL,
  CONSTRAINT pet_pk PRIMARY KEY (pet_id)
);

-- Table: user
CREATE TABLE "user"
(
  user_id          uuid        NOT NULL,
  first_name       varchar(50) NOT NULL,
  last_name        varchar(50) NULL,
  email            varchar(50) NOT NULL,
  birthday         date        NOT NULL,
  password         varchar(72) NOT NULL,
  location_id      int         NULL,
  city_id          BIGINT      NOT NULL,
  role_id          int         NOT NULL,
  user_is_deleted  boolean     NOT NULL,
  user_data_folder varchar(50) NOT NULL,
  CONSTRAINT user_pk PRIMARY KEY (user_id)
);

-- Table: role
CREATE TABLE "role"
(
  role_id   int         NOT NULL,
  role_name varchar(20) NOT NULL,
  CONSTRAINT role_pk PRIMARY KEY (role_id)
);

-- foreign keys
-- Reference: caretacker_slots_caretaker (table: caretacker_slots)
ALTER TABLE caretaker_slot
  ADD CONSTRAINT caretaker_slot_caretaker
    FOREIGN KEY (caretaker_id)
      REFERENCES caretaker (caretaker_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: caretaker_user (table: caretaker)
ALTER TABLE caretaker
  ADD CONSTRAINT caretaker_user
    FOREIGN KEY (user_id)
      REFERENCES "user" (user_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: client_order_user (table: order)
ALTER TABLE "order"
  ADD CONSTRAINT client_order_user
    FOREIGN KEY (client_id)
      REFERENCES "user" (user_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: order_caretaker (table: order)
ALTER TABLE "order"
  ADD CONSTRAINT order_caretaker
    FOREIGN KEY (caretaker_id)
      REFERENCES caretaker (caretaker_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: order_modification_order (table: order_modification)
ALTER TABLE order_modification
  ADD CONSTRAINT order_modification_order
    FOREIGN KEY (order_id)
      REFERENCES "order" (order_id)
      ON DELETE CASCADE
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: order_user_events (table: order)
ALTER TABLE "order"
  ADD CONSTRAINT order_user_events
    FOREIGN KEY (event_id)
      REFERENCES event (event_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: payment_settings_user (table: payment_settings)
ALTER TABLE payment_settings
  ADD CONSTRAINT payment_settings_user
    FOREIGN KEY (user_id)
      REFERENCES "user" (user_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: pet_breed (table: pet)
ALTER TABLE pet
  ADD CONSTRAINT pet_breed
    FOREIGN KEY (breed_id)
      REFERENCES breed (breed_id)
      ON DELETE RESTRICT
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: pet_user (table: pet)
ALTER TABLE pet
  ADD CONSTRAINT pet_user
    FOREIGN KEY (user_id)
      REFERENCES "user" (user_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_events_caretaker (table: event)
ALTER TABLE event
  ADD CONSTRAINT user_events_caretaker
    FOREIGN KEY (caretaker_id)
      REFERENCES caretaker (caretaker_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_events_user (table: event)
ALTER TABLE event
  ADD CONSTRAINT user_events_user
    FOREIGN KEY (user_id)
      REFERENCES "user" (user_id)
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_location (table: user)
ALTER TABLE "user"
  ADD CONSTRAINT user_location
    FOREIGN KEY (location_id)
      REFERENCES location (location_id)
      ON DELETE RESTRICT
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_city (table: user)
ALTER TABLE "user"
  ADD CONSTRAINT user_city
    FOREIGN KEY (city_id)
      REFERENCES city (city_id)
      ON DELETE RESTRICT
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_city (table: user)
ALTER TABLE "user"
  ADD CONSTRAINT user_role
    FOREIGN KEY (role_id)
      REFERENCES role (role_id)
      ON DELETE RESTRICT
      NOT DEFERRABLE
        INITIALLY IMMEDIATE
;

-- Reference: user_email_constraint (table: user)
ALTER TABLE "user"
  ADD CONSTRAINT user_email_constraint
    UNIQUE (email)
;
