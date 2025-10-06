## Overview

Review and Ratings service. Built with Spring Boot, PostgreSQL, and RESTful APIs.

**Key Feature:** Images are stored directly in the database as binary data (BLOB), not as URLs. 
We can also use Amazon S3 or Cloudflare R2 for storing files/images for better retrieval, but this thing is not implemented in this project.

## Base URL

```
http://localhost:8085/api/v1
```

## Swagger UI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```


#### 1. Review
http://localhost:8080/api/v1/reviews
- creating review
- updating review
- get all reviews
- get all reviews based on userId
  
#### 2. Rating
http://localhost:8080/api/v1/rating
- retrieving rating summaries and statistics
- refresh api to recalculate the average ratings

#### 3. Image
http://localhost:8080/api/v1/images
- get images
- delete images

## File Upload Requirements

### Supported Image Formats
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

### Size Limits
- Maximum file size: 2MB per image
- Maximum images per review: 10
- Maximum request size: 20MB



## Best Practices

### For Clients
1. Always validate images client-side before upload
2. Compress images and ensure that each image is below 2mb
3. Handle upload failures gracefully

### Improvement to be made
1. Store the image/files in external storage services (Amazon S3, Cloudflare R2) for better retrieval.
2. Use CDN for better retrieval of image based on the nearest location
