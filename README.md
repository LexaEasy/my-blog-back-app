# my-blog-back-app

Бэкенд веб приложения блога на Spring Boot 3.2 с запуском во встроенном сервлет контейнере Tomcat.

## Технологии

Java 21

Maven

Spring Boot 3.2

Spring Boot Web

Spring Boot Data JDBC

H2 Database

JUnit 5

Spring Boot Test

## Структура проекта

`controller` обработка REST запросов

`service` бизнес логика приложения

`dao` доступ к данным через JDBC

`model` доменные модели и DTO

`config` прикладные конфигурации, например CORS

## Основные эндпоинты

`GET /api/posts?search=...&pageNumber=...&pageSize=...` получение списка постов с поиском и пагинацией

`POST /api/posts/{id}` получение поста по идентификатору

`POST /api/posts` создание нового поста

`PUT /api/posts/{id}` редактирование поста

`DELETE /api/posts/{id}` удаление поста

`POST /api/posts/{id}/likes` постановка лайка посту

`DELETE /api/posts/{id}/likes` снятие лайка с поста

`GET /api/posts/{id}/likes` получение текущего числа лайков поста

`PUT /api/posts/{id}/image` обновление картинки поста

`GET /api/posts/{id}/image` получение картинки поста

`GET /api/posts/{id}/comments` получение списка комментариев поста

`GET /api/posts/{postId}/comments/{commentId}` получение комментария поста по идентификатору

`POST /api/posts/{id}/comments` создание нового комментария к посту

`PUT /api/posts/{postId}/comments/{commentId}` редактирование комментария поста

`DELETE /api/posts/{postId}/comments/{commentId}` удаление комментария поста

## Сборка проекта

```bash
mvn clean package
```

Для Windows:

```powershell
mvn clean package
```

Готовый исполняемый jar файл будет лежать в:

`target/my-blog-back-app-0.0.1-SNAPSHOT.jar`

## Запуск тестов

```bash
mvn test
```

Для Windows:

```powershell
mvn test
```

## Запуск приложения

Через Maven:

```bash
mvn spring-boot:run
```

Для Windows:

```powershell
mvn spring-boot:run
```

Через исполняемый jar:

```bash
java -jar target/my-blog-back-app-0.0.1-SNAPSHOT.jar
```

Для Windows:

```powershell
java -jar target\\my-blog-back-app-0.0.1-SNAPSHOT.jar
```

После старта приложение доступно на `http://localhost:8080`

## Конфигурация БД

По умолчанию используется in memory база H2.

Параметры подключения находятся в [application.properties](C:\Projects\Y_Java\my blog\my-blog-back-app-boot\src\main\resources\application.properties)

Схема и seed данные создаются при старте приложения из [schema.sql](C:\Projects\Y_Java\my blog\my-blog-back-app-boot\src\main\resources\schema.sql)

Инициализация схемы включена через:

`spring.sql.init.mode=always`

## Конфигурация multipart

Максимальный размер файла и запроса задаётся в [application.properties](C:\Projects\Y_Java\my blog\my-blog-back-app-boot\src\main\resources\application.properties)

Используются значения:

`spring.servlet.multipart.max-file-size=5MB`

`spring.servlet.multipart.max-request-size=5MB`

## Поиск постов

Строка поиска разбивается на слова по пробелам

Пустые слова игнорируются

Слова с префиксом `#` считаются тегами

Обычные слова объединяются в строку поиска по названию

Фильтрация по названию и тегам происходит по логике И

## Тесты

MVC, DAO и service тесты переведены на Spring Boot Test.

Используются:

`@SpringBootTest`

`@AutoConfigureMockMvc`

`@Transactional` для DAO тестов

`@TestConfiguration` для изолированных mock конфигураций

## Примечания

Приложение больше не требует внешнего Tomcat и запускается как Spring Boot executable jar.

Точка входа приложения находится в [BackendApplication.java](C:\Projects\Y_Java\my blog\my-blog-back-app-boot\src\main\java\com\myblog\backend\BackendApplication.java)
