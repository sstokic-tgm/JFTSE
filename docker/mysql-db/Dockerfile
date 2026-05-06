FROM mysql:8.0.23

COPY sql/create/create_fantasytennis.sql /docker-entrypoint-initdb.d/1.sql
COPY sql/insert/account.sql /docker-entrypoint-initdb.d/2.sql
COPY sql/insert/gameservertype.sql /docker-entrypoint-initdb.d/3.sql
COPY sql/insert/gameserver.sql /docker-entrypoint-initdb.d/4.sql