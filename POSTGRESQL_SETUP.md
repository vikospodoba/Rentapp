# Настройка PostgreSQL для RentApp

## Шаг 1: Установка PostgreSQL

Если PostgreSQL ещё не установлен:
1. Скачай с официального сайта: https://www.postgresql.org/download/
2. Установи PostgreSQL (запомни пароль для пользователя `postgres`)

## Шаг 2: Создание базы данных

1. Открой **pgAdmin** или **psql** (командная строка PostgreSQL)
2. Подключись к серверу PostgreSQL
3. Создай базу данных:

```sql
CREATE DATABASE rentapp;
```

Или через командную строку:
```bash
psql -U postgres
CREATE DATABASE rentapp;
\q
```

## Шаг 3: Настройка application.properties

Файл `application.properties` уже настроен для PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rentapp
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Если у тебя другие настройки:

1. Открой `rentapp/src/main/resources/application.properties`
2. Измени параметры подключения:
   - `spring.datasource.url` - адрес и порт (по умолчанию `localhost:5432`)
   - `spring.datasource.username` - имя пользователя (по умолчанию `postgres`)
   - `spring.datasource.password` - пароль (по умолчанию `postgres`)

### Или используй переменные окружения:

```bash
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/rentapp"
$env:DB_USER="postgres"
$env:DB_PASSWORD="твой_пароль"
$env:DB_DRIVER="org.postgresql.Driver"

# Linux/Mac
export DB_URL="jdbc:postgresql://localhost:5432/rentapp"
export DB_USER="postgres"
export DB_PASSWORD="твой_пароль"
export DB_DRIVER="org.postgresql.Driver"
```

## Шаг 4: Запуск приложения

1. Убедись, что PostgreSQL запущен
2. Запусти приложение:
   ```bash
   mvnw spring-boot:run
   ```
   или через IntelliJ IDEA

3. При первом запуске Spring Boot автоматически создаст все таблицы (благодаря `spring.jpa.hibernate.ddl-auto=update`)

## Шаг 5: Проверка подключения

1. Открой приложение: http://localhost:8082
2. Если всё работает - подключение успешно!

## Возврат к H2 (для разработки)

Если хочешь вернуться к H2 (база в памяти):
1. В `application.properties` закомментируй PostgreSQL:
   ```properties
   #spring.datasource.url=jdbc:postgresql://localhost:5432/rentapp
   #spring.datasource.username=postgres
   #spring.datasource.password=postgres
   #spring.datasource.driver-class-name=org.postgresql.Driver
   ```

2. Раскомментируй H2:
   ```properties
   spring.datasource.url=jdbc:h2:mem:rentapp;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
   spring.datasource.username=sa
   spring.datasource.password=
   spring.datasource.driver-class-name=org.h2.Driver
   spring.h2.console.enabled=true
   spring.h2.console.path=/h2-console
   ```

## Возможные проблемы

### Ошибка: "Connection refused"
- Проверь, что PostgreSQL запущен
- Проверь порт (по умолчанию 5432)
- Проверь настройки firewall

### Ошибка: "password authentication failed"
- Проверь правильность пароля в `application.properties`
- Проверь, что пользователь `postgres` существует

### Ошибка: "database does not exist"
- Убедись, что база данных `rentapp` создана
- Проверь правильность имени базы в `application.properties`





