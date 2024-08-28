CREATE TABLE `messages`
(
    `ID`              int(11)                      NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `message`       varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'message',
    PRIMARY KEY (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;

CREATE TABLE `user`
(
    `ID`              int(11)                      NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `name`       varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'name',
    `type`       varchar(50) COLLATE utf8_bin NOT NULL COMMENT 'type',
    `createdDate`       DATETIME NOT NULL COMMENT 'createdDate',

    PRIMARY KEY (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;