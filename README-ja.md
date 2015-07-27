# Twatcher

## 何これ？

一定期間 Twitter の更新がなかった場合、突然死と見なして任意のスクリプトを実行します。

## 使用例

パソコン起動時に実行できるように仕掛けておき、
**突然死後に家族がパソコンを起動したらハードディスクのデータを消すようにしておく**  
など。

## 注意

突然死以外の事情で Twitter の更新がなかった場合でも作動します。  
それにより大切なデータが失われてしまっても、作者は一切責任を負いません。

## 使い方

- [こちら](https://github.com/srd7/twatcher/releases/)から zip をダウンロードしてください。
- [こちら](https://apps.twitter.com/)で、ツイッタークライアントを作ってください。権限は `Read only` で十分です。
- `conf/config-sample.json` を参考に `conf/config.json` を書きます。
- `token` の中の  
    `screen_name` に あなたの Twitter ID (@はナシ)  
    `token` `secret` に `Your Access Token` の欄にある `Access Token` および `Access Token Secret`  
  を書きます。
- `app` の中の  
    `key` `secret` に `Application Settings` の `Consumer Key (API Key)` `Consumer Secret (API Secret)`  
  を書きます。
- `period` は、何日間ツイートしてない時にスクリプトを実行するかを"日"単位で指定します。サンプルは一週間です。
- `scripts` は、実行したいスクリプトファイル名を記入します。
- `bin/script.bat` (Windowsの場合) に実行したいスクリプトを書きます。
- `bin/twatcher.bat` を実行すると、最新ツイート日をチェックし、指定期間以上ツイートがなかったらスクリプトを実行します。
- `bin/twatcher.bat` をスタートアップに登録しといたりすれば、パソコン起動時に実行します。

## 動作環境

- Java 1.8

## 動作確認

- Windows 7 Home Premium 64bit(Java 1.8.0_51 64bit)

## ご意見など

[Twitter](https://twitter.com/srd7)

## LICENSE

MIT

