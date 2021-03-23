### how to use

1. apply to k8s

    ```
    ./gradlew -q :application:maria_db:applyNfsStorageClassAndPvc
    ```
2. apply maria_db

    ```
    ./gradlew -q :application:maria_db:applyK8s
    ```
3. delete one by one

    ```
    ./gradlew -q :application:maria_db:deleteK8s
    ./gradlew -q :application:maria_db:deleteNfsStorageClassAndPvc
    # inaddtional, clean pv
    ```
