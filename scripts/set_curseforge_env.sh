#!/bin/bash
#
# Copyright (C) 2011-2021 lishid. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#

# Note that this script is designed for use in GitHub Actions, and is not
# particularly robust nor configurable. Run from project parent directory.

# Parse Spigot dependency information into major Minecraft versions
function get_curseforge_minecraft_versions() {
  readarray -t versions <<< "$(. ./scripts/get_spigot_versions.sh)"

  for version in "${versions[@]}"; do
    # Parse Minecraft major version
    version="${version%[.-]"${version#*.*[.-]}"}"

    # Skip already listed versions
    if [[ "$minecraft_versions" =~ "$version"($|,) ]]; then
      continue
    fi

    # Append comma if variable is set, then append version
    minecraft_versions="${minecraft_versions:+${minecraft_versions},}Minecraft ${version}"
  done

  echo "${minecraft_versions}"
}

# Modify provided changelog to not break when inserted into yaml file.
function get_yaml_safe_changelog() {
  changelog=$1
  # Since we're using a flow scalar, newlines need to be doubled.
  echo "${changelog//
/

}"
}

minecraft_versions=$(get_curseforge_minecraft_versions)
echo "CURSEFORGE_MINECRAFT_VERSIONS=$minecraft_versions" >> "$GITHUB_ENV"

changelog=$(get_yaml_safe_changelog "$1")
printf "CURSEFORGE_CHANGELOG<<EOF\n%s\nEOF\n" "$changelog" >> "$GITHUB_ENV"