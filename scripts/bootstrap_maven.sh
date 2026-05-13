#!/usr/bin/env bash
set -euo pipefail

version="${1:-3.9.9}"
target_dir="${2:-}"

if [[ -z "$target_dir" ]]; then
  echo "Usage: $0 <maven_version> <target_dir>" >&2
  exit 1
fi

mvn_bin="$target_dir/bin/mvn"
if [[ -x "$mvn_bin" ]]; then
  exit 0
fi

archive="apache-maven-${version}-bin.tar.gz"
base_url="https://archive.apache.org/dist/maven/maven-3/${version}/binaries/${archive}"
parent_dir="$(dirname "$target_dir")"

mkdir -p "$parent_dir"
tmp_archive="$(mktemp)"
trap 'rm -f "$tmp_archive"' EXIT

curl -fsSL "$base_url" -o "$tmp_archive"
tar -xzf "$tmp_archive" -C "$parent_dir"

echo "Maven ${version} installed at ${target_dir}"
