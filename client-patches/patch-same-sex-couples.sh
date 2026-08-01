#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: bash client-patches/patch-same-sex-couples.sh PATH/TO/FantaTennis.exe

Removes the two client-side gender checks that reject same-sex proposals.
The server already accepts and persists relationships without a gender check.

The executable is validated at both patch locations before it is changed.
A sibling .bak file is created before the first patch.
EOF
}

if [[ $# -ne 1 || "$1" == "-h" || "$1" == "--help" ]]; then
    usage
    [[ $# -eq 1 ]] && exit 0
    exit 2
fi

executable="$1"
if [[ ! -f "$executable" ]]; then
    echo "Client executable not found: $executable" >&2
    exit 1
fi

if [[ -L "$executable" ]]; then
    echo "Refusing to patch a symlinked executable: $executable" >&2
    exit 1
fi

first_offset=$((0x1702e0))
second_offset=$((0x172395))
original_first="84d2746a"
original_second="84d27477"
patched="90909090"
approved_original_sha="eebd71a1b19eca60101a195c49999d29600e6cdd6302c4a6001fb98043f91162"
approved_patched_sha="ba3138048e92401eb19552b8f1a7be9eb5be8f2d84e35806fd856ecf392eb40e"

read_bytes() {
    od -An -tx1 -j "$2" -N 4 -- "$1" 2>/dev/null | tr -d ' \n'
}

executable_sha="$(sha256sum -- "$executable" | cut -d' ' -f1)"

if [[ "$executable_sha" == "$approved_patched_sha" ]]; then
    echo "Same-sex couple support is already enabled in $executable"
    exit 0
fi

if [[ "$executable_sha" != "$approved_original_sha" ]]; then
    echo "Unsupported FantaTennis.exe build; no changes were made. SHA-256: $executable_sha" >&2
    exit 1
fi

first_bytes="$(read_bytes "$executable" "$first_offset")"
second_bytes="$(read_bytes "$executable" "$second_offset")"

if [[ "$first_bytes" != "$original_first" || "$second_bytes" != "$original_second" ]]; then
    cat >&2 <<EOF
Unsupported FantaTennis.exe build; no changes were made.
Expected bytes: $original_first at 0x1702e0 and $original_second at 0x172395
Found bytes:    ${first_bytes:-<unreadable>} at 0x1702e0 and ${second_bytes:-<unreadable>} at 0x172395
EOF
    exit 1
fi

backup="$executable.bak"
if [[ -e "$backup" || -L "$backup" ]]; then
    if [[ -L "$backup" || ! -f "$backup" || "$backup" -ef "$executable" ]]; then
        echo "Existing backup is not an independent regular file; no changes were made: $backup" >&2
        exit 1
    fi
    if ! cmp -s -- "$executable" "$backup"; then
        echo "Existing backup does not match $executable; no changes were made: $backup" >&2
        exit 1
    fi
fi

if [[ ! -e "$backup" ]]; then
    cp -p --update=none -- "$executable" "$backup"
    if [[ ! -f "$backup" || -L "$backup" || "$backup" -ef "$executable" ]] ||
       ! cmp -s -- "$executable" "$backup"; then
        echo "Could not create an independent backup; no changes were made: $backup" >&2
        exit 1
    fi
fi

executable_dir="$(dirname -- "$executable")"
executable_name="$(basename -- "$executable")"
patched_copy="$(mktemp --tmpdir="$executable_dir" ".$executable_name.patch.XXXXXX")"
trap 'rm -f -- "$patched_copy"' EXIT
cp -p -- "$executable" "$patched_copy"

printf '\x90\x90\x90\x90' | dd of="$patched_copy" bs=1 seek="$first_offset" conv=notrunc status=none
printf '\x90\x90\x90\x90' | dd of="$patched_copy" bs=1 seek="$second_offset" conv=notrunc status=none

if [[ "$(read_bytes "$patched_copy" "$first_offset")" != "$patched" ||
      "$(read_bytes "$patched_copy" "$second_offset")" != "$patched" ||
      "$(sha256sum -- "$patched_copy" | cut -d' ' -f1)" != "$approved_patched_sha" ]]; then
    echo "Patch verification failed; restore from $backup" >&2
    exit 1
fi

mv -f -- "$patched_copy" "$executable"
trap - EXIT

echo "Enabled same-sex couples in $executable"
echo "Backup: $backup"
