CREATE TABLE `user`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT,
    `user_id`     varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `name`        varchar(50) COLLATE utf8mb4_general_ci          DEFAULT NULL,
    `code`        varchar(30) COLLATE utf8mb4_general_ci          DEFAULT NULL,
    `create_time` datetime                                        DEFAULT NULL,
    `update_time` datetime                                        DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `score`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT,
    `name`        varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `user_id`     varchar(100) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `score`       int                                              DEFAULT NULL,
    `hurdle`      int                                              DEFAULT NULL,
    `create_time` datetime                                         DEFAULT NULL,
    `update_time` datetime                                         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY           `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


