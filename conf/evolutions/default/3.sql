# --- !Ups
ALTER TABLE ACCOUNT ADD COLUMN GOODBYE_FLAG BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ACCOUNT ADD COLUMN TWEET_DELETE_FLAG BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ACCOUNT ADD COLUMN FAVORITE_DELETE_FLAG BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ACCOUNT ADD COLUMN UPDATE_PROFILE VARCHAR(160) NULL;

# --- !Downs
ALTER TABLE ACCOUNT DROP COLUMN UPDATE_PROFILE;
ALTER TABLE ACCOUNT DROP COLUMN FAVORITE_DELETE_FLAG;
ALTER TABLE ACCOUNT DROP COLUMN TWEET_DELETE_FLAG;
ALTER TABLE ACCOUNT DROP COLUMN GOODBYE_FLAG;
