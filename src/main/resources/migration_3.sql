drop table if exists `challenge_definitions`;
create table if not exists `challenge_definitions` (
    `id` bigint not null,
    `name` varchar(255) not null,
    `active` boolean not null default true,
    `challenge_activation_type` smallint not null,
    `challenge_summary_type` smallint not null,
    `challenge_summary_number` smallint not null,
    `objectives` varchar(255) not null,
    `modifiedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`id`)
) DEFAULT CHARSET=utf8;

drop table if exists `challenge_states`;
create table if not exists `challenge_states` (
    `uuid` varchar(36) not null,
    `challenge_id` bigint not null,
    `challenge_status` smallint not null,
    `start` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary KEY (`uuid`)
) DEFAULT CHARSET=utf8;

-- insert into `challenge_definitions` (id, name, challenge_activation_type, challenge_summary_type ,challenge_summary_number, objectives) values(1, 'weekly', 1, 1, 3, 'DONATIONS');