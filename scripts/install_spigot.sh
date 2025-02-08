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

buildtools_dir=~/buildtools
buildtools=$buildtools_dir/BuildTools.jar

get_buildtools () {
  if [[ -d $buildtools_dir && -f $buildtools ]]; then
    return
  fi

  mkdir $buildtools_dir
  wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O $buildtools
}

if [[ ! $1 ]]; then
  echo "Please specify Spigot version to install."
  exit 1
fi

get_buildtools

pushd $buildtools_dir
java -jar $buildtools -rev $1 --remapped
popd
