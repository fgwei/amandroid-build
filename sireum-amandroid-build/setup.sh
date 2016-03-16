#!/bin/bash
ips=( core parser prelude );
for i in "${ips[@]}"; do
  if [ -e codebase/${i} ] ; then
    echo "Pulling ${i}"
    cd codebase/${i}
    git pull
    cd ../..
  else
    echo "Cloning ${i}"
    git clone https://github.com/sireum/${i}.git codebase/${i}
  fi
done
ips=( amandroid jawa );
for i in "${ips[@]}"; do
  if [ -e codebase/${i} ] ; then
    echo "Pulling ${i}"
    cd codebase/${i}
    git pull
    cd ../..
  else
    echo "Cloning ${i}"
    git clone https://github.com/sireum/${i}.git codebase/${i}
  fi
  cd codebase/${i}
  git checkout develop
  cd ../..
done