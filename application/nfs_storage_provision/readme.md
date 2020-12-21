### how to use

1. start the nfs service first

    ```
    ./gradlew -q :application:nfs:applyK8s
    ```
2. apply rbac settings

    ```
    ./gradlew -q :application:nfs_storage_provision:applyRbac
    ```
3. apply nfs storage provisioner

    ```
    ./gradlew -q :application:nfs_storage_provision:applyNfsStorageProvisioner
    ```
4. apply pvc

    ```
    ./gradlew -q :application:nfs_storage_provision:applyNfsStorageClassAndPvc
    ```
5. apply centos to test

    ```
    ./gradlew -q :application:nfs_storage_provision:applyCentos
    ```

### how to delete them one by one

```
./gradlew -q :application:nfs_storage_provision:deleteCentos
./gradlew -q :application:nfs_storage_provision:deleteNfsStorageClassAndPvc
./gradlew -q :application:nfs_storage_provision:deleteRbac
./gradlew -q :application:nfs:deleteK8s
```