CREATE TABLE IF NOT EXISTS `users` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`user_id` bigint(20) DEFAULT  NULL,
`username` varchar(20) DEFAULT NULL,
`name` varchar(255) DEFAULT NULL,
`photo` varchar(255) DEFAULT NULL,
`access_token` varchar(500) DEFAULT NULL,
`access_secret` varchar(500) DEFAULT NULL,
`verified` int(1) DEFAULT NULL,
`email` varchar(255) DEFAULT NULL,
`serial_id` varchar(255) DEFAULT NULL,
`remember_token` varchar(255) DEFAULT NULL,
`expires` datetime DEFAULT NULL,
`active` int(1) DEFAULT NULL,
`account_created` datetime DEFAULT CURRENT_TIMESTAMP,
`account_updated` datetime NULL DEFAULT NULL,
PRIMARY KEY (`id`),
UNIQUE KEY(`email`),
UNIQUE KEY(`username`),
INDEX (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;