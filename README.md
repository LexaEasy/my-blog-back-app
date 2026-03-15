# my-blog-back-app

Бэкенд веб приложения блога на Spring Framework 6 для запуска в сервлет контейнере Tomcat.

## Технологии

Java 21

Gradle

Spring Framework 6

Spring Web MVC

Spring JDBC

H2 Database

JUnit 5

Spring TestContext Framework

## Структура проекта

`controller` обработка REST запросов

`service` бизнес логика приложения

`dao` доступ к данным через JDBC

`model` доменные модели и DTO

`config` Java конфигурация приложения, MVC и БД

## Основные эндпоинты

`GET /api/posts?search=...&pageNumber=...&pageSize=...`

`POST /api/posts/{id}` получение поста по идентификатору

`POST /api/posts`

`PUT /api/posts/{id}`

`DELETE /api/posts/{id}`

`POST /api/posts/{id}/likes`

`DELETE /api/posts/{id}/likes`

`PUT /api/posts/{id}/image`

`GET /api/posts/{id}/image`

`GET /api/posts/{id}/comments`

`GET /api/posts/{postId}/comments/{commentId}`

`POST /api/posts/{id}/comments`

`PUT /api/posts/{postId}/comments/{commentId}`

`DELETE /api/posts/{postId}/comments/{commentId}`

## Сборка проекта

```bash
./gradlew clean build
```

Для Windows:

```powershell
.\gradlew.bat clean build
```

## Запуск тестов

```bash
./gradlew test
```

Для Windows:

```powershell
.\gradlew.bat test
```

## Сборка war

```bash
./gradlew war
```

Для Windows:

```powershell
.\gradlew.bat war
```

Готовый war файл будет лежать в:

`build/libs/my-blog-back-app-0.0.1-SNAPSHOT.war`

## Деплой в Tomcat

1. Собрать war

2. Скопировать `build/libs/my-blog-back-app-0.0.1-SNAPSHOT.war` в директорию `webapps` вашего Tomcat

3. Запустить Tomcat

4. Проверить, что приложение доступно на `http://localhost:8080`

## Конфигурация БД

По умолчанию используется in memory база H2.

Параметры подключения находятся в [application.properties](C:\Projects\Y_Java\my blog\my-blog-back-app\src\main\resources\application.properties)

Схема и seed данные создаются при старте приложения из [schema.sql](C:\Projects\Y_Java\my blog\my-blog-back-app\src\main\resources\schema.sql)

## Поиск постов

Строка поиска разбивается на слова по пробелам

Пустые слова игнорируются

Слова с префиксом `#` считаются тегами

Обычные слова объединяются в строку поиска по названию

Фильтрация по названию и тегам происходит по логике И

## Примечания

Приложение собирается как обычный war без использования Spring Boot

Конфигурация servlet контейнера находится в [web.xml](C:\Projects\Y_Java\my blog\my-blog-back-app\src\main\webapp\WEB-INF\web.xml)
