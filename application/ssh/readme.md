### how to use

1. build the docker image
    * ```./gradlew -q :application:ssh:buildImage```
2. apply to k8s
    * ```./gradlew -q :application:ssh:applyK8s```
    * will print the connect command
3. connect to the sshd (connect to the springboard machine)
    * running the command print at previous step
    * for example

    ```
    ssh -i /Users/ben.wangz/develop/blackhole/build/keys/id_rsa -p 30022 -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null root@localhost
   ```