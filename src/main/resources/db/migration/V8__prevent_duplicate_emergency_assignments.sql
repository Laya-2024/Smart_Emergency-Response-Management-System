-- Existing installations may contain duplicates because the original check used
-- the account ID where the responder-profile ID was required. Retain the first
-- accepted assignment for each responder/emergency pair before enforcing it.
DELETE duplicate_assignment
FROM emergency_assignments duplicate_assignment
JOIN emergency_assignments retained_assignment
  ON duplicate_assignment.emergency_id = retained_assignment.emergency_id
 AND duplicate_assignment.responder_id = retained_assignment.responder_id
 AND (
      duplicate_assignment.assigned_at > retained_assignment.assigned_at
      OR (
          duplicate_assignment.assigned_at = retained_assignment.assigned_at
          AND duplicate_assignment.id > retained_assignment.id
      )
 );

-- Protect against duplicate acceptance when two requests arrive concurrently.
ALTER TABLE emergency_assignments
    ADD CONSTRAINT uk_assignment_emergency_responder UNIQUE (emergency_id, responder_id);
