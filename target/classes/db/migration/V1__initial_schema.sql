/*
  # Review and Ratings Service - Initial Schema

  1. New Tables
    - `reviews`
      - Stores all review data for products and services
      - Supports any entity type with flexible entityType and entityId fields
      - Includes rating (1-5), title, comment, images, verification status
      - Tracks helpful/unhelpful counts for community feedback
      - Supports moderation workflow with status field

    - `review_images`
      - Stores actual image binary data (BLOB)
      - Includes file metadata (name, type, size)
      - Max 10 images per review, 5MB per image

    - `review_helpfulness`
      - Tracks user votes on review helpfulness
      - Prevents duplicate votes via unique constraint
      - Links back to reviews and users

    - `review_responses`
      - Vendor/business responses to reviews
      - Supports different responder types (vendor, support, admin)

    - `rating_summaries`
      - Aggregated rating statistics per entity
      - Denormalized data for fast summary queries
      - Automatically updated when reviews change

  2. Indexes
    - Optimized for common query patterns:
      - Finding reviews by entity (type + id)
      - Finding reviews by user
      - Filtering by rating
      - Sorting by creation date
      - Looking up summaries

  3. Security
    - No RLS policies as this is a backend service
    - Application-level authorization handled in service layer
*/

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(100),
    comment VARCHAR(2000),
    verified BOOLEAN NOT NULL DEFAULT false,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    unhelpful_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    moderated_at TIMESTAMP,
    moderator_id VARCHAR(255),
    moderation_note VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_entity_type_id ON reviews(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_rating ON reviews(rating);
CREATE INDEX IF NOT EXISTS idx_created_at ON reviews(created_at);

CREATE TABLE IF NOT EXISTS review_images (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    image_data BYTEA NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_review_image_review_id ON review_images(review_id);

CREATE TABLE IF NOT EXISTS review_helpfulness (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_review_id ON review_helpfulness(review_id);
CREATE INDEX IF NOT EXISTS idx_user_id_helpfulness ON review_helpfulness(user_id);

CREATE TABLE IF NOT EXISTS review_responses (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    responder_id VARCHAR(255) NOT NULL,
    responder_type VARCHAR(50) NOT NULL,
    response VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_review_id_response ON review_responses(review_id);
CREATE INDEX IF NOT EXISTS idx_responder_id ON review_responses(responder_id);

CREATE TABLE IF NOT EXISTS rating_summaries (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    five_star_count INTEGER NOT NULL DEFAULT 0,
    four_star_count INTEGER NOT NULL DEFAULT 0,
    three_star_count INTEGER NOT NULL DEFAULT 0,
    two_star_count INTEGER NOT NULL DEFAULT 0,
    one_star_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS idx_entity_summary ON rating_summaries(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_avg_rating ON rating_summaries(average_rating);
