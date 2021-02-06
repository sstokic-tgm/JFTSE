CREATE TABLE fantasytennis.GameServer  (
    id BIGINT NOT NULL AUTO_INCREMENT,
    host VARCHAR(255),
    port INT,
    gameServerType_id BIGINT,
    FOREIGN KEY (gameServerType_id) REFERENCES GameServerType(id),
    PRIMARY KEY (id)
);

INSERT INTO `fantasytennis`.`GameServer`(`host`, `port`, `gameServerType_id`) VALUES ('127.0.0.1', 5895, 2);