-- liquibase formatted sql

-- changeset lyndexter:1745856000-1-create-article
-- comment: Create article table for community articles/posts
CREATE TABLE peti.article (
  article_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title                 VARCHAR(300)  NOT NULL,
  summary               VARCHAR(1000),
  content               TEXT          NOT NULL,
  tags                  JSONB         NOT NULL DEFAULT '[]',
  created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMP,
  estimated_read_minutes INTEGER      NOT NULL DEFAULT 1,
  user_id               UUID          NOT NULL REFERENCES peti."user"(user_id),
  is_deleted            BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_article_created ON peti.article(created_at DESC);
CREATE INDEX idx_article_user    ON peti.article(user_id);

-- changeset lyndexter:1745856000-2-create-comment
-- comment: Create comment table supporting replies (self-referencing)
CREATE TABLE peti.comment (
  comment_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id           UUID          NOT NULL REFERENCES peti."user"(user_id),
  content           VARCHAR(1000) NOT NULL,
  target_type       VARCHAR(20)   NOT NULL,
  target_id         UUID          NOT NULL,
  parent_comment_id UUID          REFERENCES peti.comment(comment_id),
  created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMP,
  is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_comment_target ON peti.comment(target_type, target_id);
CREATE INDEX idx_comment_parent ON peti.comment(parent_comment_id);
CREATE INDEX idx_comment_user   ON peti.comment(user_id);

-- changeset lyndexter:1745856000-3-create-reaction
-- comment: Create reaction (like) table for articles/comments
CREATE TABLE peti.reaction (
  reaction_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID        NOT NULL REFERENCES peti."user"(user_id),
  target_type  VARCHAR(20) NOT NULL,
  target_id    UUID        NOT NULL,
  created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX idx_reaction_target ON peti.reaction(target_type, target_id);

-- changeset lyndexter:1745856000-4-seed-articles
-- comment: Seed article entries for mock data
INSERT INTO peti.article (article_id, title, summary, content, tags, created_at, estimated_read_minutes, user_id) VALUES
  ('20000000-0000-0000-0000-000000000001',
   'How I prepare a first walk with an anxious rescue dog',
   'A simple field checklist for the first meeting, route choice, and how to avoid overstimulation.',
   '## Before the walk\n\nAlways ask the owner about triggers. Common ones include other dogs, loud noises, and bicycles.\n\n## Route choice\n\nPick a quiet route with escape options. Avoid dog parks on the first walk.\n\n## During the walk\n\n- Keep a loose leash\n- Let the dog sniff freely\n- Watch for calming signals\n- Keep sessions short (20-30 min)',
   '["dogs", "walking", "behavior"]',
   '2026-04-13 07:40:00', 2,
   'a1b2c3d4-e5f6-7890-1234-567892abcdef'),
  ('20000000-0000-0000-0000-000000000002',
   'Cat grooming basics: what every new groomer should know',
   'Essential tips for grooming cats without stress — for you or the cat.',
   '## Preparation\n\nNever groom a cat that is agitated. Let them settle in a calm room first.\n\n## Tools\n\n- Slicker brush for long-haired cats\n- Rubber mitt for short-haired cats\n- Nail clippers (guillotine type works best)\n\n## Technique\n\n- Always brush in the direction of hair growth\n- Be gentle around the belly\n- Reward with treats after each session',
   '["cats", "grooming", "tips"]',
   '2026-04-15 11:20:00', 3,
   'b2c3d4e5-f6a7-8901-2345-67890abcdef7'),
  ('20000000-0000-0000-0000-000000000003',
   'Training a puppy to walk on a leash: step-by-step',
   'A structured approach to leash training for puppies aged 3-6 months.',
   '## Week 1: Indoor introduction\n\nLet the puppy wear the collar and leash indoors. No pulling, just let them drag it.\n\n## Week 2: Follow the puppy\n\nPick up the leash and follow. Reward when they move forward.\n\n## Week 3: Outdoor transition\n\nStart in the yard. Keep sessions to 10 minutes. Lots of treats.\n\n## Common mistakes\n\n- Pulling back on the leash\n- Too long sessions\n- Scolding instead of redirecting',
   '["dogs", "training", "puppies", "leash"]',
   '2026-04-18 09:00:00', 4,
   'a1b2c3d4-e5f6-7890-1234-567892abcdef'),
  ('20000000-0000-0000-0000-000000000004',
   'Understanding bird body language',
   'Birds communicate through posture, feather position, and vocalizations. Learn to read them.',
   '## Happy signs\n\n- Fluffed feathers while relaxed\n- Singing or chattering\n- Head bobbing\n\n## Stress signals\n\n- Feather plucking\n- Screaming (not singing)\n- Biting\n\n## What to do\n\nIf you notice stress signals during a visit, reduce stimulation and give the bird space.',
   '["birds", "behavior", "visiting"]',
   '2026-04-22 15:30:00', 2,
   'c3d4e5f6-a7b8-9012-3456-7890abcdef31');

-- changeset lyndexter:1745856000-5-seed-comments
-- comment: Seed comments for mock data
INSERT INTO peti.comment (comment_id, user_id, content, target_type, target_id, parent_comment_id, created_at) VALUES
  ('30000000-0000-0000-0000-000000000001', 'a1b2c3d4-e5f6-7890-1234-567892abcdef',
   'Great article! The tip about letting the dog sniff freely really helped me.',
   'article', '20000000-0000-0000-0000-000000000001', NULL, '2026-04-14 08:00:00'),
  ('30000000-0000-0000-0000-000000000002', 'b2c3d4e5-f6a7-8901-2345-67890abcdef7',
   'I would add: always carry high-value treats for the first walk.',
   'article', '20000000-0000-0000-0000-000000000001', NULL, '2026-04-14 09:30:00'),
  ('30000000-0000-0000-0000-000000000003', 'c3d4e5f6-a7b8-9012-3456-7890abcdef31',
   'Agreed! Cheese works great as a high-value treat.',
   'article', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '2026-04-14 10:00:00'),
  ('30000000-0000-0000-0000-000000000004', 'a1b2c3d4-e5f6-7890-1234-567892abcdef',
   'The grooming tips for cats are spot on, especially about the belly.',
   'article', '20000000-0000-0000-0000-000000000002', NULL, '2026-04-16 08:00:00');

-- changeset lyndexter:1745856000-6-seed-reactions
-- comment: Seed reactions for mock data
INSERT INTO peti.reaction (user_id, target_type, target_id, created_at) VALUES
  ('a1b2c3d4-e5f6-7890-1234-567892abcdef', 'article', '20000000-0000-0000-0000-000000000001', '2026-04-14 08:01:00'),
  ('b2c3d4e5-f6a7-8901-2345-67890abcdef7', 'article', '20000000-0000-0000-0000-000000000001', '2026-04-14 09:31:00'),
  ('c3d4e5f6-a7b8-9012-3456-7890abcdef31', 'article', '20000000-0000-0000-0000-000000000001', '2026-04-14 10:01:00'),
  ('a1b2c3d4-e5f6-7890-1234-567892abcdef', 'article', '20000000-0000-0000-0000-000000000002', '2026-04-16 12:00:00'),
  ('c3d4e5f6-a7b8-9012-3456-7890abcdef31', 'comment', '30000000-0000-0000-0000-000000000001', '2026-04-14 10:05:00');

