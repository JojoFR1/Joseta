ALTER TABLE users
    ADD COLUMN counting_success INT DEFAULT 0,
    ADD COLUMN counting_fail INT DEFAULT 0,
    ADD column counting_special_success INT DEFAULT 0,
    ADD column counting_special_fail INT DEFAULT 0;