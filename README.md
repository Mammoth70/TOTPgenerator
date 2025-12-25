# TOTPgenerator<br>Генератор TOTP

[![Android][1]][2] [![Kotlin][3]][4] [![GitHub license][5]][6] [![GitHub code size in bytes][7]]()

[1]: https://img.shields.io/badge/Android-10+-blue.svg?logoColor=white&color=green
[2]: https://android.com/
[3]: https://img.shields.io/badge/kotlin-2.3-blue.svg?logo=kotlin
[4]: http://kotlinlang.org
[5]: https://img.shields.io/github/license/Mammoth70/TOTPgenerator.svg
[6]: LICENSE
[7]: https://img.shields.io/github/languages/code-size/Mammoth70/TOTPgenerator.svg?color=teal

Time-Based One-Time Password generator  
Генератор одноразовых паролей на основе времени  

Андроид-приложение для генерации одноразовых паролей на основе времени.  

Возможности приложения:
- можно загрузить ключ генерации вручную или отсканировав QR-код (схема otpauth://);
- можно перенести ключи из Google Authenticator отсканировав QR-код (схема otpauth-migration://);
- можно защитить вход PIN-кодом;
- ключи и PIN-код для входа хранятся в зашифрованном виде (AES256/GCM/NoPadding);
- ключ шифрования хранится в защищённом хранилище AndroidKeyStore;
- поддерживается алгоритмы HmacSHA1, HmacSHA256, HmacSHA512;
- поддерживается произвольный шаг времени;
- поддерживается генерация токенов TOTP длинной от 6-ти до 8-ми символов;
- можно нажатием скопировать токен в буфер обмена.

## История
Подробности см. в файле [HISTORY.md](HISTORY.md).  

## Лицензирование
Данный проект распространяется по лицензии **GNU General Public License v3.0 (GPLv3)**  
Подробности см. в файле [LICENSE](LICENSE).  
Автор 2025 Андрей Яковлев <andrey-yakovlev@yandex.ru>

Иконки Android Material Icons доступны по разрешительной лицензии Apache License 2.0,  
что означает, что их можно использовать бесплатно в личных, образовательных или коммерческих проектах.

## Licensing
This project is licensed under the **GNU General Public License v3.0 (GPLv3)**  
See the [LICENSE](LICENSE) file for details.  
Copyright 2025 Andrey Yakovlev <andrey-yakovlev@yandex.ru>

Android Material Icons are available under the permissive Apache License 2.0,  
which means they are free to use for personal, educational, or commercial projects without cost.
