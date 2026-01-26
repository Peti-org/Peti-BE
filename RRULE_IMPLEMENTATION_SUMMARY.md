# RRule Management Implementation Summary

## Overview
Successfully implemented RRule (Recurrence Rule) management functionality for caretakers. This allows caretakers to define recurring availability patterns (e.g., "every Monday 9-17", "weekdays 14-18").

## Implementation Date
January 25, 2026

## Components Created

### 1. Database Migration
**File:** `src/main/resources/db/changelog/scripts/26-01-2025.sql`
- Created `caretaker_rrule` table with the following schema:
  - `rrule_id` (UUID, Primary Key)
  - `caretaker_id` (UUID, Foreign Key to caretaker table)
  - `rrule` (VARCHAR(500)) - RFC 5545 format recurrence rule
  - `dtstart` (TIMESTAMP) - Start date/time
  - `dtend` (TIMESTAMP, nullable) - End date/time
  - `description` (VARCHAR(255), nullable) - Human-readable description
  - `created_at` (TIMESTAMP) - Record creation timestamp
- Added index on `caretaker_id` for performance
- Included mock data for 6 RRules across existing caretakers

### 2. Domain Model
**File:** `src/main/java/com/peti/backend/model/domain/CaretakerRRule.java`
- JPA Entity mapping to `caretaker_rrule` table
- `@ManyToOne` relationship with `Caretaker` entity
- All necessary getters/setters via Lombok

### 3. Repository Layer
**File:** `src/main/java/com/peti/backend/repository/CaretakerRRuleRepository.java`
- Extends `JpaRepository<CaretakerRRule, UUID>`
- Custom query method: `findAllByCaretaker_CaretakerId(UUID caretakerId)`

### 4. DTOs (Data Transfer Objects)
**Location:** `src/main/java/com/peti/backend/dto/rrule/`

#### RRuleDto.java (Response DTO)
- Record type with all fields: rruleId, caretakerId, rrule, dtstart, dtend, description, createdAt
- Used for returning RRule data to clients

#### CreateRRuleDto.java (Request DTO)
- Record type for creating new RRules
- Fields: rrule, dtstart, dtend, description
- Validation annotations:
  - `@NotEmpty` on rrule
  - `@NotNull` on dtstart

#### RequestRRuleDto.java (Request DTO)
- Record type for updating existing RRules
- Same structure as CreateRRuleDto
- Same validation annotations

### 5. Service Layer
**File:** `src/main/java/com/peti/backend/service/CaretakerRRuleService.java`
- `@Service` component with transaction support
- Methods implemented:
  - `getAllRRulesForCaretaker(UUID caretakerId)` - Get all RRules for a caretaker
  - `createRRule(CreateRRuleDto, UUID caretakerId)` - Create new RRule
  - `updateRRule(UUID rruleId, UpdateRRuleDto, UUID caretakerId)` - Update existing RRule
  - `deleteRRule(UUID rruleId, UUID caretakerId)` - Delete RRule
  - `convertToDto(CaretakerRRule)` - Static utility method for DTO conversion
- All operations include caretaker ownership validation
- Transactional methods ensure data consistency

### 6. Controller Endpoints
**File:** `src/main/java/com/peti/backend/controller/SlotController.java`
- Added 4 new RRule endpoints under `/api/slots/rrules`:

#### GET /api/slots/rrules
- Returns all RRules for the authenticated caretaker
- Response: `List<RRuleDto>`

#### POST /api/slots/rrules
- Creates a new RRule for the authenticated caretaker
- Request Body: `CreateRRuleDto` (validated)
- Response: `RRuleDto`

#### PUT /api/slots/rrules/{rruleId}
- Updates an existing RRule
- Path Variable: `rruleId` (UUID)
- Request Body: `UpdateRRuleDto` (validated)
- Response: `RRuleDto` or 404 if not found/not owned

