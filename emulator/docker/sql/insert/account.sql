CREATE TABLE fantasytennis.Account  (
	id BIGINT NOT NULL AUTO_INCREMENT,
    created DATETIME,
    modified DATETIME,
	ap INT,
	gameMaster BIT,
	lastLogin DATETIME,
	password VARCHAR(255),
	status INT,
	username VARCHAR(255),
    PRIMARY KEY (id)
);

INSERT INTO `fantasytennis`.`Account`(`ap`, `gameMaster`, `password`, `status`, `username`) VALUES (0, b'1', 'test', 0, 'test');