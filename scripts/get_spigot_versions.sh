#!/bin/bash
#
# Copyright (C) 2011-2022 lishid. All rights reserved.
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

# Use a nameref as a cache - maven evaluation is pretty slow.
# Re-calling the script and relying on it to handle caching is way easier than passing around info.
declare -a spigot_versions

# We don't care about concatenation - either it's not null and we handle entries or it's null and we instantiate.
# shellcheck disable=SC2199
if [[ ${spigot_versions[@]} ]]; then
  for spigot_version in "${spigot_versions[@]}"; do
    echo "$spigot_version"
  done
  return
fi

old_maven_opts=$MAVEN_OPTS
# Add JVM parameters to allow help plugin access to packages it needs.
export MAVEN_OPTS="$old_maven_opts --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED"

# Pull Spigot dependency information from Maven.
# Since we only care about Spigot versions, only check modules in the folder internal.
readarray -t modules <<< "$(mvn help:evaluate -Dexpression=project.modules -q -DforceStdout -P all | grep -oP '(?<=<string>)(internal/.*)(?=</string>)')"

declare -n versions="spigot_versions"

for module in "${modules[@]}"; do
  # Get Spigot version.
  spigot_version=$(mvn help:evaluate -Dexpression=spigot.version -q -DforceStdout -P all -pl "$module")
  versions+=("$spigot_version")
  echo "$spigot_version"
done

# Reset JVM parameters
export MAVEN_OPTS=$old_maven_opts