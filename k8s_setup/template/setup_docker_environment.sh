#! /bin/bash

set -x
set -e
set -o pipefail

SCRIPT_PATH="${__DOLLAR__}( cd "${__DOLLAR__}( dirname "${__DOLLAR__}{BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

yum install -y yum-utils device-mapper-persistent-data lvm2 \
    && yum-config-manager --add-repo ${(dockerCeRepo)!"https://download.docker.com/linux/centos/docker-ce.repo"} \
    && yum -y install docker-ce \
    && systemctl enable docker \
    && systemctl start docker