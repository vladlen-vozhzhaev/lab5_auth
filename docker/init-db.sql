-- Создаём пользователя program (если его нет)
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'program') THEN
CREATE ROLE program LOGIN PASSWORD 'test';
END IF;
END
$$;


CREATE DATABASE ratings     OWNER program;
CREATE DATABASE libraries   OWNER program;
CREATE DATABASE reservations OWNER program;


GRANT ALL PRIVILEGES ON DATABASE ratings      TO program;
GRANT ALL PRIVILEGES ON DATABASE libraries    TO program;
GRANT ALL PRIVILEGES ON DATABASE reservations TO program;