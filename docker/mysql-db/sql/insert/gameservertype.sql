CREATE TABLE `fantasytennis`.`GameServerType`  (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL,
    `type` tinyint DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Chat', 0);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Free', 1);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Rookie', 2);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Relay', NULL);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Pro', 3);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Master', 4);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Club', 7);
INSERT INTO `fantasytennis`.`GameServerType` (name, `type`) VALUES ('Tournament', 8);