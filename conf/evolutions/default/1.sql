# --- !Ups

CREATE TABLE IF NOT EXISTS CONFIG(
  PERIOD INT NOT NULL
);

CREATE TABLE IF NOT EXISTS SCRIPT(
  ID INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  PATH VARCHAR(1023) NOT NULL
);

CREATE TABLE IF NOT EXISTS ACCOUNT(
  USER_ID LONG PRIMARY KEY NOT NULL,
  SCREEN_NAME VARCHAR(20) NOT NULL,
  ACCESS_TOKEN VARCHAR(100) NOT NULL,
  ACCESS_TOKEN_SECRET VARCHAR(200) NOT NULL
);

# --- !Downs

DROP TABLE IF EXISTS CONFIG;
DROP TABLE IF EXISTS SCRIPT;
DROP TABLE IF EXISTS ACCOUNT;
