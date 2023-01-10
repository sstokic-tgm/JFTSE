CREATE TABLE fantasytennis.GameServerType  (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name varchar(255),
    type tinyint,
    PRIMARY KEY (id)
);

INSERT INTO `fantasytennis`.`GameServerType`(`name`, `type`) VALUES ('Chat', 0);
INSERT INTO `fantasytennis`.`GameServerType`(`name`, `type`) VALUES ('Free', 1);
INSERT INTO `fantasytennis`.`GameServerType`(`name`, `type`) VALUES ('Rookie', 2);
INSERT INTO `fantasytennis`.`GameServerType`(`name`, `type`) VALUES ('Relay', 4);