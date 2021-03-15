# blackhole

吸收优秀基础服务成为大数据世界的起点

### how to setup project(before import it to IntelliJ IDEA)

```shell
bash setup_project.sh
```

### applications
1. [army_knife](application/army_knife): auto-setup base components, such as 
    * [docker engine](application/army_knife/src/test/java/tech/geekcity/blackhole/application/army/knife/install/DockerEngineInstallerTest.java)
    * [kubernetes](application/army_knife/src/test/java/tech/geekcity/blackhole/application/army/knife/install/K8sInstallerTest.java)
    * glusterFS
    * NFS
2. [sshd service](application/ssh): act as a springboard machine
3. [nfs service](application/nfs): act as distributed storage
4. [nfs storage provisioner](application/nfs_storage_provision): used to provide k8s volume automatically
5. [maria db](application/maria_db): mysql service
6. postgres
7. rabbit mq
8. redis
9. zookeeper
10. etcd
11. kafka
12. flink
13. hdfs
14. hbase

### libs
1. [core](lib/core): base interfaces
2. [docker](lib/docker): a lib to access local docker engine
3. [render](lib/render): render template engine
4. [ssh](lib/ssh): ssh utils including ssh_commander and simple_scp
5. [agent](lib/agent): an agent based on grpc(developing...)
