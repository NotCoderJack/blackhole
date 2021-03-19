### create_vm_with_virtualbox

* only tested for ubuntu

1. install virtualbox: https://www.virtualbox.org/wiki/Linux_Downloads
2. download vm ovf
    * ![binary_code_for_vm_ovf.png](images/binary_code_for_vm_ovf.baidu.png)
    * [centos.7.virtualbox.ovf.tar.gz at pan.baidu.com code(hanl)](https://pan.baidu.com/s/1ETXjQ2oE30i3sb63oMWMpw)
    * ![binary_code_for_vm_ovf.png](images/binary_code_for_vm_ovf.xunlei.png)
    * [centos.7.virtualbox.ovf.tar.gz at pan.xunlei.com code(7emj)](https://pan.xunlei.com/s/VMW8PHxOeDC90M2JFNSamXceA1)
3. extract(into ~/packages for example)
    * ```tar jxvf centos-base.tar.bz2 -C ~/packages```
4. import
    * ```shell
      VBoxManage import ~/packages/centos-base/centos-base.20210319.ovf \
          --vsys 0 \
          --vmname master-k8s \
          --settingsfile "$HOME/vms/master-k8s/master-k8s.vbox" \
          --cpus 2 \
          --memory 2048
      VBoxManage import ~/packages/centos-base/centos-base.20210319.ovf \
          --vsys 0 \
          --vmname worker1-k8s \
          --settingsfile "$HOME/vms/worker1-k8s/worker1-k8s.vbox" \
          --cpus 2 \
          --memory 2048
      VBoxManage import ~/packages/centos-base/centos-base.20210319.ovf \
          --vsys 0 \
          --vmname worker2-k8s \
          --settingsfile "$HOME/vms/worker2-k8s/worker2-k8s.vbox" \
          --cpus 2 \
          --memory 2048
      # optional check vms
      VBoxManage list vms
      ```
5. bind bridgeadapter1 with the interface in your host
    * ```shell
      # find your interface to bridge
      # linux (usually be enp42s0 for ubuntu)
      ip addr
      # macos
      #ifconfig
      ```
    * ```shell
      # for example enp42s0
      VBoxManage modifyvm master-k8s --bridgeadapter1 enp42s0
      VBoxManage modifyvm worker1-k8s --bridgeadapter1 enp42s0
      VBoxManage modifyvm worker2-k8s --bridgeadapter1 enp42s0
      ```
6. start
    * ```shell
      VBoxManage startvm master-k8s --type headless
      VBoxManage startvm worker1-k8s --type headless
      VBoxManage startvm worker2-k8s --type headless
      ```
7. stop
    * ```shell
      VBoxManage controlvm master-k8s acpipowerbutton
      VBoxManage controlvm worker1-k8s acpipowerbutton
      VBoxManage controlvm worker2-k8s acpipowerbutton
      ```
8. DO EXTERNAL: bind ip and hostname with your router, then you can use this command to set hostname
   * ```shell
      # the default password is '123456' only for test
      ssh root@master-k8s hostnamectl set-hostname master-k8s
      ssh root@worker1-k8s hostnamectl set-hostname worker1-k8s
      ssh root@worker2-k8s hostnamectl set-hostname worker2-k8s
      ```
9. take a snapshot(for example: base)
    * ```shell
      VBoxManage snapshot master-k8s take base
      VBoxManage snapshot worker1-k8s take base
      VBoxManage snapshot worker2-k8s take base
      # list snapshots of a vm
      VBoxManage snapshot master-k8s list
      VBoxManage snapshot worker1-k8s list
      VBoxManage snapshot worker2-k8s list
      ```
10. shutdown and restore a snapshot(for example: base)
    * ```shell
      VBoxManage controlvm master-k8s acpipowerbutton
      VBoxManage controlvm worker1-k8s acpipowerbutton
      VBoxManage controlvm worker2-k8s acpipowerbutton
      # we need to wait some time to let vms to stop
      sleep 30
      # check state
      vboxmanage showvminfo master-k8s | grep State
      vboxmanage showvminfo worker1-k8s | grep State
      vboxmanage showvminfo worker2-k8s | grep State
      # restore
      VBoxManage snapshot master-k8s restore base
      VBoxManage snapshot worker1-k8s restore base
      VBoxManage snapshot worker2-k8s restore base
      ```
