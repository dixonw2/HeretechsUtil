CREATE TABLE IF NOT EXISTS Player (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    UUID NVARCHAR(128) NOT NULL,
    PlayerName NVARCHAR(128) NOT NULL,
    Points DECIMAL NOT NULL,
    Lives INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS World (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    WorldName NVARCHAR(128) NOT NULL,
    UUID NVARCHAR(128) NOT NULL,
    Active BOOL NOT NULL
);

CREATE TABLE IF NOT EXISTS PlayerWorld (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	idPlayer INT NOT NULL,
    idWorld INT NOT NULL,
    CONSTRAINT FOREIGN KEY FK_PlayerWorld_idPlayer (idPlayer) REFERENCES Player (id),
    CONSTRAINT FOREIGN KEY FK_PlayerWorld_idWorld (idWorld) REFERENCES World (id)
);

CREATE TABLE IF NOT EXISTS Task (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    TaskDescription NVARCHAR(256) NOT NULL,
    Difficulty NVARCHAR(32) NOT NULL,
    PointReward INT NOT NULL,
    CONSTRAINT U_Task_TaskDescription UNIQUE (TaskDescription)
);

CREATE TABLE IF NOT EXISTS PlayerWorldTask (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Completed BOOL NOT NULL,
    Assigned BOOL NOT NULL,
    idTask INT NOT NULL,
    idPlayerWorld INT NOT NULL,
    CONSTRAINT FOREIGN KEY FK_PlayerWorldTask_idTask (idTask) REFERENCES Task (id),
    CONSTRAINT FOREIGN KEY FK_PlayerWorldTask_idPlayerWorld (idPlayerWorld) REFERENCES PlayerWorld (id)
);