--FK 조건 강제 비활성화
SET REFERENTIAL_INTEGRITY FALSE;

--모든 테이블마다, 데이터를 날리고, id 인덱스를 1로 초기화함.

TRUNCATE TABLE shop;
ALTER TABLE shop ALTER COLUMN id RESTART WITH 1;

TRUNCATE TABLE review;
ALTER TABLE review ALTER COLUMN id RESTART WITH 1;

TRUNCATE TABLE reservation;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;

TRUNCATE TABLE res_open_day;

TRUNCATE TABLE res_open_time;

--FK 조건 활성화
SET REFERENTIAL_INTEGRITY TRUE;