<div align="center"> <h1 align="center"> Telegram/AviaBot! </h1> </div>


Телеграм бот для поиска авиабилетов и оформления подписок, использующий бесплатное API Aviasales.
Обратите внимание, все данные передаются из кеша, который формируется на основании поисков пользователей сайтов Aviasales за последние 48 часов.
___
#### ОБРАТИТЕ ВНИМАНИЕ, ЧТО ЭТОТ ПРОЕКТ НЕ СМОЖЕТ ЗАПУСТИТЬСЯ
Из-за ограничений безопасности этот репозиторий не содержит токенов.

Если вы хотите запустить этот код, выполните следующие действия.

Добавить значения следующих свойств в файл конфигурации application.properties:
- токен для API Aviasales (travelpayouts.accessToken);
- свойства Телеграм бота (telegrambot.userName, telegrambot.botToken, telegrambot.webHookPath);
- свойства БД (spring.datasource.url, spring.datasource.username, spring.datasource.password).

___
### Используемые технологии
Java 17, Spring (Boot, Data JPA, Scheduled), Hibernate, PostgreSQL, Lombok, API Aviasales.

___
### Функции
- Поиск всех городов с аэропортом по названию страны
- Поиск самого дешевого билета в одну сторону по выбранным критериям (город отправления, город назначения, дата отправления)
- Под каждым найденным билетом реализованы кнопки для перехода на страницу https://www.aviasales.ru с текущим билетом и для оформления подписки
- Вывод пользователю списка всех подписок с возможностью отписаться
- Оформленная подписка позволяет ежеденевно получать обновления по стоимости билета. 
Если билет в подписке потерял актуальность, то будет отправлено автоматическое оповещение и произведена отписка.

___
### Скриншоты

Основное меню:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/menu.png)

Справочник городов:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/search_cities_with_airport_by_name_country.png)

Поиск самого дешевого билета:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/search_ticket.png)

Подписка на билет:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/subscribe.png)

Отписка от билета:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/unsubscribe.png)

Ежедневное обновление подписок:

![ScreenShot](https://raw.github.com/StasMalykhin/aviabot/master/screenshots/update_tickets.png)


___
### Авторы
* Stanislav Malykhin - [StasMalykhin](https://github.com/StasMalykhin)

___
### Лицензия
This project is Apache License 2.0 - see the [LICENSE](LICENSE) file for details

___
### Внесение изменений
Не стесняйтесь предлагать новые функции через [github issue](https://github.com/StasMalykhin/aviabot/issues/new).
Обратите внимание, что новые функции должны быть предварительно одобрены до внедрения.