#### DELETE /api/slots/rrules/{rruleId}
- Deletes an existing RRule
- Path Variable: `rruleId` (UUID)
- Response: `RRuleDto` (deleted resource) or 404 if not found/not owned

All endpoints:
- Protected with `@HasCaretakerRole` annotation
- Automatically inject `caretakerId` from authentication context
- Return appropriate HTTP status codes
- Include Swagger documentation via annotations

## Security
- All endpoints require CARETAKER role
- Automatic caretaker ID extraction from authentication
- Ownership validation prevents unauthorized access/modification
- Each operation validates the caretaker owns the RRule

## Mock Data
Included 6 sample RRules:
1. Olena Kovalenko - Weekends 9am-6pm
2. Serhiy Melnyk - Weekdays 9am-6pm
3. Serhiy Melnyk - Evening hours Mon/Wed/Fri
4. Alice Smith - Tuesday/Thursday evenings
5. Bob Johnson - Weekend mornings (no end date)
6. Carol Williams - Daily mornings

## API Examples

### Get All RRules
```http
GET /api/slots/rrules
Authorization: Bearer {token}
```

### Create RRule
```http
POST /api/slots/rrules
Authorization: Bearer {token}
Content-Type: application/json

{
  "rrule": "FREQ=WEEKLY;BYDAY=MO,WE,FR",
  "dtstart": "2026-01-01T09:00:00",
  "dtend": "2026-12-31T17:00:00",
  "description": "Available Mon/Wed/Fri mornings"
}
```

### Update RRule
```http
PUT /api/slots/rrules/{rruleId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "rrule": "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
  "dtstart": "2026-01-01T10:00:00",
  "dtend": "2026-12-31T18:00:00",
  "description": "Updated availability - weekdays"
}
```

### Delete RRule
```http
DELETE /api/slots/rrules/{rruleId}
Authorization: Bearer {token}
```

## Database Migration Instructions
To apply the changes:
1. Ensure the database is accessible
2. Start the Spring Boot application
3. Liquibase will automatically apply the migration from `26-01-2025.sql`
4. The `caretaker_rrule` table will be created
5. Mock data will be inserted

## Future Enhancements
Consider implementing:
1. **RRule Validation**: Add RFC 5545 format validation using a library like `ical4j` or `biweekly`
2. **Slot Generation**: Add service method to generate `Slot` entities from RRules for a date range
3. **Timezone Support**: Add timezone fields and handle `TIMESTAMP WITH TIME ZONE`
4. **Conflict Detection**: Check for overlapping RRules or availability conflicts
5. **Recurring Slot Creation**: Integrate RRule expansion into slot creation workflow

## Testing Notes
- IDE may show temporary errors for the `caretaker_rrule` table until migration runs
- All endpoints follow existing project patterns (DTOs, security, validation)
- Service layer includes proper transaction management
- Repository follows Spring Data JPA conventions

## Files Modified
1. `SlotController.java` - Added RRule endpoints and service injection

## Files Created
1. `26-01-2025.sql` - Database migration script
2. `CaretakerRRule.java` - Domain model
3. `CaretakerRRuleRepository.java` - Repository interface
4. `CaretakerRRuleService.java` - Service layer
5. `RRuleDto.java` - Response DTO
6. `CreateRRuleDto.java` - Create request DTO
7. `UpdateRRuleDto.java` - Update request DTO

## Implementation Complete ✓
All requested features have been implemented:
- ✅ Database table for RRules with foreign key to caretaker
- ✅ Support for multiple RRules per caretaker
- ✅ Repository for managing RRule entities
- ✅ Domain model representing the table
- ✅ DTOs using Java records
- ✅ Service layer with appropriate business logic
- ✅ REST endpoints in SlotController (GET, POST, PUT, DELETE)
- ✅ Mock data for testing
- ✅ Proper security with caretaker role validation
- ✅ Ownership validation for all operations

