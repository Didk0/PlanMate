#!/usr/bin/env bash
#
# Creates a set of sample PlanMate users for local testing, plus an `admin`
# app user with the ADMIN role. You need Keycloak to be up to run the script.
#
# planmate-realm.json seeds no application users at all (removed so the same
# realm file is safe to import straight into production) — this script is
# what replaces it locally, since PlanMate splits expenses *between people*
# and exercising groups/members/settlements needs several users to begin with.
#
# Every password equals the username. Safe to re-run: existing users are skipped.
#
# Usage:  ./docker/planmate/dev-users.sh
#
set -euo pipefail

KC="${KC:-http://localhost:9090}"
API="${API:-http://localhost:8080/api}"
REALM="${REALM:-planmate}"
KC_ADMIN="${KC_ADMIN:-admin}"
KC_ADMIN_PASS="${KC_ADMIN_PASS:-password}"

# username:FirstName:LastName
USERS=(
  "alice:Alice:Smith"
  "bob:Bob:Johnson"
  "carol:Carol:Williams"
  "david:David:Brown"
  "emma:Emma:Jones"
  "frank:Frank:Garcia"
  "grace:Grace:Miller"
  "henry:Henry:Davis"
  "ivy:Ivy:Rodriguez"
  "jack:Jack:Martinez"
)

command -v jq >/dev/null || { echo "jq is required"; exit 1; }

BACKEND_UP=1
if ! curl -sf -m 5 -o /dev/null "${API%/api}/actuator/health"; then
  BACKEND_UP=0
  echo "NOTE: backend not reachable at $API — Keycloak users will still be created,"
  echo "      but app-side user rows will be provisioned on first login instead."
  echo
fi

echo "==> Getting Keycloak admin token from $KC"
AT=$(curl -sf -X POST "$KC/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" -d "grant_type=password" \
  -d "username=$KC_ADMIN" -d "password=$KC_ADMIN_PASS" | jq -r '.access_token')

if [ -z "$AT" ] || [ "$AT" = "null" ]; then
  echo "Could not authenticate to Keycloak. Is the stack up?" >&2
  exit 1
fi

ALL_ROLES=$(curl -sf "$KC/admin/realms/$REALM/roles" -H "Authorization: Bearer $AT")
USER_ROLES=$(echo "$ALL_ROLES" | jq -c '[.[] | select(.name=="USER" or .name=="default-roles-planmate") | {id,name}]')
ADMIN_ROLES=$(echo "$ALL_ROLES" | jq -c '[.[] | select(.name=="USER" or .name=="ADMIN" or .name=="default-roles-planmate") | {id,name}]')

# create_user <username> <firstName> <lastName> <rolesJson>
# Password always equals the username. Skips creation if the user already
# exists, but still (re-)provisions the app-side row so the script stays safe
# to re-run after a `docker compose down -v`.
create_user() {
  local UN="$1" FN="$2" LN="$3" ROLES="$4" UID_

  EXISTING=$(curl -sf "$KC/admin/realms/$REALM/users?username=$UN&exact=true" \
    -H "Authorization: Bearer $AT" | jq -r '.[0].id // empty')

  if [ -n "$EXISTING" ]; then
    echo "==> $UN already exists, skipping creation"
    UID_="$EXISTING"
  else
    curl -sf -o /dev/null -X POST "$KC/admin/realms/$REALM/users" \
      -H "Authorization: Bearer $AT" -H "Content-Type: application/json" \
      -d "{
            \"username\": \"$UN\",
            \"enabled\": true,
            \"emailVerified\": true,
            \"email\": \"$UN@gmail.com\",
            \"firstName\": \"$FN\",
            \"lastName\": \"$LN\",
            \"credentials\": [{\"type\":\"password\",\"value\":\"$UN\",\"temporary\":false}]
          }"
    UID_=$(curl -sf "$KC/admin/realms/$REALM/users?username=$UN&exact=true" \
      -H "Authorization: Bearer $AT" | jq -r '.[0].id')
    curl -sf -o /dev/null -X POST \
      "$KC/admin/realms/$REALM/users/$UID_/role-mappings/realm" \
      -H "Authorization: Bearer $AT" -H "Content-Type: application/json" -d "$ROLES"
    echo "==> created $UN ($FN $LN)"
  fi

  [ "$BACKEND_UP" -eq 1 ] || return 0

  # A Keycloak identity alone is not enough: the app-side planmate.users row is
  # created lazily by POST /api/users. The frontend does this on login; do it
  # here so the user is immediately usable via the API and addable to groups.
  TOKEN=$(curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=planmate-frontend" -d "grant_type=password" \
    -d "username=$UN" -d "password=$UN" -d "scope=openid profile email" \
    | jq -r '.access_token') || TOKEN=""

  if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "    WARNING: could not log in as $UN — app-side row not provisioned" >&2
    return 0
  fi

  # `|| echo 000` keeps a connection failure from tripping `set -e`.
  CODE=$(curl -s -m 10 -o /dev/null -w '%{http_code}' -X POST "$API/users" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" || echo 000)

  case "$CODE" in
    200|201) echo "    provisioned app-side user row (HTTP $CODE)" ;;
    000)     echo "    WARNING: backend unreachable — row not provisioned" >&2 ;;
    *)       echo "    WARNING: provisioning returned HTTP $CODE" >&2 ;;
  esac
}

create_user admin Admin Admin "$ADMIN_ROLES"

for ENTRY in "${USERS[@]}"; do
  UN="${ENTRY%%:*}"; REST="${ENTRY#*:}"; FN="${REST%%:*}"; LN="${REST#*:}"
  create_user "$UN" "$FN" "$LN" "$USER_ROLES"
done

echo
echo "Done. Users ready (password == username):"
echo "  admin (ADMIN + USER)"
printf '  %s\n' "${USERS[@]%%:*}"
