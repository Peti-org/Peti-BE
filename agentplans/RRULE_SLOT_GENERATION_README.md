# RRULE-based Automatic Slot Generation

## Overview
This feature automatically generates caretaker availability slots based on RFC 5545 Recurrence Rules (RRules). The system runs a scheduled job daily at 2 AM to generate slots for the next 14 days.

## Components

### 1. Database Schema
**Table: `caretaker_rrule`**
- `rrule_id` (UUID) - Primary key
- `caretaker_id` (UUID) - Foreign key to caretaker
- `rrule` (VARCHAR) - RFC 5545 recurrence rule (e.g., "FREQ=WEEKLY;BYDAY=MO,TU")
- `dtstart` (TIMESTAMP) - Start date/time
- `dtend` (TIMESTAMP) - End date/time (nullable)
- `description` (VARCHAR) - Human-readable description
- `slot_type` (VARCHAR) - Type of slot (e.g., "STANDARD", "PREMIUM")
- `created_at` (TIMESTAMP) - Record creation time

### 2. Services

#### RRuleSlotGenerator
**Location:** `com.peti.backend.service.RRuleSlotGenerator`

Core service responsible for:
- Parsing RRule strings using `lib-recur` library
- Generating date occurrences within a date range
- Checking for existing slots to avoid duplicates
- Creating new slot entities
- Batch processing (100 slots per transaction)

**Key Method:**
```java
public int generateSlotsForRRule(CaretakerRRule rrule, LocalDate startDate, LocalDate endDate)
```

#### SlotGenerationScheduler
**Location:** `com.peti.backend.service.SlotGenerationScheduler`

Scheduled component that:
- Runs daily at 2 AM (configurable via cron expression)
- Fetches all active RRules
- Processes them in batches (default: 50 RRules per batch)
- Generates slots for the next 14 days (configurable)
- Provides generation statistics

**Key Method:**
```java
@Scheduled(cron = "${app.slot-generation.cron:0 0 2 * * *}")
public void generateDailySlots()
```

### 3. Admin Controller
**Location:** `com.peti.backend.controller.AdminSlotController`

**Endpoint:** `POST /api/admin/slots/generate`
- **Security:** Requires ADMIN role
- **Purpose:** Manually trigger slot generation
- **Returns:** `SlotGenerationResult` with statistics

### 4. Configuration
**File:** `application.properties`

```properties
# Cron expression: Run daily at 2 AM
app.slot-generation.cron=0 0 2 * * *

# Number of days ahead to generate slots
app.slot-generation.days-ahead=14

# Batch size for processing RRules
app.slot-generation.batch-size=50
```

## Usage

### Creating an RRule
**Endpoint:** `POST /api/slots/rrules` (CARETAKER role)

```json
{
  "rrule": "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
  "dtstart": "2026-02-01T09:00:00",
  "dtend": "2026-12-31T17:00:00",
  "description": "Weekdays 9am-5pm",
  "slotType": "STANDARD"
}
```

### Common RRule Patterns

**Weekdays (Monday-Friday):**
```
FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR
```

**Weekends:**
```
FREQ=WEEKLY;BYDAY=SA,SU
```

**Daily:**
```
FREQ=DAILY
```

**Every Monday and Wednesday:**
```
FREQ=WEEKLY;BYDAY=MO,WE
```

**First Monday of each month:**
```
FREQ=MONTHLY;BYDAY=1MO
```

## Slot Generation Process

1. **Scheduled Trigger** - Job runs at 2 AM daily (or manual trigger by admin)
2. **Fetch Active RRules** - Query all RRules where `dtstart <= now` and (`dtend IS NULL` OR `dtend >= now`)
3. **Process in Batches** - Process RRules in configurable batch sizes
4. **Generate Occurrences** - For each RRule, calculate date occurrences for next 14 days
5. **Check Duplicates** - Verify if slots already exist for each occurrence
6. **Create Slots** - Generate new slot entities with:
   - Date from occurrence
   - Time range from RRule's dtstart/dtend
   - Type from RRule's slot_type
   - Default price (100.00 UAH) and capacity (5)
7. **Batch Save** - Save slots in batches of 100
8. **Log Statistics** - Record RRules processed, slots created, and errors

## Performance Considerations

- **Batch Processing:** RRules processed in configurable batches to manage memory
- **Batch Saving:** Slots saved in groups of 100 to optimize database performance
- **Duplicate Detection:** Efficient query using indexed columns
- **Error Handling:** Individual RRule errors don't stop the entire process
- **Logging:** Detailed logging for monitoring and debugging

## Testing

### Unit Tests
- **RRuleSlotGeneratorTest** - Tests RRULE parsing, occurrence generation, duplicate detection
- **SlotGenerationSchedulerTest** - Tests scheduling, batch processing, error handling
- **AdminSlotControllerTest** - Tests admin endpoint

### Test Coverage
All major functionality is covered including:
- Valid and invalid RRule parsing
- Date occurrence generation
- Existing slot detection
- Batch processing
- Error scenarios
- Configuration variations

## Dependencies

**Maven Dependency:**
```xml
<dependency>
  <groupId>org.dmfs</groupId>
  <artifactId>lib-recur</artifactId>
  <version>0.11.7</version>
</dependency>
```

## Monitoring

### Logs
Monitor the application logs for:
- Daily job execution at 2 AM
- Generation statistics (RRules processed, slots created, errors)
- Individual RRule processing errors
- Batch processing progress

### Admin Endpoint
Use `POST /api/admin/slots/generate` to:
- Manually trigger generation
- Get immediate feedback on generation results
- Test configuration changes

## Future Enhancements

1. **Configurable Slot Details** - Add price, capacity, currency to RRule configuration
2. **Time Slot Division** - Integrate with SlotDivider to create multiple slots per day
3. **Notification System** - Notify admins of generation failures
4. **Dashboard** - Admin UI to view generation history and statistics
5. **RRule Validation** - Frontend validation of RRule syntax before creation

## Troubleshooting

### Slots Not Being Generated
1. Check that the scheduled job is enabled (`@EnableScheduling`)
2. Verify RRule is active (dtstart <= now, dtend >= now or NULL)
3. Check logs for parsing errors
4. Verify database connectivity

### Duplicate Slots
1. Check the `existsByCaretakerIdAndDateAndTime` query
2. Verify database indexes are created
3. Review time zone handling

### Performance Issues
1. Reduce batch size in configuration
2. Increase scheduling interval (run less frequently)
3. Reduce days-ahead configuration
4. Add database indexes if needed

## Support

For issues or questions, contact the development team or refer to:
- Plan document: `plan-rruleSlotGeneration.prompt.md`
- RRule documentation: RFC 5545
- lib-recur documentation: https://github.com/dmfs/lib-recur

