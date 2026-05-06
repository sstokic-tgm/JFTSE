#!/bin/bash

# Database credentials
DB_NAME="fantasytennis"
DB_USER="jftse"
DB_PASS="jftse"
DB_HOST="127.0.0.1"

# Secure MySQL authentication (avoid password exposure)
export MYSQL_PWD="$DB_PASS"

# Directory containing the SQL scripts
SQL_DIR="$(dirname "$0")/sql"

# Ensure SQL directory exists
if [[ ! -d "$SQL_DIR" ]]; then
    echo "Error: SQL directory $SQL_DIR does not exist."
    exit 1
fi

# Ordered list of SQL files to be executed
SQL_FILES=(
    "config.sql"
    "status.sql"
    "maps.sql"
    "scenarios.sql"
    "map2scenarios.sql"
    "guardian2maps.sql"
    "skill2guardians.sql"
    "guardianmultiplier.sql"
    "relationshiproles.sql"
    "relationshiptypes.sql"
    "relationships.sql"
)

# Check if mysql client is installed
if ! command -v mysql &> /dev/null; then
    echo "Error: MySQL client is not installed. Install it using: sudo apt install mysql-client"
    exit 1
fi

# MySQL command for execution
MYSQL_CMD="mysql -u$DB_USER -h $DB_HOST $DB_NAME"

# Test MySQL connection
echo "Testing MySQL connection..."
if ! mysql -u"$DB_USER" -h "$DB_HOST" -e "USE $DB_NAME;" 2>/dev/null; then
    echo "Error: Could not connect to MySQL database '$DB_NAME'. Check credentials and database status."
    exit 1
fi
echo "MySQL connection successful."

# Execute each SQL file in order
for FILE in "${SQL_FILES[@]}"; do
    FILE_PATH="$SQL_DIR/$FILE"

    if [[ -f "$FILE_PATH" ]]; then
        echo "Importing $FILE..."
        $MYSQL_CMD < "$FILE_PATH"

        if [[ $? -ne 0 ]]; then
            echo "Error: Failed to import $FILE"
            exit 1
        fi
    else
        echo "Warning: $FILE_PATH not found, skipping..."
    fi
done

# Unset MySQL password
unset MYSQL_PWD

echo "All SQL files imported successfully."
exit 0