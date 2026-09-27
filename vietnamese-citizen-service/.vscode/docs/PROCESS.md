# Document for: vietnamese-citizen-service

## Process

### 1. Requirements

| Functional Requirements | Non Functional Requirements |
|-------------------------|-----------------------------|
| Fake Citizen (Job)      | Strict Consistency (ACID)   |
| Create Citizen (POST API) | Idempotency                 |
| Get Citizen (GET API)   | Scalability                 |
|                         | High Availability           |
|                         | Low Latency                 |

### 2. Data Model

```plantuml
@startuml
allowmixing
title 01_db_init.sql - PostgreSQL Range Partitioning\n(Partition key: created_date, interval: 1 giờ)

skinparam shadowing false
skinparam roundcorner 8
skinparam packageStyle rectangle
skinparam entity {
  BackgroundColor #F8F9FA
  BorderColor #34495E
}
skinparam rectangle {
  BackgroundColor #EAF2F8
  BorderColor #2874A6
}

' =============== TABLES ===============
entity "citizen" as citizen {
  * id : UUID = gen_random_uuid() <<PK1>>
  --
  * first_name : VARCHAR(50)
  * last_name : VARCHAR(50)
  * full_name : VARCHAR(150)
  * birthday : DATE
  * created_date : TIMESTAMPTZ = now() <<PK2 / Partition Key>>
  * updated_date : TIMESTAMPTZ = now()
  --
  PRIMARY KEY (id, created_date)
  PARTITION BY RANGE (created_date)
}

entity "address" as address {
  * id : UUID = gen_random_uuid() <<PK1>>
  --
  * citizen_id : UUID <<FK1>>
  detail : TEXT
  ward : VARCHAR(100)
  province : VARCHAR(100)
  * address_type : VARCHAR(50)
  * created_date : TIMESTAMPTZ = now() <<PK2 / FK2 / Partition Key>>
  * updated_date : TIMESTAMPTZ = now()
  --
  PRIMARY KEY (id, created_date)
  FOREIGN KEY (citizen_id, created_date)
  REFERENCES citizen(id, created_date)
  PARTITION BY RANGE (created_date)
}

citizen ||--o{ address : "FK composite"

' =============== PARTITIONS ===============
package "Partitions tự động sinh theo giờ (pre-make 24h)\nĐặt tên: table_YYYYMMDD_HH24MI" as P {
  entity "citizen_YYYYMMDD_HHMI" as c_part {
    INDEX idx_partition_id ON (id)
  }
  entity "address_YYYYMMDD_HHMI" as a_part {
    INDEX idx_partition_id ON (id)
    INDEX idx_partition_citizen_id ON (citizen_id)
  }
}

citizen ||--o{ c_part : PARTITION OF
address ||--o{ a_part : PARTITION OF

' =============== FUNCTIONS ===============
package "Functions (plpgsql)" as F {
  rectangle "create_partition_for_timerange(\n  table_name TEXT,\n  start_time TIMESTAMPTZ,\n  end_time TIMESTAMPTZ\n) RETURNS VOID" as fn_range
  rectangle "create_future_partitions(\n  table_name TEXT,\n  num_intervals INT = 24\n) RETURNS VOID" as fn_future
}

fn_future ..> fn_range : PERFORM (loop 0..N)
fn_range ..> P : CREATE TABLE ... PARTITION OF\n+ CREATE INDEX

' =============== PG_CRON ===============
package "pg_cron jobs (chạy mỗi giờ)" as C {
  rectangle "create-citizen-partitions-hourly\nschedule: '0 * * * *'" as cron_c
  rectangle "create-address-partitions-hourly\nschedule: '0 * * * *'" as cron_a
}

cron_c ..> fn_future : SELECT create_future_partitions('citizen', 24)
cron_a ..> fn_future : SELECT create_future_partitions('address', 24)

note bottom of citizen
  Theo quy tắc PostgreSQL:
  Partition key bắt buộc
  phải nằm trong Primary Key
end note

@enduml
```

### 3. API Design
### 4. High Level Design
### 5. Deep Dive 1: The Immutable Ledger
### 6. Deep Dive 2: Cross-Shard Transactions
### 7. Deep Dive 3: Asynchronous Settlement & Reconciliation
### 8. Complete Architecture
### 9. Additional Discussion Points
