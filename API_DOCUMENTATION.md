# Review and Ratings Service - API Documentation

## Overview

Universal review and ratings service that supports all products and services. Built with Spring Boot, PostgreSQL, and RESTful APIs.

**Key Feature:** Images are stored directly in the database as binary data (BLOB), not as URLs. This provides better data integrity, security, and eliminates dependency on external storage services.

## Base URL

```
http://localhost:8080/api/v1
```

## API Endpoints

### Review Management

#### 1. Create Review

**POST** `/reviews`

Creates a new review for a product or service with optional image uploads.

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `entityType` (string, required): Entity type (e.g., PRODUCT, SERVICE)
- `entityId` (string, required): Entity ID
- `userId` (string, required): User ID
- `rating` (integer, required): Rating 1-5
- `title` (string, optional): Review title (max 100 chars)
- `comment` (string, optional): Review comment (max 2000 chars)
- `images` (file[], optional): Image files (max 10 files, 5MB each)

**Response:** `201 Created`
```json
{
  "id": 1,
  "entityType": "PRODUCT",
  "entityId": "PROD-123",
  "userId": "USER-1",
  "rating": 5,
  "title": "Excellent Product",
  "comment": "Very satisfied with the quality and delivery",
  "verified": false,
  "helpfulCount": 0,
  "unhelpfulCount": 0,
  "images": [
    {
      "id": 1,
      "fileName": "product-photo.jpg",
      "contentType": "image/jpeg",
      "fileSize": 245678,
      "uploadedAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Validation Rules:**
- `rating`: 1-5 (required)
- `entityType`: max 50 chars (required)
- `entityId`: max 100 chars (required)
- `userId`: required
- `title`: max 100 chars
- `comment`: max 2000 chars
- `images`: max 10 files, 5MB each
- Supported image formats: JPEG, PNG, GIF, WebP

**Error Responses:**
- `400 Bad Request`: Invalid input or file validation failed
- `409 Conflict`: User already reviewed this entity

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -F "entityType=PRODUCT" \
  -F "entityId=PROD-123" \
  -F "userId=USER-1" \
  -F "rating=5" \
  -F "title=Great Product" \
  -F "comment=Highly recommend" \
  -F "images=@photo1.jpg" \
  -F "images=@photo2.jpg"
```

---

#### 2. Update Review

**PUT** `/reviews/{reviewId}?userId={userId}`

Updates an existing review. Can add new images (existing images remain unless deleted separately).

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `rating` (integer, required): Rating 1-5
- `title` (string, optional): Review title
- `comment` (string, optional): Review comment
- `images` (file[], optional): Additional image files

**Response:** `200 OK`

**Error Responses:**
- `403 Forbidden`: User not authorized
- `404 Not Found`: Review not found

---

#### 3. Delete Review

**DELETE** `/reviews/{reviewId}?userId={userId}`

Soft deletes a review (sets status to DELETED). Associated images are also removed.

**Response:** `204 No Content`

**Error Responses:**
- `403 Forbidden`: User not authorized
- `404 Not Found`: Review not found

---

#### 4. Get Review by ID

**GET** `/reviews/{reviewId}`

Retrieves a specific review with responses and image metadata.

**Response:** `200 OK`
```json
{
  "id": 1,
  "entityType": "PRODUCT",
  "entityId": "PROD-123",
  "userId": "USER-1",
  "rating": 5,
  "title": "Excellent Product",
  "comment": "Very satisfied",
  "verified": true,
  "helpfulCount": 15,
  "unhelpfulCount": 2,
  "images": [
    {
      "id": 1,
      "fileName": "product-photo.jpg",
      "contentType": "image/jpeg",
      "fileSize": 245678,
      "uploadedAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "responses": [
    {
      "id": 1,
      "responderId": "VENDOR-1",
      "responderType": "VENDOR",
      "response": "Thank you for your feedback!",
      "createdAt": "2024-01-16T09:00:00",
      "updatedAt": "2024-01-16T09:00:00"
    }
  ]
}
```

---

#### 5. Get Reviews by Entity

**GET** `/reviews?entityType={type}&entityId={id}&page=0&size=10&sortBy=createdAt&direction=DESC`

Retrieves paginated reviews for an entity with optional filters.

