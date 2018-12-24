-- drop table if exists `measure_strings`;
create table if not exists `measure_strings` (
    `hash` bigint not null,
    `value` varchar(512),
    `type` int(10) not null,
    `modifiedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`hash`, `modifiedAt`, `type`)
) DEFAULT CHARSET=utf8;

-- drop table if exists `measure_numbers`;
create table if not exists `measure_numbers` (
    `hash` bigint not null,
    `value` bigint,
    `type` int(10) not null,
    `modifiedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`hash`, `modifiedAt`, `type`)
) DEFAULT CHARSET=utf8;

-- drop table if exists `measure_decimals`;
create table if not exists `measure_decimals` (
    `hash` bigint not null,
    `value` decimal(10, 2),
    `type` int(10) not null,
    `modifiedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`hash`, `modifiedAt`, `type`)
) DEFAULT CHARSET=utf8;