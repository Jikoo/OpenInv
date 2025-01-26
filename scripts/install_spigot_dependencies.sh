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

buildtools_dir=~/buildtools
buildtools=$buildtools_dir/BuildTools.jar

get_buildtools () {
  if [[ -d $buildtools_dir && -f $buildtools ]]; then
    return
  fi

  mkdir $buildtools_dir
  wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O $buildtools
}

readarray -t versions <<< "$(. ./scripts/get_spigot_versions.sh)"
echo Found Spigot dependencies: "${versions[@]}"

# Install dependencies aside from Spigot prior to running in offline mode.
mvn dependency:go-offline -DexcludeArtifactIds=spigot

for version in "${versions[@]}"; do
  set -e
  exit_code=0
  mvn dependency:get -Dartifact=org.spigotmc:spigot:"$version":remapped-mojang -q -o || exit_code=$?
  if [ $exit_code -ne 0 ]; then
    echo Installing missing Spigot version "$version"
    revision=${version%%-R*}
    get_buildtools
    java -jar $buildtools -rev "$revision" --remapped
  else
    echo Spigot "$version" is already installed
  fi
done
