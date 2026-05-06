CREATE TABLE `fantasytennis`.`Account` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `created` datetime(6) DEFAULT NULL,
    `modified` datetime(6) DEFAULT NULL,
    `ap` int(11) DEFAULT NULL,
    `banReason` varchar(255) DEFAULT NULL,
    `bannedUntil` datetime(6) DEFAULT NULL,
    `email` varchar(255) DEFAULT NULL,
    `gameMaster` bit(1) DEFAULT NULL,
    `lastLogin` datetime(6) DEFAULT NULL,
    `password` varchar(255) DEFAULT NULL,
    `status` int(11) DEFAULT NULL,
    `username` varchar(255) DEFAULT NULL COLLATE utf8_bin,
    `loggedInServer` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `fantasytennis`.`Account` (ap, gameMaster, password, status, username) VALUES (0, b'1', 'test', 0, 'test');