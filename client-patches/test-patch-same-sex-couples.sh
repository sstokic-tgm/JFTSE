#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
patcher="$script_dir/patch-same-sex-couples.sh"
client_source="${FANTA_TENNIS_EXE:-$script_dir/../.jftse-client-linux/client/FantaTennis.exe}"
fixture="$(mktemp)"
matching_fixture="$(mktemp)"
wrong_fixture="$(mktemp)"
stale_fixture="$(mktemp)"
hardlink_fixture="$(mktemp)"
symlink_fixture="$(mktemp)"
target_symlink_fixture="$(mktemp)"
target_symlink_path="$target_symlink_fixture.link"
fault_fixture="$(mktemp)"
fault_dir="$(mktemp -d)"
mixed_fixture="$(mktemp)"
trap 'rm -f "$fixture" "$fixture.bak" "$matching_fixture" "$matching_fixture.bak" "$wrong_fixture" "$wrong_fixture.bak" "$stale_fixture" "$stale_fixture.bak" "$hardlink_fixture" "$hardlink_fixture.bak" "$symlink_fixture" "$symlink_fixture.bak" "$target_symlink_fixture" "$target_symlink_path" "$target_symlink_path.bak" "$fault_fixture" "$fault_fixture.bak" "$mixed_fixture" "$mixed_fixture.bak"; rm -rf "$fault_dir"' EXIT

if [[ ! -f "$client_source" ]]; then
    echo "Test client executable not found: $client_source" >&2
    exit 1
fi

cp -- "$client_source" "$fixture"

bash "$patcher" "$fixture"

first_patch="$(od -An -tx1 -j $((0x1702e0)) -N 4 "$fixture" | tr -d ' \n')"
second_patch="$(od -An -tx1 -j $((0x172395)) -N 4 "$fixture" | tr -d ' \n')"

test "$first_patch" = "90909090"
test "$second_patch" = "90909090"
test -f "$fixture.bak"
if ! cmp -s -- "$client_source" "$fixture.bak" || [[ "$fixture.bak" -ef "$fixture" ]]; then
    echo "Expected a byte-identical independent backup" >&2
    exit 1
fi

bash "$patcher" "$fixture"

cp -- "$client_source" "$matching_fixture"
cp -- "$client_source" "$matching_fixture.bak"
matching_backup_before="$(sha256sum "$matching_fixture.bak" | cut -d' ' -f1)"

if ! bash "$patcher" "$matching_fixture" >/dev/null 2>&1; then
    echo "Expected a matching independent backup to be accepted" >&2
    exit 1
fi

test "$(sha256sum "$matching_fixture" | cut -d' ' -f1)" = "ba3138048e92401eb19552b8f1a7be9eb5be8f2d84e35806fd856ecf392eb40e"
test "$(sha256sum "$matching_fixture.bak" | cut -d' ' -f1)" = "$matching_backup_before"
test ! "$matching_fixture.bak" -ef "$matching_fixture"

if bash "$patcher" "$script_dir/does-not-exist.exe" >/dev/null 2>&1; then
    echo "Expected a missing executable to fail" >&2
    exit 1
fi

truncate -s $((0x172399)) "$wrong_fixture"
printf '\x84\xd2\x74\x6a' | dd of="$wrong_fixture" bs=1 seek=$((0x1702e0)) conv=notrunc status=none
printf '\x84\xd2\x74\x77' | dd of="$wrong_fixture" bs=1 seek=$((0x172395)) conv=notrunc status=none
wrong_fixture_before="$(sha256sum "$wrong_fixture")"

if bash "$patcher" "$wrong_fixture" >/dev/null 2>&1; then
    echo "Expected a wrong-build executable to fail full-file validation" >&2
    exit 1
fi

test "$(sha256sum "$wrong_fixture")" = "$wrong_fixture_before"
test ! -e "$wrong_fixture.bak"

cp -- "$client_source" "$stale_fixture"
printf 'stale backup' >"$stale_fixture.bak"
stale_fixture_before="$(sha256sum "$stale_fixture")"
stale_backup_before="$(sha256sum "$stale_fixture.bak")"

if bash "$patcher" "$stale_fixture" >/dev/null 2>&1; then
    echo "Expected a mismatched existing backup to fail safely" >&2
    exit 1
