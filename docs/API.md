# API Specification

## REST API Endpoints

### Authentication
- **POST** `/api/auth/login` - Returns JWT token

### Reports
- **POST** `/api/reports` - Register new timesheet report
- **GET** `/api/reports/{year}/{month}` - Get timesheet report by year/month
- **PUT** `/api/reports/{year}/{month}` - Update existing timesheet report
- **DELETE** `/api/reports/{year}/{month}` - Delete timesheet report
- **POST** `/api/reports/generate` - Generate timesheet report from location data

### Locations
- **POST** `/api/locations` - Register new location data

### Work Rules
- **POST** `/api/work-rules` - Register new work rule

### Default Work Rules
- **POST** `/api/default-work-rules` - Register new default work rule

### Report Creation Rules
- **POST** `/api/report-creation-rules` - Register new report creation rule

## Authentication Requirements
- All API endpoints except `/api/auth/**` require JWT token in Authorization header: `Bearer {token}`
- Year and month are passed as path parameters, user ID is extracted from JWT token
- Request/response bodies use DTOs for data transfer between layers

## HTTP Status Codes
- **200 OK**: Successful retrieval
- **201 Created**: Successful creation
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: User not authorized for requested resource
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error