CREATE TABLE `fantasytennis`.`GameServer` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `host` VARCHAR(255) DEFAULT NULL,
    `port` INT DEFAULT NULL,
    `gameServerType_id` bigint(20) DEFAULT NULL,
    `isCustomChannel` BIT DEFAULT NULL,
    `name` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`gameServerType_id`) REFERENCES `GameServerType` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `fantasytennis`.`GameServer` (host, isCustomChannel, name, port, gameServerType_id) VALUES ('127.0.0.1', b'0', NULL, 5895, 2);
INSERT INTO `fantasytennis`.`GameServer` (host, isCustomChannel, name, port, gameServerType_id) VALUES ('127.0.0.1', b'0', NULL, 5896, 4);
INSERT INTO `fantasytennis`.`GameServer` (host, isCustomChannel, name, port, gameServerType_id) VALUES ('127.0.0.1', b'0', NULL, 5895, 8);
INSERT INTO `fantasytennis`.`GameServer` (host, isCustomChannel, name, port, gameServerType_id) VALUES ('127.0.0.1', b'0', NULL, 5900, 1);