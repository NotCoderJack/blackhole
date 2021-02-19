### create_vm_with_virtualbox

* only tested for mac
* but virtualbox is also available for linux
* create vm step by step
    1. install
        1. ```brew install virtualbox```
        2. may have some dependencies to install according to the verbose
    2. download vm ovf
        * ![binary_code_for_vm_ovf.png](binary_code_for_vm_ovf.png)
        * [centos.7.virtualbox.ovf.tar.gz](https://pan.baidu.com/s/1HpCvnnXlKRJrgF7jbmUvvQ) code: vne2
    3. extract
        * ```tar zxvf centos.7.virtualbox.ovf.tar.gz```
        * cd centos.7.virtualbox.ovf
    4. import
        * ```shell
          VBoxManage import centos7.20210219.ovf \
              --vsys 0 \
              --vmname cloud01 \
              --settingsfile $HOME/VirtualBox\ VMs/cloud01/cloud01.vbox \
              --cpus 1 \
              --memory 1024
          VBoxManage import centos7.20210219.ovf \
              --vsys 0 \
              --vmname cloud02 \
              --settingsfile $HOME/VirtualBox\ VMs/cloud02/cloud02.vbox \
              --cpus 1 \
              --memory 1024
          ```
    5. start
        * without headless to login and check the ip adress
        * ```shell
          VBoxManage startvm cloud01
          VBoxManage startvm cloud02
          ```
        * with headless to disable guest GUI
        * ```shell
          VBoxManage startvm cloud01 --type headless
          VBoxManage startvm cloud02 --type headless
          ```