**Query Parameters:**
- `entityType` (required): Entity type (e.g., PRODUCT, SERVICE)
- `entityId` (required): Entity ID
- `minRating` (optional): Filter by minimum rating
- `maxRating` (optional): Filter by maximum rating
- `verifiedOnly` (optional): Show only verified reviews
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sortBy` (default: createdAt): Sort field
- `direction` (default: DESC): Sort direction

**Response:** `200 OK`
```json
{
  "content": [...],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

---

#### 6. Get Reviews by User

**GET** `/reviews/user/{userId}?page=0&size=10`

Retrieves all reviews submitted by a specific user.

**Response:** `200 OK` (paginated)

---

#### 7. Mark Review Helpfulness

**POST** `/reviews/{reviewId}/helpfulness`

Marks a review as helpful or unhelpful.

**Request Body:**
```json
{
  "userId": "USER-2",
  "helpful": true
}
```

**Response:** `200 OK`

**Notes:**
- User can change their vote
- Previous vote is automatically removed when voting again

---

#### 8. Add Response to Review

**POST** `/reviews/{reviewId}/responses`

Adds a vendor/admin response to a review.

**Request Body:**
```json
{
  "responderId": "VENDOR-1",
  "responderType": "VENDOR",
  "response": "Thank you for your feedback! We're glad you're satisfied."
}
```

**Response:** `201 Created`

**Responder Types:**
- `VENDOR`: Product/service vendor
- `SUPPORT`: Customer support
- `ADMIN`: Platform admin

---

### Image Management

#### 9. Get Image

**GET** `/images/{imageId}`

Retrieves the actual image file by ID.

**Response:** `200 OK`

Returns the binary image data with appropriate `Content-Type` header (image/jpeg, image/png, etc.)

**Usage:**
```html
<img src="/api/v1/images/1" alt="Review photo">
```

---

#### 10. Delete Image

**DELETE** `/images/{imageId}`

Deletes an image by ID.

**Response:** `204 No Content`

**Authorization:** Typically restricted to review owner or admin

---

### Rating Summary

#### 11. Get Rating Summary

**GET** `/ratings?entityType={type}&entityId={id}`

Retrieves aggregated rating statistics for an entity.

**Response:** `200 OK`
```json
{
  "entityType": "PRODUCT",
  "entityId": "PROD-123",
  "averageRating": 4.5,
  "totalReviews": 100,
  "ratingDistribution": {
    "5": 60,
    "4": 25,
    "3": 10,
    "2": 3,
    "1": 2
  },
  "updatedAt": "2024-01-15T15:30:00"
}
```

---

#### 12. Refresh Rating Summary

**POST** `/ratings/refresh?entityType={type}&entityId={id}`

Manually recalculates rating summary for an entity.

**Response:** `200 OK`

**Note:** Summary is automatically updated when reviews are created/updated/deleted.

---

## Error Response Format

All error responses follow this structure:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/v1/reviews",
  "validationErrors": {
    "rating": "Rating must be at least 1"
  }
}
```

---

## Database Schema

### Tables

1. **reviews**: Main review data
2. **review_images**: Binary image storage (BLOB) with metadata
   - Stores actual image data in `image_data` column (BYTEA)
   - Includes file_name, content_type, file_size
   - Max 5MB per image, 10 images per review
3. **review_helpfulness**: Helpfulness votes
4. **review_responses**: Vendor/admin responses
5. **rating_summaries**: Aggregated statistics

### Image Storage

Images are stored directly in PostgreSQL as binary data (BLOB):
- **Advantages:**
  - No dependency on external storage services (S3, CDN, etc.)
  - ACID compliance and transactional integrity
  - Automatic backup with database backups
  - Simplified security and access control
  - No broken external links
  - Single source of truth
- **Considerations:**
  - Database size increases with images
  - Suitable for moderate image volumes
  - For high-volume scenarios (millions of images), consider external storage with CDN

---

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reviewdb
    username: postgres
    password: postgres

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 50MB

server:
  port: 8080
```

### Environment Variables

- `DATABASE_URL`: PostgreSQL connection URL
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password

---

## File Upload Requirements

### Supported Image Formats
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

### Size Limits
- Maximum file size: 5MB per image
- Maximum images per review: 10
- Maximum request size: 50MB

### Validation
- File type validation (content-type and extension)
- File size validation
- Automatic rejection of invalid formats

---

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test File Upload with cURL
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -F "entityType=PRODUCT" \
  -F "entityId=TEST-001" \
  -F "userId=TEST-USER" \
  -F "rating=5" \
  -F "title=Test Review" \
  -F "comment=Testing image upload" \
  -F "images=@/path/to/image.jpg"
```

---

## Swagger UI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification available at:
```
http://localhost:8080/api-docs
```

---

## Best Practices

### For Clients
1. Always validate images client-side before upload
2. Compress images before uploading when possible
3. Use the image metadata to display file information
4. Cache image responses with appropriate headers
5. Handle upload failures gracefully

### For Administrators
1. Monitor database size regularly
2. Set up regular database backups
3. Consider archiving old review images
4. Implement rate limiting for image uploads
5. Add authentication/authorization for image deletion
