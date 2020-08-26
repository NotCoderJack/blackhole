ARG BASE_IMAGE=blackhole_base
FROM $BASE_IMAGE
RUN set -x \
    && yum -y install openssh-server \
    && ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_rsa_key -N "" \
    && mkdir -p /root/.ssh \
    && chmod 700 /root/.ssh \
    && touch $HOME/.ssh/authorized_keys \
    && chmod 600 $HOME/.ssh/authorized_keys
CMD ["/usr/sbin/sshd", "-D", "-E", "/tmp/sshd.log"]
