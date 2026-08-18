-- Hibernate maps the Java `int` rating field to MySQL INTEGER.
-- Keep the validated database schema aligned with the entity mapping.
ALTER TABLE emergency_feedback
    MODIFY COLUMN rating INT NOT NULL;
