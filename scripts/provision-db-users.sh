#!/bin/sh
# Runs once per Compose deployment after Flyway. Never echo SQL or secret values.
set -eu

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${APP_DB_PASSWORD:?APP_DB_PASSWORD is required}"
: "${NACOS_DB_PASSWORD:?NACOS_DB_PASSWORD is required}"
: "${FILE_DB_PASSWORD:?FILE_DB_PASSWORD is required}"
: "${USER_DB_PASSWORD:?USER_DB_PASSWORD is required}"
: "${ACTIVITY_DB_PASSWORD:?ACTIVITY_DB_PASSWORD is required}"
: "${PROJECT_AWARD_DB_PASSWORD:?PROJECT_AWARD_DB_PASSWORD is required}"
: "${HR_DB_PASSWORD:?HR_DB_PASSWORD is required}"
: "${MONITOR_DB_PASSWORD:?MONITOR_DB_PASSWORD is required}"
: "${CONTENT_DB_PASSWORD:?CONTENT_DB_PASSWORD is required}"
: "${AI_DB_PASSWORD:?AI_DB_PASSWORD is required}"

if [ "${DB03_BUSINESS_DB:-software_group}" != 'software_group' ]; then
    echo "DB-03 grants currently target only software_group" >&2
    exit 1
fi

mysql_host="${DB03_MYSQL_HOST:-mysql}"
mysql_port="${DB03_MYSQL_PORT:-3306}"
grants_file="${DB03_GRANTS_FILE:-/provision/runtime-grants.sql}"
mysql_client="${DB03_MYSQL_CLIENT:-mysql}"

if [ ! -r "$grants_file" ]; then
    echo "DB-03 grant file is missing" >&2
    exit 1
fi

seen='|'
check_password() {
    account="$1"
    password="$2"
    case "$password" in
        *[!A-Za-z0-9_-]*|'')
            echo "Invalid password format for $account: use 32-128 random letters, digits, _ or -" >&2
            exit 1
            ;;
    esac
    if [ "${#password}" -lt 32 ] || [ "${#password}" -gt 128 ]; then
        echo "Invalid password length for $account: use 32-128 characters" >&2
        exit 1
    fi
    case "$seen" in
        *"|$password|"*)
            echo "Duplicate database password for $account" >&2
            exit 1
            ;;
    esac
    seen="${seen}${password}|"
}

emit_account() {
    account="$1"
    password="$2"
    printf "CREATE USER IF NOT EXISTS '%s'@'%%' IDENTIFIED BY '%s';\n" "$account" "$password"
    printf "ALTER USER '%s'@'%%' IDENTIFIED BY '%s';\n" "$account" "$password"
}

# Validate the entire secret set before sending any SQL to MySQL.
check_password sg_app_svc "$APP_DB_PASSWORD"
check_password sg_nacos_svc "$NACOS_DB_PASSWORD"
check_password sg_file_svc "$FILE_DB_PASSWORD"
check_password sg_user_svc "$USER_DB_PASSWORD"
check_password sg_activity_svc "$ACTIVITY_DB_PASSWORD"
check_password sg_project_award_svc "$PROJECT_AWARD_DB_PASSWORD"
check_password sg_hr_svc "$HR_DB_PASSWORD"
check_password sg_monitor_svc "$MONITOR_DB_PASSWORD"
check_password sg_content_svc "$CONTENT_DB_PASSWORD"
check_password sg_ai_svc "$AI_DB_PASSWORD"

export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"
{
    emit_account sg_app_svc "$APP_DB_PASSWORD"
    emit_account sg_nacos_svc "$NACOS_DB_PASSWORD"
    emit_account sg_file_svc "$FILE_DB_PASSWORD"
    emit_account sg_user_svc "$USER_DB_PASSWORD"
    emit_account sg_activity_svc "$ACTIVITY_DB_PASSWORD"
    emit_account sg_project_award_svc "$PROJECT_AWARD_DB_PASSWORD"
    emit_account sg_hr_svc "$HR_DB_PASSWORD"
    emit_account sg_monitor_svc "$MONITOR_DB_PASSWORD"
    emit_account sg_content_svc "$CONTENT_DB_PASSWORD"
    emit_account sg_ai_svc "$AI_DB_PASSWORD"
    cat "$grants_file"
} | "$mysql_client" --protocol=tcp --host="$mysql_host" --port="$mysql_port" --user=root --batch

echo "DB-03 runtime database accounts and grants applied"
