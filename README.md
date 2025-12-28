# OAuth 2.0 Authorization Server

This project is a production-ready OAuth 2.0 Authorization Server built with **Spring Boot 3.4.1**, **Java 21**, and **Spring Authorization Server**. It supports OpenID Connect 1.0 and provides JDBC-backed persistence for clients and sessions.

## Features

- **OAuth 2.0 & OIDC 1.0**: Full implementation of the authorization server protocol.
- **JDBC Persistence**: Stores registered clients, authorizations, and consents in a PostgreSQL database.
- **Environment Separation**: Uses Spring Profiles (`local` vs. default/prod) for flexible configuration.
- **Docker Ready**: Includes a multi-stage `Dockerfile` for QA and Production deployments.

---

## Local Setup Guide (Native)

Follow these steps to get the server running on your local machine using your IDE.

### 1. Prerequisites

- **Java 21** installed.
- **Homebrew** (recommended for macOS).

### 2. Install PostgreSQL (macOS)

If you don't have PostgreSQL installed, follow these steps:

1.  **Install via Homebrew**:
    ```bash
    brew install postgresql@16
    ```
2.  **Start the Service**:
    ```bash
    brew services start postgresql@16
    ```
3.  **Create the Database & User**:
    By default, Homebrew creates a user with your Mac username and no password. To match the project's local config:
    ```bash
    # Enter Postgres CLI
    psql postgres
    ```
    Inside the `psql` prompt, run:
    ```sql
    -- Create the database
    CREATE DATABASE oauth_db;

    -- Create the 'postgres' user with password 'password'
    CREATE USER postgres WITH PASSWORD 'password';
    GRANT ALL PRIVILEGES ON DATABASE oauth_db TO postgres;

    -- Exit
    \q
    ```

### 3. Run the Application

You can run the application using the Gradle wrapper. **Important:** You must activate the `local` profile.

#### Using Terminal:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### Using IntelliJ IDEA:
1. Open the "Run/Debug Configurations".
2. Add `local` to the "Active profiles" field.
3. Click "Run".

### 4. Verify the Setup

Once the app is running, visit the OpenID Connect discovery endpoint:
[http://localhost:9000/.well-known/openid-configuration](http://localhost:9000/.well-known/openid-configuration)

---

## Testing OAuth 2.0 Flows

### Default Credentials
The application comes with a pre-registered client and user for testing:
- **Client ID**: `oidc-client`
- **Client Secret**: `secret`
- **User**: `user` / `password`

### Get a Token (Client Credentials Flow)
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -u oidc-client:secret \
  -d "grant_type=client_credentials&scope=openid profile"
```

---

## Deployment (QA/Production)

### Build Docker Image
```bash
docker build -t oauth-server:latest .
```

### Run Container
Inject your environment-specific database details via environment variables:

```bash
docker run -p 9000:9000 \
  -e DB_URL=jdbc:postgresql://your-db-host:5432/your_db \
  -e DB_USERNAME=your_user \
  -e DB_PASSWORD=your_password \
  -e SQL_INIT_MODE=never \
  -e JPA_DDL_AUTO=validate \
  oauth-server:latest
```

