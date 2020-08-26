ARG BASE_IMAGE=blackhole_base
FROM $BASE_IMAGE
RUN set -x \
    && yum -y install openssh-server \
    && ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_rsa_key -N "" \
    && mkdir -p /root/.ssh \
    && chmod 700 /root/.ssh \
    && echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDnAr3ww7XEMJuflI4qDeXAp4nU+VYNNbYwOGTkF6Cvb9Klvk5ZhLz7LLGx5aewWRxq3V0bMkLpgDMhZRcXaGt8X+WCwI2jKWqN0YdsJQZZdF4ZePXb3Td90yjd8lRaJUL3s7lZ4d65SWtDRltH9NbevCBy2FG5gQtfvK4e8OjCkR5yUqKotwFXkx+bQYJzZejSsGc2UZ8NivEp+d8NQL4i7W/3kxI2x9sHlR7CJRxFgCO3IJnzc40udiZasXPtQBciT8HJVvcuTDFHta/kgwas5EbqkZOzkPcFLXf74qUr/cEDI84c5Qlmd3D2PyWaVsE0cXKtUOM4zQbfuPh8j25F root@7a61d7d382df" >> $HOME/.ssh/authorized_keys \
    && chmod 600 $HOME/.ssh/authorized_keys
CMD ["/usr/sbin/sshd", "-D", "-E", "/tmp/sshd.log"]
