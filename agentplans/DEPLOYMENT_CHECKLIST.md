# Deployment Checklist - RRule Capacity Implementation

## Pre-Deployment

### 1. Code Review
- [ ] Review all entity changes (Slot, CaretakerRRule)
- [ ] Review all service changes (especially generation logic)
- [ ] Review database migration script
- [ ] Verify all validation annotations are correct

### 2. Testing
- [ ] Run all unit tests: `mvn test`
- [ ] Verify SlotServiceTest passes (8 tests)
- [ ] Verify CaretakerRRuleServiceTest passes (7 tests)
- [ ] Verify RRuleSlotGeneratorTest passes (5 tests)
- [ ] Verify SlotGenerationSchedulerTest passes (8 tests)
- [ ] Verify validation tests pass (14 tests)
- [ ] Run integration tests if available

### 3. Database Migration Testing
- [ ] Test migration on dev database
- [ ] Verify new columns are created correctly
- [ ] Verify indexes are created
- [ ] Verify foreign keys work
- [ ] Test rollback scenario

## Deployment Steps

### 1. Backup
- [ ] Backup production database
- [ ] Backup application configuration

### 2. Database Migration
```bash
mvn liquibase:update
```
- [ ] Run Liquibase migration
- [ ] Verify all changesets applied successfully
- [ ] Check `caretaker_slot` table for new columns
- [ ] Check `caretaker_rrule` table for new columns
- [ ] Verify indexes created

### 3. Application Deployment
- [ ] Build application: `mvn clean package`
- [ ] Deploy WAR/JAR to server
- [ ] Restart application server
- [ ] Check application logs for startup errors

### 4. Post-Deployment Verification

#### API Endpoints
- [ ] Test GET /api/rrules (should include capacity, intervalMinutes, generatedTo)
- [ ] Test POST /api/rrules (should create and generate slots)
- [ ] Test PUT /api/rrules (should regenerate slots)
- [ ] Test DELETE /api/rrules (should cleanup slots)
- [ ] Test GET /api/slots (should include isRepeated field)
- [ ] Test POST /api/slots (manual slots should have isRepeated=false)

#### Data Validation
```sql
-- Check existing slots have defaults
SELECT COUNT(*) FROM peti.caretaker_slot WHERE is_repeated = false;

-- Check RRules have defaults
SELECT COUNT(*) FROM peti.caretaker_rrule WHERE capacity = 1 AND interval_minutes = 30;

-- Verify index exists
SELECT indexname FROM pg_indexes WHERE tablename = 'caretaker_rrule' AND indexname = 'idx_rrule_generated_to';
```

#### Scheduler Verification
- [ ] Wait for scheduled job (2 AM default) or trigger manually
- [ ] Check logs for slot generation activity
- [ ] Verify slots are created for active RRules
- [ ] Verify `generatedTo` field is updated

#### Validation Testing
```bash
# Test validation with curl
curl -X POST http://localhost:8080/api/rrules \
  -H "Content-Type: application/json" \
  -d '{"rrule":"FREQ=DAILY","dtstart":"2026-02-10T09:00:00","slotType":"walk"}' \
  # Should fail - missing capacity and intervalMinutes
```

### 5. Monitor

#### Application Logs
- [ ] Check for any exceptions
- [ ] Monitor slot generation performance
- [ ] Check for database connection issues

#### Database Performance
- [ ] Monitor query performance on `findAllNeedingGeneration`
- [ ] Check index usage
- [ ] Monitor slot creation batch operations

#### Business Metrics
- [ ] Verify slot generation counts are reasonable
- [ ] Check that manual slots work correctly
- [ ] Verify capacity constraints are enforced

## Rollback Plan

If issues occur:

### 1. Application Rollback
- [ ] Stop application server
- [ ] Deploy previous version
- [ ] Restart application server

### 2. Database Rollback
```sql
-- Rollback changeset 4 (RRule fields)
DROP INDEX IF EXISTS peti.idx_rrule_generated_to;
ALTER TABLE peti.caretaker_rrule DROP COLUMN IF EXISTS generated_to;
ALTER TABLE peti.caretaker_rrule DROP COLUMN IF EXISTS interval_minutes;
ALTER TABLE peti.caretaker_rrule DROP COLUMN IF EXISTS capacity;

-- Rollback changeset 1 (Slot fields)
ALTER TABLE peti.caretaker_slot DROP CONSTRAINT IF EXISTS fk_slot_rrule;
ALTER TABLE peti.caretaker_slot DROP COLUMN IF EXISTS rrule_id;
ALTER TABLE peti.caretaker_slot DROP COLUMN IF EXISTS is_repeated;
```

Or use Liquibase:
```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=2
```

## Known Issues / Limitations

1. **IDE Database Sync**: IDE may show "Cannot resolve table/column" until database connection is refreshed
2. **Initial Generation**: Large RRules may take time for initial slot generation
3. **Slot Cleanup**: Only unoccupied slots are deleted; booked slots are preserved
4. **Price Field**: Currently uses hardcoded default price in RRuleSlotGenerator

## Support Contacts

- Developer: [Your Name]
- Database Admin: [DBA Name]
- DevOps: [DevOps Contact]

## Post-Deployment Tasks

### Within 24 Hours
- [ ] Monitor application logs for errors
- [ ] Check scheduler execution
- [ ] Verify slot generation working correctly
- [ ] Test all affected API endpoints

### Within 1 Week
- [ ] Review performance metrics
- [ ] Check for any unexpected behavior
- [ ] Gather user feedback
- [ ] Document any issues found

### Future Enhancements
- [ ] Add price field to RRule (currently hardcoded)
- [ ] Add pessimistic locking for concurrent generation
- [ ] Add unique constraint on slot time ranges
- [ ] Consider limiting initial generation to 90 days
- [ ] Add monitoring/alerting for slot generation failures

## Success Criteria

Deployment is successful when:
- ✅ All tests pass
- ✅ Database migration completes without errors
- ✅ Application starts successfully
- ✅ RRule CRUD operations work with new fields
- ✅ Slot generation creates slots with correct capacity
- ✅ Manual slots have `isRepeated = false`
- ✅ Auto-generated slots have `isRepeated = true`
- ✅ Scheduler runs and generates slots incrementally
- ✅ No critical errors in logs
- ✅ API validation works correctly

## Sign-Off

- [ ] Developer: _________________ Date: _______
- [ ] QA: _________________ Date: _______
- [ ] DevOps: _________________ Date: _______
- [ ] Product Owner: _________________ Date: _______

## Notes

[Add any deployment-specific notes here]

