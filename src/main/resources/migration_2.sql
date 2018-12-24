-- drop table if exists `measure_texts`;
create table if not exists `measure_texts` (
    `hash` bigint not null,
    `value` varchar(4000),
    `type` int(10) not null,
    `modifiedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`hash`, `modifiedAt`, `type`)
) DEFAULT CHARSET=utf8;

INSERT INTO measure_texts (hash, value, type, modifiedAt)
  SELECT hash, value, type, modifiedAt FROM measure_strings WHERE measure_strings.type=1;