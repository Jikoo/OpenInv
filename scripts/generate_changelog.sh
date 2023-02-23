#!/bin/bash
#
# Copyright (C) 2011-2023 lishid. All rights reserved.
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

# Query GitHub for the username of the given email address.
# Falls through to the given author name.
function lookup_email_username() {
  lookup=$(curl -G --data-urlencode "q=$1 in:email" https://api.github.com/search/users -H 'Accept: application/vnd.github.v3+json' | grep '"login":' | sed -e 's/^.*": "//g' -e 's/",.*$//g')

  if [[ $lookup ]]; then
    echo -n "@$lookup"
  else
    echo "$2"
  fi
}

# Get a pretty list of supported Minecraft versions
function get_minecraft_versions() {
  readarray -t versions <<< "$(. ./scripts/get_spigot_versions.sh)"

  for version in "${versions[@]}"; do
    # Append comma if variable is set, then append version
    minecraft_versions="${minecraft_versions:+${minecraft_versions}, }${version%%-R*}"
  done

  echo "${minecraft_versions}"
}

previous_tag=$(git describe --tags --abbrev=0 @^)

# Use formatted log to pull authors list
authors_raw=$(git log --pretty=format:"%ae|%an" "$previous_tag"..@)
readarray -t authors <<<"$authors_raw"

# Use associative array to map email to author name
declare -A author_data

for author in "${authors[@]}"; do
  # Match author email
  author_email=${author%|*}
  # Convert to lower case
  author_email=${author_email,,}
  # Match author name
  author_name=${author##*|}
  if [[ -n ${author_data[$author_email]} ]]; then
    # Skip emails we already have data for
    continue
  fi

  # Fetch and store author GitHub username by email
  author_data[$author_email]=$(lookup_email_username "$author_email" "$author_name")
done

# Fetch actual formatted changelog
changelog=$(git log --pretty=format:"* %s (%h) - %ae" "$previous_tag"..@)

for author_email in "${!author_data[@]}"; do
  # Ignore case when matching
  shopt -s nocasematch
  # Match and replace email
  changelog=${changelog//$author_email/${author_data[$author_email]}}
done

minecraft_versions=$(get_minecraft_versions)

printf "## Supported Minecraft versions\n%s\n\n## Changelog\n%s" "${minecraft_versions}" "${changelog}"
