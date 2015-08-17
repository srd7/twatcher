# --- !Ups
CREATE TABLE IF NOT EXISTS TWEET(
  USER_ID LONG NOT NULL,
  TWEET_ID LONG NOT NULL,
  FOREIGN KEY (USER_ID) REFERENCES ACCOUNT(USER_ID)
);

# --- !Downs
DROP TABLE IF EXISTS TWEET;