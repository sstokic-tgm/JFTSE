FROM mysql:8.0.23
COPY docker/sql/create/create_fantasytennis.sql /docker-entrypoint-initdb.d/1.sql
COPY docker/sql/insert/gameservertype.sql /docker-entrypoint-initdb.d/2.sql
COPY docker/sql/insert/gameserver.sql /docker-entrypoint-initdb.d/3.sql
COPY docker/sql/insert/account.sql /docker-entrypoint-initdb.d/4.sql