fi

test "$(sha256sum "$stale_fixture")" = "$stale_fixture_before"
test "$(sha256sum "$stale_fixture.bak")" = "$stale_backup_before"

cp -- "$client_source" "$hardlink_fixture"
ln -- "$hardlink_fixture" "$hardlink_fixture.bak"
hardlink_fixture_before="$(sha256sum "$hardlink_fixture" | cut -d' ' -f1)"

if bash "$patcher" "$hardlink_fixture" >/dev/null 2>&1; then
    echo "Expected a hardlinked backup alias to fail safely" >&2
    exit 1
fi

test "$(sha256sum "$hardlink_fixture" | cut -d' ' -f1)" = "$hardlink_fixture_before"
test "$(sha256sum "$hardlink_fixture.bak" | cut -d' ' -f1)" = "$hardlink_fixture_before"

cp -- "$client_source" "$symlink_fixture"
ln -s -- "$symlink_fixture" "$symlink_fixture.bak"
symlink_fixture_before="$(sha256sum "$symlink_fixture" | cut -d' ' -f1)"

if bash "$patcher" "$symlink_fixture" >/dev/null 2>&1; then
    echo "Expected a symlinked backup alias to fail safely" >&2
    exit 1
fi

test "$(sha256sum "$symlink_fixture" | cut -d' ' -f1)" = "$symlink_fixture_before"
test "$(sha256sum "$symlink_fixture.bak" | cut -d' ' -f1)" = "$symlink_fixture_before"

cp -- "$client_source" "$target_symlink_fixture"
ln -s -- "$target_symlink_fixture" "$target_symlink_path"
target_symlink_before="$(sha256sum "$target_symlink_fixture" | cut -d' ' -f1)"

if bash "$patcher" "$target_symlink_path" >/dev/null 2>&1; then
    echo "Expected a symlinked executable target to fail safely" >&2
    exit 1
fi

test -L "$target_symlink_path"
test "$(sha256sum "$target_symlink_fixture" | cut -d' ' -f1)" = "$target_symlink_before"
test ! -e "$target_symlink_path.bak"

cp -- "$client_source" "$fault_fixture"
fault_fixture_before="$(sha256sum "$fault_fixture" | cut -d' ' -f1)"
real_dd="$(command -v dd)"
cat >"$fault_dir/dd" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
count=0
[[ -f "$DD_COUNTER" ]] && read -r count <"$DD_COUNTER"
count=$((count + 1))
printf '%s\n' "$count" >"$DD_COUNTER"
if [[ $count -eq 2 ]]; then
    exit 1
fi
exec "$REAL_DD" "$@"
EOF
chmod +x "$fault_dir/dd"

if PATH="$fault_dir:$PATH" DD_COUNTER="$fault_dir/count" REAL_DD="$real_dd" \
    bash "$patcher" "$fault_fixture" >/dev/null 2>&1; then
    echo "Expected a second-write failure to leave the executable unchanged" >&2
    exit 1
fi

if [[ "$(sha256sum "$fault_fixture" | cut -d' ' -f1)" != "$fault_fixture_before" ]]; then
    echo "A failed patch left the executable partially modified" >&2
    exit 1
fi
test "$(sha256sum "$fault_fixture.bak" | cut -d' ' -f1)" = "$fault_fixture_before"
fault_temp_count="$(find "$(dirname -- "$fault_fixture")" -maxdepth 1 \
    -name ".$(basename -- "$fault_fixture").patch.*" -print | wc -l)"
if [[ "$fault_temp_count" -ne 0 ]]; then
    echo "A failed patch left a temporary executable copy" >&2
    exit 1
fi

truncate -s $((0x172399)) "$mixed_fixture"
printf '\x84\xd2\x74\x6a' | dd of="$mixed_fixture" bs=1 seek=$((0x1702e0)) conv=notrunc status=none
mixed_fixture_before="$(sha256sum "$mixed_fixture")"

if bash "$patcher" "$mixed_fixture" >/dev/null 2>&1; then
    echo "Expected a mixed-signature executable to fail safely" >&2
    exit 1
fi

test "$(sha256sum "$mixed_fixture")" = "$mixed_fixture_before"
test ! -e "$mixed_fixture.bak"

echo "same-sex couple client patch tests passed"
