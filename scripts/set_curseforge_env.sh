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

if [[ ! $1 ]]; then
  echo "No changelog, no Minecraft versions."
  exit 0
fi

# Find line declaring Paper versions.
raw=$(grep "**Paper:**" <<< "$1")

# Enable extended glob pattern to match 0 or more whitespace characters.
shopt -s extglob
# Trim Paper versions identifier prefix.
raw=${raw##*([[:space:]])'**'Paper:'**'*([[:space:]])}
# Replace commas and optional spaces with a newline.
raw=${raw//,*([[:space:]])/$'\n'}
# Turn extglob back off.
shopt -u extglob

# Split into an array on newlines.
readarray -td $'\n' versions <<< "${raw}"

for version in "${versions[@]}"; do
  # Parse Minecraft minor version by dropping everything from the second period onward.
  # CurseForge doesn't usually add patch versions for Bukkit, so we're more likely to
  # hit a supported identifier this way.
  version="${version%[.-]"${version#*.*[.-]}"}"

  # Skip already listed versions
  if [[ "$minecraft_versions" =~ "$version"($|,) ]]; then
    continue
  fi

  # Append comma if variable is set, then append version.
  # Note that Minecraft versions on CurseForge are declared "Minecraft x.y.z"
  minecraft_versions="${minecraft_versions:+${minecraft_versions},}Minecraft ${version}"
done

printf "$minecraft_versions\n"
#echo "CURSEFORGE_MINECRAFT_VERSIONS=$minecraft_versions" >> "$GITHUB_ENV"
