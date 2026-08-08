# Bank Auth JWT / JWE

Demo auth-сервис для банковского сценария: регистрация и логин, access token как **JWT** (подпись HS256), refresh token как **JWE** (шифрование RSA-OAEP-256 + A256GCM).



## Endpoints


| Method | Path                 | Описание                |
| ------ | -------------------- | ----------------------- |
| POST   | `/api/auth/register` | Регистрация             |
| POST   | `/api/auth/login`    | Логин                   |
| POST   | `/api/auth/refresh`  | Обновление пары токенов |
| GET    | `/api/me`            | Профиль (Bearer JWT)    |


## Стек

Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2, Nimbus JOSE+JWT

## Запуск

```bash
./gradlew bootRun
```

Переменные окружения (опционально):

- `AUTH_JWT_SECRET` — секрет для JWT access token



## Примеры

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"client1\",\"password\":\"Secret123!\"}"

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"client1\",\"password\":\"Secret123!\"}"

curl http://localhost:8080/api/me -H "Authorization: Bearer <accessToken>"
```



## Тесты

```bash
./gradlew test
```



## JWT vs JWE

- **JWT (access)** — подписанный токен, передаётся в `Authorization: Bearer ...`
- **JWE (refresh)** — зашифрованный токен, используется только для `/api/auth/refresh`





