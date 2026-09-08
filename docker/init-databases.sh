#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE petstore_user_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'petstore_user_db')\gexec
    SELECT 'CREATE DATABASE petstore_catalog_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'petstore_catalog_db')\gexec
    SELECT 'CREATE DATABASE petstore_order_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'petstore_order_db')\gexec
    SELECT 'CREATE DATABASE petstore_inventory_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'petstore_inventory_db')\gexec
    GRANT ALL PRIVILEGES ON DATABASE petstore_user_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE petstore_catalog_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE petstore_order_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE petstore_inventory_db TO $POSTGRES_USER;
EOSQL
