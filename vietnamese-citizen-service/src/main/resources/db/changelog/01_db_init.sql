-- =====================================================
-- FILE: init_database.sql
-- MỤC ĐÍCH: Tạo bảng citizen & address partition theo GIỜ (created_date)
-- CÁCH DÙNG: Chạy TOÀN BỘ file này lên database đích.
--            Idempotent một phần: phần DROP đầu file cho phép chạy lại từ đầu.
-- YÊU CẦU: User chạy script có quyền CREATE trên schema public.
-- =====================================================

-- Cố định timezone của session để:
--   (1) Ranh giới giờ của partition thẳng hàng giờ Việt Nam (trùng giờ đọc log vận hành)
--   (2) Tên partition (YYYYMMDD_HH24MI) theo chuẩn +07
-- Nếu chuẩn hóa toàn hệ thống sang UTC: đổi DUY NHẤT dòng này thành 'UTC'.
-- SET timezone = 'Asia/Ho_Chi_Minh';
SET timezone = 'UTC';   -- trước đó là 'Asia/Ho_Chi_Minh'

-- =====================================================
-- 0. DỌN DẸP OBJECT CŨ (để script chạy lại được từ đầu)
--    DROP bảng cha sẽ tự động DROP toàn bộ partition con.
-- =====================================================
DROP TABLE IF EXISTS address CASCADE;
DROP TABLE IF EXISTS citizen CASCADE;
DROP FUNCTION IF EXISTS create_partition_for_timerange(TEXT, TIMESTAMP WITH TIME ZONE, TIMESTAMP WITH TIME ZONE);
DROP FUNCTION IF EXISTS create_upcoming_partitions(TEXT, INTEGER);

-- =====================================================
-- 1. BẢNG citizen - PARTITION BY RANGE (created_date)
--    PostgreSQL >= 13: gen_random_uuid() là built-in, không cần extension.
--    Nếu dùng PG < 13: thêm CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- =====================================================
CREATE TABLE citizen (
    id UUID DEFAULT gen_random_uuid(),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    birthday DATE NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id, created_date)
) PARTITION BY RANGE (created_date);

-- =====================================================
-- 2. BẢNG address - PARTITION BY RANGE (created_date)
--    FK composite (citizen_id, created_date) -> citizen(id, created_date)
-- =====================================================
CREATE TABLE address (
    id UUID DEFAULT gen_random_uuid(),
    citizen_id UUID NOT NULL,
    detail TEXT,
    ward VARCHAR(100),
    province VARCHAR(100),
    address_type VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id, created_date),
    FOREIGN KEY (citizen_id, created_date) REFERENCES citizen(id, created_date)
) PARTITION BY RANGE (created_date);

-- =====================================================
-- 3. FUNCTION TẠO PARTITION CHO MỘT KHOẢNG THỜI GIAN CỤ THỂ
--    (Idempotent: IF NOT EXISTS nên gọi lại bao nhiêu lần cũng an toàn)
-- =====================================================
CREATE OR REPLACE FUNCTION create_partition_for_timerange(
    p_table_name TEXT,
    p_start_time TIMESTAMP WITH TIME ZONE,
    p_end_time TIMESTAMP WITH TIME ZONE
) RETURNS VOID AS $$
DECLARE
    v_partition_name TEXT;
BEGIN
    v_partition_name := p_table_name || '_' || to_char(p_start_time, 'YYYYMMDD_HH24MI');

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
        v_partition_name, p_table_name, p_start_time, p_end_time
    );

    EXECUTE format(
        'CREATE INDEX IF NOT EXISTS %I ON %I(id)',
        'idx_' || v_partition_name || '_id',
        v_partition_name
    );

    IF p_table_name = 'address' THEN
        EXECUTE format(
            'CREATE INDEX IF NOT EXISTS %I ON %I(citizen_id)',
            'idx_' || v_partition_name || '_citizen_id',
            v_partition_name
        );
    END IF;

    RAISE NOTICE 'Created partition: %', v_partition_name;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 4. FUNCTION TẠO PARTITION "SẮP DIỄN RA"
--    Lưới an toàn: giờ HIỆN TẠI luôn có partition (tự vá nếu cron app bị lỡ nhịp)
--    + tạo trước p_num_intervals giờ tương lai
-- =====================================================
CREATE OR REPLACE FUNCTION create_upcoming_partitions(
    p_table_name TEXT,
    p_num_intervals INTEGER DEFAULT 1
) RETURNS VOID AS $$
DECLARE
    i INTEGER;
    v_current_hour TIMESTAMP WITH TIME ZONE;
    v_start TIMESTAMP WITH TIME ZONE;
    v_end   TIMESTAMP WITH TIME ZONE;
BEGIN
    v_current_hour := date_trunc('hour', CURRENT_TIMESTAMP);

    -- Lưới an toàn: giờ hiện tại phải luôn có partition
    PERFORM create_partition_for_timerange(
        p_table_name, v_current_hour, v_current_hour + INTERVAL '1 hour');

    -- Tạo partition cho các giờ SẮP bắt đầu
    FOR i IN 1..p_num_intervals LOOP
        v_start := v_current_hour + (i * INTERVAL '1 hour');
        v_end   := v_start + INTERVAL '1 hour';
        PERFORM create_partition_for_timerange(p_table_name, v_start, v_end);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 5. KHỞI TẠO BAN ĐẦU: giờ hiện tại + 3 giờ kế tiếp
--    (khớp với partition.maintenance.num-intervals: 3 trong application.yaml)
--    ⚠️ TUYỆT ĐỐI KHÔNG XÓA 2 DÒNG NÀY - thiếu chúng, seeder sẽ dính
--    lỗi "no partition of relation" ngay lần insert đầu tiên.
-- =====================================================
SELECT create_upcoming_partitions('citizen', 3);
SELECT create_upcoming_partitions('address', 3);

-- =====================================================
-- 6. VERIFY (chạy tay khi cần kiểm tra)
--    Phải thấy tối thiểu 4 partition mỗi bảng (giờ hiện tại + 3 giờ tới)
-- =====================================================
-- SELECT p.relname  AS parent_table,
--        c.relname  AS partition_name,
--        pg_get_expr(c.relpartbound, c.oid) AS range_bound
-- FROM pg_inherits i
-- JOIN pg_class c ON c.oid = i.inhrelid
-- JOIN pg_class p ON p.oid = i.inhparent
-- WHERE p.relname IN ('citizen', 'address')
-- ORDER BY p.relname, c.relname;

-- =====================================================
-- GHI CHÚ VẬN HÀNH
--
-- [!] KHÔNG thêm DEFAULT partition: khi có partition DEFAULT, việc tạo
--     partition giờ mới sẽ FAIL nếu default đang chứa row thuộc range đó,
--     đồng thời nó che mất lỗi "thiếu partition" mà ta muốn phát hiện sớm.
--
-- [OPTIONAL - production] Để DB TỰ maintenance, không phụ thuộc uptime app:
--     CREATE EXTENSION IF NOT EXISTS pg_cron;
--     SELECT cron.schedule(
--       'partition-maintenance-hourly',
--       '59 * * * *',
--       $$ SELECT create_upcoming_partitions('citizen', 3);
--          SELECT create_upcoming_partitions('address', 3); $$
--     );
-- =====================================================