cws
=================


[![Maven Central](https://img.shields.io/maven-central/v/com.uchicom/cws.svg)](http://search.maven.org/#search|ga|1|com.uchicom.cws)
[![Maven Central](https://img.shields.io/github/license/uchicom/cws.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)


Convert to withholding slip  

csvに源泉徴収票に出力したい内容をまとめて記載して、  
国税庁からダウンロードしたPDF（源泉徴収票の部分のみ）  
へ転記して保存します。  

Adobe Readerでの印刷を想定しています。  
そのうち一括印刷機能も追加します。

Usage
-----

To build this project, use Apache Maven 2.2.1 or newer and run:

    mvn clean install

To use cws from a Maven project, add:

    <dependency>
        <groupId>com.uchicom</groupId>
        <artifactId>cws</artifactId>
        <version>1.0.0</version>
    </dependency>
