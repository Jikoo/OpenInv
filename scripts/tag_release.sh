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

if [[ ! $1 ]]; then
  echo "Please provide a version string."
  return
fi

version="$1"
snapshot="${version%.*}.$((${version##*.} + 1))-SNAPSHOT"

mvn versions:set -DnewVersion="$version"

git add .
git commit -S -m "Bump version to $version for release"
git tag -s "$version" -m "Release $version"

mvn clean package -am -P all

mvn versions:set -DnewVersion="$snapshot"

git add .
git commit -S -m "Bump version to $snapshot for development"
