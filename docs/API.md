# FindYourDreamHouseAI API Reference

[![API Version](https://img.shields.io/badge/API-v1-blue)](#)
[![Base URL](https://img.shields.io/badge/Base%20URL-localhost:8080-green)](#)
[![Authentication](https://img.shields.io/badge/Auth-JWT%20Bearer-orange)](#)

Complete API documentation for the FindYourDreamHouseAI backend service.

## 📋 Table of Contents

- [Base Information](#-base-information)
- [Authentication](#-authentication)
- [User Management](#-user-management)
- [House Advertisement Management](#-house-advertisement-management)
- [Image Management](#-image-management)
- [Messaging System](#-messaging-system)
- [AI Search & Analysis](#-ai-search--analysis)
- [Data Models](#-data-models)
- [Error Handling](#-error-handling)
- [Rate Limiting](#-rate-limiting)
- [Examples](#-examples)

## 🌐 Base Information

**Base URL:** `http://localhost:8080`  
**API Version:** v1  
**Content-Type:** `application/json` (unless specified)  
**Authentication:** JWT Bearer Token (for protected endpoints)

### Headers

All requests should include appropriate headers:

```http
Content-Type: application/json
Authorization: Bearer <jwt-token>  # For protected endpoints
```

### Response Format

All responses follow a consistent format:

**Success Response:**
```json
{
  "data": { ... },
  "message": "Success message",
  "timestamp": "2025-01-07T10:30:00Z"
}
```

**Error Response:**
```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "timestamp": "2025-01-07T10:30:00Z",
  "details": { ... }
}
```

## 🔐 Authentication

### Register User

Create a new user account.

**Endpoint:** `POST /api/v1/auth/register`  
**Authentication:** None (Public)  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "securePassword123",
  "name": "John",
  "lastname": "Doe",
  "email": "john.doe@example.com"
}
```

**Response:**
```json
{
  "userUid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "name": "John",
  "lastname": "Doe",
  "email": "john.doe@example.com",
  "createdAt": "2025-01-07T10:30:00Z"
}
```

**Status Codes:**
- `200 OK` - User created successfully
- `409 Conflict` - Username already exists
- `400 Bad Request` - Invalid input data

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123",
    "name": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com"
  }'
```

### Login

Authenticate user and receive JWT token.

**Endpoint:** `POST /login`  
**Authentication:** None (Public)  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "username": "johndoe",
  "token": "Bearer eyJhbGciOiJBMjU2R0NNIiwidHlwIjoiSldUIn0...",
  "expiresAt": "2025-01-07T18:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Login successful
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Missing required fields

**cURL Example:**
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }'
```

## 👤 User Management

### Get User Profile

Retrieve user profile information.

**Endpoint:** `GET /api/v1/user/{userId}`  
**Authentication:** Required (Bearer Token)  
**Authorization:** User must match the requested userId or have ADMIN role

**Path Parameters:**
- `userId` (string, required) - User UUID

**Response:**
```json
{
  "userUid": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "name": "John",
  "lastname": "Doe",
  "email": "john.doe@example.com",
  "addresses": [
    {
      "addressUid": "660e8400-e29b-41d4-a716-446655440001",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    }
  ],
  "roles": ["ROLE_USER"],
  "lastLogin": "2025-01-07T10:30:00Z",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Status Codes:**
- `200 OK` - User profile retrieved
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - User not found

### Add User Address

Add a new address to user profile.

**Endpoint:** `POST /api/v1/user/{userId}/address`  
**Authentication:** Required (Bearer Token)  
**Authorization:** User must match the requested userId or have ADMIN role

**Request Body:**
```json
{
  "street": "456 Oak Avenue",
  "city": "Los Angeles",
  "state": "CA",
  "postalCode": "90210",
  "country": "USA"
}
```

**Response:**
```json
{
  "message": "Address added successfully",
  "addressUid": "770e8400-e29b-41d4-a716-446655440002"
}
```

**Status Codes:**
- `200 OK` - Address added successfully
- `400 Bad Request` - Invalid address data
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions

### Delete User Account

Delete user account and all associated data.

**Endpoint:** `DELETE /api/v1/auth/account-deletion/{userId}`  
**Authentication:** Required (Bearer Token)  
**Authorization:** User must match the requested userId or have ADMIN role

**Path Parameters:**
- `userId` (string, required) - User UUID

**Response:**
```json
{
  "message": "Account deleted successfully",
  "deletedAt": "2025-01-07T10:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Account deleted successfully
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - User not found

## 🏠 House Advertisement Management

### Create House Advertisement

Create a new house advertisement.

**Endpoint:** `POST /api/v1/houseAds/create`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Request Body:**
```json
{
  "title": "Beautiful 3-Bedroom House",
  "description": "Spacious family home with garden and garage",
  "price": 450000,
  "address": "789 Pine Street, Seattle, WA 98101",
  "images": [
    {
      "imageName": "front-view.jpg",
      "imageUrl": "https://example.com/images/front-view.jpg",
      "imageDescription": "Front view of the house",
      "imageType": "image/jpeg",
      "imageThumbnail": "https://example.com/thumbnails/front-view.jpg",
      "storageKey": "house-ads/550e8400-e29b-41d4-a716-446655440000/uuid.jpg"
    }
  ]
}
```

**Response:**
```json
{
  "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "title": "Beautiful 3-Bedroom House",
  "description": "Spacious family home with garden and garage",
  "price": 450000,
  "address": "789 Pine Street, Seattle, WA 98101",
  "user": {
    "userUid": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "name": "John",
    "lastname": "Doe"
  },
  "images": [
    {
      "houseAdImageUid": "990e8400-e29b-41d4-a716-446655440004",
      "imageName": "front-view.jpg",
      "imageUrl": "https://example.com/images/front-view.jpg",
      "imageDescription": "Front view of the house",
      "imageType": "image/jpeg",
      "imageThumbnail": "https://example.com/thumbnails/front-view.jpg",
      "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
      "storageKey": "house-ads/550e8400-e29b-41d4-a716-446655440000/uuid.jpg"
    }
  ],
  "createdAt": "2025-01-07T10:30:00Z",
  "updatedAt": "2025-01-07T10:30:00Z"
}
```

**Status Codes:**
- `200 OK` - House ad created successfully
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions

### Get All House Advertisements

Retrieve all house advertisements (public endpoint).

**Endpoint:** `GET /api/v1/houseAds`  
**Authentication:** None (Public)

**Query Parameters:**
- `page` (integer, optional) - Page number (default: 0)
- `size` (integer, optional) - Page size (default: 20)
- `sort` (string, optional) - Sort field (default: createdAt)
- `direction` (string, optional) - Sort direction: ASC or DESC (default: DESC)

**Response:**
```json
{
  "content": [
    {
      "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
      "title": "Beautiful 3-Bedroom House",
      "description": "Spacious family home with garden and garage",
      "price": 450000,
      "address": "789 Pine Street, Seattle, WA 98101",
      "user": {
        "userUid": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "name": "John",
        "lastname": "Doe"
      },
      "imagesCount": 3,
      "createdAt": "2025-01-07T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

**Status Codes:**
- `200 OK` - House ads retrieved successfully

### Get House Advertisement by ID

Retrieve a specific house advertisement by ID.

**Endpoint:** `GET /api/v1/houseAds/id/{houseAdId}`  
**Authentication:** None (Public)

**Path Parameters:**
- `houseAdId` (string, required) - House advertisement UUID

**Response:**
```json
{
  "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "title": "Beautiful 3-Bedroom House",
  "description": "Spacious family home with garden and garage",
  "price": 450000,
  "address": "789 Pine Street, Seattle, WA 98101",
  "user": {
    "userUid": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "name": "John",
    "lastname": "Doe"
  },
  "images": [
    {
      "houseAdImageUid": "990e8400-e29b-41d4-a716-446655440004",
      "imageName": "front-view.jpg",
      "imageUrl": "https://example.com/images/front-view.jpg",
      "imageDescription": "Front view of the house",
      "imageType": "image/jpeg",
      "imageThumbnail": "https://example.com/thumbnails/front-view.jpg",
      "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
      "storageKey": "house-ads/550e8400-e29b-41d4-a716-446655440000/uuid.jpg"
    }
  ],
  "createdAt": "2025-01-07T10:30:00Z",
  "updatedAt": "2025-01-07T10:30:00Z"
}
```

**Status Codes:**
- `200 OK` - House ad retrieved successfully
- `404 Not Found` - House ad not found

### Search House Advertisements

Search house advertisements by title.

**Endpoint:** `GET /api/v1/houseAds/title`  
**Authentication:** None (Public)

**Query Parameters:**
- `title` (string, required) - Search query
- `page` (integer, optional) - Page number (default: 0)
- `size` (integer, optional) - Page size (default: 20)

**Response:**
```json
{
  "content": [
    {
      "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
      "title": "Beautiful 3-Bedroom House",
      "description": "Spacious family home with garden and garage",
      "price": 450000,
      "address": "789 Pine Street, Seattle, WA 98101",
      "user": {
        "userUid": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "name": "John",
        "lastname": "Doe"
      },
      "imagesCount": 3,
      "createdAt": "2025-01-07T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": false,
      "unsorted": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

**Status Codes:**
- `200 OK` - Search completed successfully
- `400 Bad Request` - Missing or invalid search query

### Update House Advertisement

Update house advertisement title and description.

**Endpoint:** `PUT /api/v1/houseAds/updateTitleAndDescription`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Request Body:**
```json
{
  "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "title": "Updated Beautiful 3-Bedroom House",
  "description": "Updated description with more details"
}
```

**Response:**
```json
{
  "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "title": "Updated Beautiful 3-Bedroom House",
  "description": "Updated description with more details",
  "price": 450000,
  "address": "789 Pine Street, Seattle, WA 98101",
  "user": {
    "userUid": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "name": "John",
    "lastname": "Doe"
  },
  "images": [],
  "createdAt": "2025-01-07T10:30:00Z",
  "updatedAt": "2025-01-07T11:00:00Z"
}
```

**Status Codes:**
- `200 OK` - House ad updated successfully
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - House ad not found

### Delete House Advertisement

Delete a house advertisement.

**Endpoint:** `DELETE /api/v1/houseAds/houseAdId`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Query Parameters:**
- `houseAdUid` (string, required) - House advertisement UUID

**Response:**
```json
{
  "message": "House advertisement deleted successfully",
  "deletedAt": "2025-01-07T11:30:00Z"
}
```

**Status Codes:**
- `200 OK` - House ad deleted successfully
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - House ad not found

## 🖼️ Image Management

### Upload Images

Upload images for a house advertisement.

**Endpoint:** `POST /api/v1/houseAds/{houseAdId}/images`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN  
**Content-Type:** `multipart/form-data`

**Path Parameters:**
- `houseAdId` (string, required) - House advertisement UUID

**Form Data:**
- `files` (file[], required) - Image files (max 10MB each, image/* types only)
- `captions` (string[], optional) - Image captions (same order as files)

**Response:**
```json
[
  {
    "houseAdImageUid": "aa0e8400-e29b-41d4-a716-446655440005",
    "imageName": "living-room.jpg",
    "imageUrl": "https://s3.amazonaws.com/bucket/house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg",
    "imageDescription": "Spacious living room",
    "imageType": "image/jpeg",
    "imageThumbnail": "https://s3.amazonaws.com/bucket/thumbnails/house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg",
    "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
    "storageKey": "house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg"
  }
]
```

**Status Codes:**
- `201 Created` - Images uploaded successfully
- `400 Bad Request` - Invalid file format or size
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - House ad not found

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/houseAds/880e8400-e29b-41d4-a716-446655440003/images \
  -H "Authorization: Bearer <token>" \
  -F "files=@/path/to/living-room.jpg" \
  -F "files=@/path/to/kitchen.jpg" \
  -F "captions=Spacious living room" \
  -F "captions=Modern kitchen"
```

### Get Images

Retrieve images for a house advertisement.

**Endpoint:** `GET /api/v1/houseAds/{houseAdId}/images`  
**Authentication:** None (Public)

**Path Parameters:**
- `houseAdId` (string, required) - House advertisement UUID

**Response:**
```json
[
  {
    "houseAdImageUid": "aa0e8400-e29b-41d4-a716-446655440005",
    "imageName": "living-room.jpg",
    "imageUrl": "https://s3.amazonaws.com/bucket/house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg",
    "imageDescription": "Spacious living room",
    "imageType": "image/jpeg",
    "imageThumbnail": "https://s3.amazonaws.com/bucket/thumbnails/house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg",
    "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
    "storageKey": "house-ads/880e8400-e29b-41d4-a716-446655440003/aa0e8400-e29b-41d4-a716-446655440005.jpg"
  }
]
```

**Status Codes:**
- `200 OK` - Images retrieved successfully
- `404 Not Found` - House ad not found

### Delete Image

Delete an image from a house advertisement.

**Endpoint:** `DELETE /api/v1/houseAds/{houseAdId}/images/{imageUid}`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Path Parameters:**
- `houseAdId` (string, required) - House advertisement UUID
- `imageUid` (string, required) - Image UUID

**Response:**
```json
{
  "message": "Image deleted successfully",
  "deletedAt": "2025-01-07T12:00:00Z"
}
```

**Status Codes:**
- `204 No Content` - Image deleted successfully
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Image or house ad not found

## 💬 Messaging System

### Send Message

Send a message related to a house advertisement.

**Endpoint:** `POST /api/v1/houseAds/message`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Request Body:**
```json
{
  "receiverHouseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "messageDate": "2025-01-07T12:30:00Z",
  "senderEmail": "buyer@example.com",
  "senderName": "Jane Smith",
  "senderPhone": "+1-555-0123",
  "subject": "Interested in your property",
  "message": "Hi, I'm interested in viewing this property. When would be a good time to schedule a visit?"
}
```

**Response:**
```json
{
  "messageUid": "bb0e8400-e29b-41d4-a716-446655440006",
  "receiverHouseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "senderName": "Jane Smith",
  "senderEmail": "buyer@example.com",
  "senderPhone": "+1-555-0123",
  "subject": "Interested in your property",
  "message": "Hi, I'm interested in viewing this property. When would be a good time to schedule a visit?",
  "messageDate": "2025-01-07T12:30:00Z",
  "sentAt": "2025-01-07T12:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Message sent successfully
- `400 Bad Request` - Invalid message data
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - House ad not found

### Get Message

Retrieve a specific message by ID.

**Endpoint:** `GET /api/v1/houseAds/message/{messageUid}`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Path Parameters:**
- `messageUid` (string, required) - Message UUID

**Response:**
```json
{
  "messageUid": "bb0e8400-e29b-41d4-a716-446655440006",
  "receiverHouseAdUid": "880e8400-e29b-41d4-a716-446655440003",
  "senderName": "Jane Smith",
  "senderEmail": "buyer@example.com",
  "senderPhone": "+1-555-0123",
  "subject": "Interested in your property",
  "message": "Hi, I'm interested in viewing this property. When would be a good time to schedule a visit?",
  "messageDate": "2025-01-07T12:30:00Z",
  "sentAt": "2025-01-07T12:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Message retrieved successfully
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Message not found

### Get Messages for House Advertisement

Retrieve all messages for a specific house advertisement.

**Endpoint:** `GET /api/v1/houseAds/messages/{houseUid}`  
**Authentication:** Required (Bearer Token)  
**Authorization:** ROLE_USER or ROLE_ADMIN

**Path Parameters:**
- `houseUid` (string, required) - House advertisement UUID

**Response:**
```json
[
  {
    "messageUid": "bb0e8400-e29b-41d4-a716-446655440006",
    "receiverHouseAdUid": "880e8400-e29b-41d4-a716-446655440003",
    "senderName": "Jane Smith",
    "senderEmail": "buyer@example.com",
    "senderPhone": "+1-555-0123",
    "subject": "Interested in your property",
    "message": "Hi, I'm interested in viewing this property. When would be a good time to schedule a visit?",
    "messageDate": "2025-01-07T12:30:00Z",
    "sentAt": "2025-01-07T12:30:00Z"
  }
]
```

**Status Codes:**
- `200 OK` - Messages retrieved successfully
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - House ad not found

## 🤖 AI Search & Analysis

### Natural Language Property Search

Search properties using conversational language with AI-powered query understanding.

**Endpoint:** `POST /api/v1/ai/search`  
**Authentication:** None (Public)  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "q": "I'm looking for a 3 bedroom house near Central Park in NYC under $500k"
}
```

**Response:**
```json
{
  "summary": "Found 15 properties matching your criteria",
  "houseAdDTOS": [
    {
      "houseAdUid": "880e8400-e29b-41d4-a716-446655440003",
      "title": "Beautiful 3-Bedroom House",
      "description": "Spacious family home with garden and garage",
      "price": 450000,
      "address": "789 Pine Street, NYC, NY 10001",
      "city": "NYC",
      "state": "NY",
      "beds": 3,
      "baths": 2,
      "type": "House",
      "user": {
        "userUid": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "name": "John",
        "lastname": "Doe"
      },
      "images": [
        {
          "houseAdImageUid": "990e8400-e29b-41d4-a716-446655440004",
          "imageName": "front-view.jpg",
          "imageUrl": "https://s3.amazonaws.com/bucket/house-ads/880e8400-e29b-41d4-a716-446655440003/990e8400-e29b-41d4-a716-446655440004.jpg",
          "imageDescription": "Front view of the house",
          "imageType": "image/jpeg",
          "imageThumbnail": "https://s3.amazonaws.com/bucket/thumbnails/house-ads/880e8400-e29b-41d4-a716-446655440003/990e8400-e29b-41d4-a716-446655440004.jpg",
          "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
          "storageKey": "house-ads/880e8400-e29b-41d4-a716-446655440003/990e8400-e29b-41d4-a716-446655440004.jpg"
        }
      ],
      "createdAt": "2025-01-07T10:30:00Z",
      "updatedAt": "2025-01-07T10:30:00Z"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Search completed successfully
- `400 Bad Request` - Invalid search query
- `500 Internal Server Error` - AI service error

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/ai/search \
  -H "Content-Type: application/json" \
  -d '{
    "q": "I need a 2 bedroom apartment in Boston under $300k"
  }'
```

### Intelligent Image Similarity Search

Upload property photos to find visually similar listings using AI-powered image analysis with local Qwen Vision model.

**Endpoint:** `POST /api/v1/ai/similar`  
**Authentication:** None (Public)  
**Content-Type:** `multipart/form-data`

**Form Data:**
- `file` (file, required) - Image file (JPEG, PNG, WebP, max 10MB)
- `k` (integer, optional) - Number of results to return (default: 12, max: 50)
- `cityHint` (string, optional) - City filter for results
- `typeHint` (string, optional) - Property type filter (House, Apartment, Condo, etc.)
- `bedsHint` (integer, optional) - Bedroom count filter
- `priceHint` (number, optional) - Price range filter (will search ±15% range)

**Response:**
```json
{
  "inferredDescription": {
    "style": "Modern",
    "exterior": "Brick facade with large windows",
    "stories": "2",
    "bed_bath_hint": "3 bed, 2 bath",
    "features": ["Garage", "Garden", "Balcony"],
    "condition": "Excellent",
    "notes": "Well-maintained property with modern amenities"
  },
  "results": [
    {
      "houseAdUid": "aa0e8400-e29b-41d4-a716-446655440007",
      "title": "Modern 3-Bedroom House",
      "description": "Contemporary home with brick exterior and modern finishes",
      "price": 425000,
      "address": "456 Oak Avenue, Boston, MA 02101",
      "city": "Boston",
      "state": "MA",
      "beds": 3,
      "baths": 2,
      "type": "House",
      "user": {
        "userUid": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "name": "John",
        "lastname": "Doe"
      },
      "images": [
        {
          "houseAdImageUid": "bb0e8400-e29b-41d4-a716-446655440008",
          "imageName": "exterior.jpg",
          "imageUrl": "https://s3.amazonaws.com/bucket/house-ads/aa0e8400-e29b-41d4-a716-446655440007/bb0e8400-e29b-41d4-a716-446655440008.jpg",
          "imageDescription": "Modern brick exterior",
          "imageType": "image/jpeg",
          "imageThumbnail": "https://s3.amazonaws.com/bucket/thumbnails/house-ads/aa0e8400-e29b-41d4-a716-446655440007/bb0e8400-e29b-41d4-a716-446655440008.jpg",
          "viewUrl": "https://s3.amazonaws.com/bucket/presigned-url",
          "storageKey": "house-ads/aa0e8400-e29b-41d4-a716-446655440007/bb0e8400-e29b-41d4-a716-446655440008.jpg"
        }
      ],
      "createdAt": "2025-01-07T09:15:00Z",
      "updatedAt": "2025-01-07T09:15:00Z"
    }
  ],
  "appliedHints": {
    "city": "Boston",
    "type": "House",
    "beds": 3,
    "price": 425000
  }
}
```

**Status Codes:**
- `200 OK` - Image analysis and search completed successfully
- `400 Bad Request` - Invalid file format or size
- `413 Payload Too Large` - File exceeds size limit
- `415 Unsupported Media Type` - Invalid file type
- `500 Internal Server Error` - AI service error

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/ai/similar \
  -F "file=@/path/to/property-photo.jpg" \
  -F "k=10" \
  -F "cityHint=Boston" \
  -F "typeHint=House" \
  -F "bedsHint=3" \
  -F "priceHint=400000"
```

### AI Search Features

**Supported Image Formats:**
- JPEG (.jpg, .jpeg)
- PNG (.png)
- WebP (.webp)

**AI Analysis Capabilities:**
- Property style identification (Modern, Traditional, Victorian, etc.) using Qwen Vision
- Exterior features detection (Brick, Wood, Stone, etc.)
- Building characteristics (Stories, Bed/Bath estimation)
- Property features recognition (Garage, Garden, Pool, etc.)
- Condition assessment (Excellent, Good, Fair, etc.)
- Complete local processing for data privacy

**Search Optimization:**
- Automatic image resizing and compression
- Vector embedding generation for similarity matching
- Multi-criteria filtering with hints
- Distributed caching for performance
- Request throttling and rate limiting

## 📊 Data Models

### UserDTO

```json
{
  "userUid": "string (UUID)",
  "username": "string",
  "name": "string",
  "lastname": "string",
  "email": "string",
  "addresses": [
    {
      "addressUid": "string (UUID)",
      "street": "string",
      "city": "string",
      "state": "string",
      "postalCode": "string",
      "country": "string"
    }
  ],
  "roles": ["string"],
  "lastLogin": "string (ISO-8601)",
  "createdAt": "string (ISO-8601)",
  "updatedAt": "string (ISO-8601)"
}
```

### HouseAdDTO

```json
{
  "houseAdUid": "string (UUID)",
  "title": "string",
  "description": "string",
  "price": "number",
  "address": "string",
  "user": {
    "userUid": "string (UUID)",
    "username": "string",
    "name": "string",
    "lastname": "string"
  },
  "images": [
    {
      "houseAdImageUid": "string (UUID)",
      "imageName": "string",
      "imageUrl": "string",
      "imageDescription": "string",
      "imageType": "string",
      "imageThumbnail": "string",
      "viewUrl": "string (optional)",
      "storageKey": "string"
    }
  ],
  "createdAt": "string (ISO-8601)",
  "updatedAt": "string (ISO-8601)"
}
```

### HouseAdImageDTO

```json
{
  "houseAdImageUid": "string (UUID)",
  "imageName": "string",
  "imageUrl": "string",
  "imageDescription": "string",
  "imageType": "string",
  "imageThumbnail": "string",
  "viewUrl": "string (optional)",
  "storageKey": "string"
}
```

### HouseAdMessageDTO

```json
{
  "messageUid": "string (UUID)",
  "receiverHouseAdUid": "string (UUID)",
  "senderName": "string",
  "senderEmail": "string",
  "senderPhone": "string",
  "subject": "string",
  "message": "string",
  "messageDate": "string (ISO-8601)",
  "sentAt": "string (ISO-8601)"
}
```

## 🚨 Error Handling

### HTTP Status Codes

| Code | Description | When Used |
|------|-------------|-----------|
| 200 | OK | Successful GET, PUT operations |
| 201 | Created | Successful POST operations |
| 204 | No Content | Successful DELETE operations |
| 400 | Bad Request | Invalid request data, validation errors |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (e.g., username exists) |
| 413 | Payload Too Large | File upload too large |
| 415 | Unsupported Media Type | Invalid file type |
| 500 | Internal Server Error | Server-side errors |

### Error Response Format

```json
{
  "error": "Validation failed",
  "code": "VALIDATION_ERROR",
  "timestamp": "2025-01-07T12:30:00Z",
  "details": {
    "field": "username",
    "message": "Username is required"
  },
  "path": "/api/v1/auth/register"
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `VALIDATION_ERROR` | Input validation failed |
| `USER_NOT_FOUND` | User does not exist |
| `USER_ALREADY_EXISTS` | Username already taken |
| `INVALID_CREDENTIALS` | Wrong username/password |
| `INVALID_TOKEN` | JWT token is invalid or expired |
| `INSUFFICIENT_PERMISSIONS` | User lacks required role |
| `HOUSE_AD_NOT_FOUND` | House advertisement not found |
| `IMAGE_NOT_FOUND` | Image not found |
| `MESSAGE_NOT_FOUND` | Message not found |
| `FILE_TOO_LARGE` | Uploaded file exceeds size limit |
| `UNSUPPORTED_FILE_TYPE` | File type not supported |
| `AWS_ERROR` | AWS service error |
| `DATABASE_ERROR` | Database operation failed |

## ⚡ Rate Limiting

Currently, no rate limiting is implemented. For production deployment, consider implementing:

- **API Rate Limiting**: Limit requests per user/IP
- **File Upload Limits**: Restrict image upload frequency
- **Authentication Limits**: Prevent brute force attacks

## 📝 Examples

### Complete Workflow Example

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123",
    "name": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com"
  }'

# 2. Login to get JWT token
TOKEN=$(curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }' | jq -r '.token')

# 3. Create a house advertisement
curl -X POST http://localhost:8080/api/v1/houseAds/create \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Beautiful 3-Bedroom House",
    "description": "Spacious family home with garden and garage",
    "price": 450000,
    "address": "789 Pine Street, Seattle, WA 98101"
  }'

# 4. Upload images
curl -X POST http://localhost:8080/api/v1/houseAds/{houseAdId}/images \
  -H "Authorization: $TOKEN" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "captions=Living room" \
  -F "captions=Kitchen"

# 5. Send a message
curl -X POST http://localhost:8080/api/v1/houseAds/message \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverHouseAdUid": "{houseAdId}",
    "messageDate": "2025-01-07T12:30:00Z",
    "senderEmail": "buyer@example.com",
    "senderName": "Jane Smith",
    "senderPhone": "+1-555-0123",
    "subject": "Interested in your property",
    "message": "Hi, I am interested in viewing this property."
  }'
```

---

**API Version:** 1.0  
**Last Updated:** 2025-01-07  
**Contact:** [Open an issue](https://github.com/your-org/FindYourDreamHouseAI/issues) for API support