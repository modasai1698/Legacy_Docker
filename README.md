# Legacy Docker – modernised

A small registration web app, rebuilt to be simple, good-looking and buildable on **Java 21**.

## What changed

| Before | After |
|---|---|
| Java 1.7 source/target (rejected by JDK 21) | `maven.compiler.release` = **21** |
| `javax.servlet` 2.5, `web.xml` 2.5 | **Jakarta Servlet 6.0**, `web.xml` 6.0 |
| JUnit 4 (`junit-dep`), Mockito 1.8, Hamcrest | **JUnit 5** (Jupiter) |
| 2011-era Maven plugins, dead report plugins (FindBugs, etc.) | Current plugin versions, reporting config removed |
| Jetty 8 milestone plugin | Jetty 12 (EE10) plugin |
| Unstyled form posting to a non-existent `action_page.php` | Styled page + `/register` servlet with server-side validation |
| No Dockerfile | Multi-stage **Dockerfile** (Maven → Tomcat 10.1, JRE 21) |
| k8s YAML in project root | Moved to `k8s/` (same image/names, added probes) |

## Requirements

* JDK 21
* Maven 3.6.3+ (3.9.x recommended)

## Build & test

```bash
mvn clean package
```

The WAR is created at `webapp/target/webapp.war`.

## Run it

**Option A – Docker (recommended)**

```bash
docker build -t regapp .
docker run --rm -p 8080:8080 regapp
```

Open <http://localhost:8080>

**Option B – Jetty from Maven (no Docker)**

```bash
mvn clean install
cd webapp
mvn jetty:run
```

Open <http://localhost:8080>

**Option C – your own Tomcat**

Copy `webapp/target/webapp.war` into the `webapps/` folder of **Tomcat 10.1 or newer**.
(Tomcat 9 and older will *not* work – they use the old `javax.*` packages.)
The app is then at <http://localhost:8080/webapp/>.

## Kubernetes

```bash
kubectl apply -f k8s/regapp-deploy.yml
kubectl apply -f k8s/regapp-service.yml
```

Change `image: valaxy/regapp` in `k8s/regapp-deploy.yml` to your own image if needed.

## Project layout

```
legacy-docker/
├── pom.xml                      parent POM (versions, Java 21)
├── Dockerfile
├── docker-compose.yml           app + MySQL with a persistent volume
├── k8s/                         Deployment + Service
├── server/                      plain-Java logic + unit tests
│   └── src/main/java/com/example/{Greeter,Registration,UserStore,InMemoryUserStore,JdbcUserStore,PasswordHasher}.java
└── webapp/                      WAR: UI + servlet
    └── src/main
        ├── java/com/example/web/{Register,Login,Welcome,Logout}Servlet.java (+ Pages, Html, AppInit: picks the store)
        └── webapp/{index.html, assets/style.css, WEB-INF/web.xml}
```

## Storage: in-memory or MySQL

The app picks its storage from the environment variable **`DB_URL`**:

| `DB_URL` | Storage | Users survive a restart? |
|---|---|---|
| not set | in memory (demo mode) | No |
| `jdbc:mysql://host:3306/regapp` | **MySQL** (table `users` is created automatically) | **Yes** |

Settings: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### Run with MySQL (Docker Compose, recommended)

```bash
cp .env.example .env          # then edit the two passwords
docker compose up -d --build
docker compose logs -f app    # wait for: "Using the MySQL/JDBC user store (persistent)"
```

Open <http://localhost:8080>. Data lives in the `db-data` Docker volume.

* Stop: `docker compose down` (data is kept). Delete the data too: `docker compose down -v`.
* Look inside the database: `docker compose exec db mysql -uregapp -p regapp -e "select email,name,created_at from users"`
* Change the passwords in `.env` **before the first start**. MySQL only reads them when it creates
  an empty volume; later changes need `docker compose down -v` (which deletes the data).

### Run with MySQL without Compose

```bash
docker network create regnet
docker run -d --name regdb --network regnet -v regdb-data:/var/lib/mysql \
  -e MYSQL_DATABASE=regapp -e MYSQL_USER=regapp -e MYSQL_PASSWORD=change-me \
  -e MYSQL_ROOT_PASSWORD=change-me-too mysql:8.4
docker build -t regapp .
docker run -d --name regapp --network regnet -p 8080:8080 \
  -e DB_URL='jdbc:mysql://regdb:3306/regapp?useSSL=false&allowPublicKeyRetrieval=true' \
  -e DB_USER=regapp -e DB_PASSWORD=change-me regapp
```

### Amazon RDS (MySQL)

Skip the `db` container and point the app at your RDS endpoint (the RDS security group must allow
port 3306 from your EC2 instance):

```bash
docker run -d --name regapp -p 8080:8080 \
  -e DB_URL='jdbc:mysql://<rds-endpoint>:3306/regapp' \
  -e DB_USER=<user> -e DB_PASSWORD=<password> regapp
```

Create the empty `regapp` database first; the app creates the table itself. `useSSL=false` is only
for the private Docker network above – for RDS leave it out.

## How the demo works

| Page | URL | What it does |
|---|---|---|
| Register | `/` | Create an account (validated in the browser and again on the server) |
| Sign in | `/login` | Log in with the email + password you registered |
| Welcome | `/welcome` | Only visible when signed in, otherwise redirects to `/login` |
| Sign out | `POST /logout` | Ends the session |

Passwords are never stored in clear text (salted PBKDF2 hash). **Login sessions** are kept in the
memory of the running app, so restarting it signs everyone out (accounts are kept when MySQL is used).
With several Kubernetes replicas, either enable sticky sessions on the Service/Ingress or use one replica.

## Notes

* This is still a demo: no rate limiting on login, no email verification, no password reset.
* The page follows your OS light/dark setting automatically.
