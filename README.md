# Twatcher

[日本語はこちら](README-ja.md)

## What is this?

When your Twitter is not updated for certain period, Twatcher will execute your script.  

## Usage

Set up to run this when your PC boots.
**If you die suddenly and your family boot your PC, Twatcher format your HDD/SSD.**  
etc...

## Caution

Whatever reason you do not update your Twitter, Twatcher will run the program.  
If Twatcher kills your important files, we do not take any responsibility.

## How to use

- Download zip [here](https://github.com/srd7/twatcher/releases/)
- Create your Twitter Client [here](https://apps.twitter.com/). Any permission is fine.
- Copy `conf/config-sample.json` to `conf/config.json` and modify it.
- In `token`  
    fill your Twitter ID (without @) to `screen_name`  
    fill `Access Token` and `Access Token Secret` in `Your Access Token` to `token` and `secret`  
- In `app`
    fill `Consumer Key (API Key)` and `Consumer Secret (API Secret)` to `key` and `secret`
- Execute script if you do not update your Twitter for `period` days. For the sample, it is 1 week.
- `scripts` is files you want to run.
- Write your Script `bin/script.bat` (for Windows).
- When you run `bin/twatcher.bat`, check your latest tweet date.
  Run the scripts if last tweet date is older than certain period.
- If you register `bin/twatcher.bat` to the startup, twatcher is executed when you boot the PC.

## Operation environment

- Java 1.8

## Operation check

- Windows 7 Home Premium 64bit(Java 1.8.0_51 64bit)

## Opinions

[Twitter](https://twitter.com/srd7)

## LICENSE

MIT
