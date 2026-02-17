# Quick Reference: RRule Capacity Implementation

## Database Migration

Run Liquibase to apply migrations:
```bash
mvn liquibase:update
```

Changes in `25-12-2025.sql`:
- Slot table: `is_repeated`, `rrule_id`
- RRule table: `capacity`, `interval_minutes`, `generated_to`, index

## Key Changes Summary

### 1. Models
- **Slot**: Added `isRepeated`, `rrule` (ManyToOne)
- **CaretakerRRule**: Added `capacity`, `intervalMinutes`, `generatedTo`

### 2. DTOs
- **RRuleDto/RequestRRuleDto**: Added capacity, intervalMinutes (+ validation)
- **SlotDto**: Added isRepeated
- **RequestEventDto**: Added @NotEmpty validation

### 3. Repositories
- **SlotRepository**: Added `existsByCaretakerIdAndDateAndTime()`, `deleteByRRuleIdAndDateAfterAndUnoccupied()`
- **CaretakerRRuleRepository**: Added `findAllNeedingGeneration()`

### 4. Services

**SlotService**:
- Manual slots: `isRepeated = false`, `rrule = null`

**CaretakerRRuleService**:
- Create/Update: Auto-triggers slot generation
- Delete: Cleans up unoccupied slots

**RRuleSlotGenerator**:
- Uses SlotDivider for time slot division
- Sets `isRepeated = true` on generated slots
- Uses RRule capacity
- Updates `generatedTo` field

**SlotGenerationScheduler**:
- Incremental generation (checks `generatedTo`)
- New: `generateSlotsForSingleRRule()` for on-demand
- New: `deleteSlotsForRRule()` for cleanup

## Testing

Run all tests:
```bash
mvn test
```

Test coverage:
- SlotServiceTest (8 tests)
- CaretakerRRuleServiceTest (7 tests)
- RRuleSlotGeneratorTest (5 tests)
- SlotGenerationSchedulerTest (8 tests)
- RequestRRuleDtoValidationTest (10 tests)
- RequestEventDtoValidationTest (4 tests)

## API Usage

### Create RRule (auto-generates slots)
```json
POST /api/rrules
{
  "rrule": "FREQ=DAILY",
  "dtstart": "2026-02-10T09:00:00",
  "dtend": "2026-12-31T18:00:00",
  "slotType": "walk",
  "capacity": 5,
  "intervalMinutes": 30
}
```

### Update RRule (deletes old, regenerates new)
```json
PUT /api/rrules/{id}
{
  "capacity": 10,
  "intervalMinutes": 15
  // ... other fields
}
```

### Create Manual Slot
```json
POST /api/slots
{
  "date": "2026-02-10",
  "timeFrom": "09:00:00",
  "timeTo": "11:00:00",
  "type": "walk",
  "capacity": 3
}
```
Result: `isRepeated = false`

## Behavior

### Slot Generation
- **Scheduled**: Daily at 2 AM, generates slots for next 14 days
- **On-Demand**: After RRule create/update, generates next 14 days immediately
- **Incremental**: Only generates slots from `generatedTo + 1` onwards

### Slot Cleanup on RRule Update/Delete
- Deletes only **unoccupied** slots (`occupiedCapacity = 0`)
- Preserves booked slots
- Only affects slots with `date >= today`

### Slot Origin
- Manual slots: `isRepeated = false`, `rrule_id = null`
- RRule-generated: `isRepeated = true`, `rrule_id = <rrule_id>`

## Validation Rules

**RequestRRuleDto**:
- `rrule`: NotBlank
- `dtstart`: NotNull
- `slotType`: NotBlank
- `capacity`: NotNull, Positive
- `intervalMinutes`: NotNull, Min(1)

**RequestSlotDto**:
- `capacity`: NotNull, Positive
- All existing validations retained

**RequestEventDto**:
- `slotsIds`: NotEmpty
- `petsIds`: NotEmpty

## Troubleshooting

**IDE shows "Cannot resolve table/column" errors**:
- These are database sync issues
- Run the migration: `mvn liquibase:update`
- Refresh IDE database connection

**Slots not generating**:
- Check scheduler logs for errors
- Verify RRule is active (dtstart <= now, dtend >= now or null)
- Check `generatedTo` field value

**Validation errors**:
- Ensure capacity > 0
- Ensure intervalMinutes >= 1
- Ensure required fields are provided

## Performance Notes

- Batch saves: 100 slots at a time
- Index on `(caretaker_id, generated_to)` for efficient queries
- Incremental generation reduces redundant work
- Only unoccupied slots are deleted on RRule update

## Next Steps

1. ✅ Run database migration
2. ✅ Run tests to verify implementation
3. ✅ Test API endpoints manually
4. Consider: Add price field to RRule (future)
5. Consider: Add pessimistic locking for concurrent generation (future)

