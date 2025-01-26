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

# Get a pretty string of the project's name and version
# Disable SC warning about variable expansion for this function - those are Maven variables.
# shellcheck disable=SC2016
function get_versioned_name() {
  mvn -q -Dexec.executable=echo -Dexec.args='${project.name} ${project.version}' --non-recursive exec:exec
}

# Set GitHub environmental variables
echo "VERSIONED_NAME=$(get_versioned_name)" >> "$GITHUB_ENV"

changelog="$(. ./scripts/generate_changelog.sh)"
printf "GENERATED_CHANGELOG<<EOF\n%s\nEOF\n" "$changelog" >> "$GITHUB_ENV"
