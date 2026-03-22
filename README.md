# TOTP generator<br>Генератор TOTP

[![Android][1]][2] [![Kotlin][3]][4] [![GitHub license][5]][6] [![GitHub code size in bytes][7]]()

[1]: https://img.shields.io/badge/Android-9+-blue.svg?logo=Android&color=brightgreen
[2]: https://android.com/
[3]: https://img.shields.io/badge/Kotlin-2.2-blue.svg?logo=Kotlin
[4]: http://kotlinlang.org
[5]: https://img.shields.io/github/license/Mammoth70/TOTPgenerator.svg
[6]: LICENSE
[7]: https://img.shields.io/github/languages/code-size/Mammoth70/TOTPgenerator.svg?color=teal

Time-Based One-Time Password generator.  
Генератор одноразовых паролей на основе времени.  

Генерация паролей
- длина от шести до восьми символов;
- алгоритмы HmacSHA1, HmacSHA256 и HmacSHA512;
- произвольный шаг времени.

Импорт и экспорт секретов
- через QR-код (схемы otpauth и otpauth-migration);
- через открытый или зашифрованный (AES256/GCM/NoPadding на ключе из хеша пароля PBKDF2WithHmacSHA256) файл JSON;
- добавление секретов вручную.

Безопасность
- вход в приложение может быть защищён PIN-кодом или строгой биометрией;
- секреты и хеш PIN-кода хранятся в зашифрованном виде (AES256/GCM/NoPadding);
- ключ шифрования хранится в защищённом хранилище AndroidKeyStore.

Интерфейс
- опциональный сдвиг времени генерации паролей вперёд до пяти секунд для улучшения UX;
- опциональная генерация пароля, следующего за текущим для авторизации на сервере с ушедшим вперёд временем;
- копирование пароля в буфер обмена нажатием;
- не требуется запроса разрешений, в том числе и разрешения "CAMERA permission";
- дневная, системная и ночная темы;
- английский и русский языки.

## История
Подробности см. в файле [HISTORY.md](HISTORY.md).  

## Лицензирование
Данный проект распространяется по лицензии **GNU General Public License v3.0 (GPLv3)**  
Подробности см. в файле [LICENSE](LICENSE).  
Автор 2025-2026 Андрей Яковлев <andrey-yakovlev@yandex.ru>

Иконки Android Material Icons доступны по разрешительной лицензии Apache License 2.0,  
что означает, что их можно использовать бесплатно в личных, образовательных или коммерческих проектах.

## Licensing
This project is licensed under the **GNU General Public License v3.0 (GPLv3)**  
See the [LICENSE](LICENSE) file for details.  
Copyright 2025-2026 Andrey Yakovlev <andrey-yakovlev@yandex.ru>

Android Material Icons are available under the permissive Apache License 2.0,  
which means they are free to use for personal, educational, or commercial projects without cost.
