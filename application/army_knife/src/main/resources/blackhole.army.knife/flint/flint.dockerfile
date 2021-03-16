FROM centos:centos7.8.2003
ENV TZ=Asia/Shanghai
RUN rm -rf /etc/yum.repos.d/* \
    && for i in $(ls /lib/systemd/system/sysinit.target.wants/); \
        do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; \
    done \
    && rm -f /lib/systemd/system/multi-user.target.wants/* \
    && rm -f /etc/systemd/system/*.wants/* \
    && rm -f /lib/systemd/system/local-fs.target.wants/* \
    && rm -f /lib/systemd/system/sockets.target.wants/*udev* \
    && rm -f /lib/systemd/system/sockets.target.wants/*initctl* \
    && rm -f /lib/systemd/system/basic.target.wants/* \
    && rm -f /lib/systemd/system/anaconda.target.wants/* \
    && rm -rf /etc/yum.repos.d/* \
    && curl -o /etc/yum.repos.d/CentOS-Base.repo https://mirrors.aliyun.com/repo/Centos-7.repo
ADD centos.7.aliyun.repo /etc/yum.repos.d/centos.7.aliyun.repo
ADD kubernetes.aliyun.repo /etc/yum.repos.d/kubernetes.aliyun.repo
RUN set -x \
    && yum install -y kubectl java-1.8.0-openjdk-devel --disableexcludes=kubernetes
ENV JAVA_HOME=/usr/lib/jvm/java