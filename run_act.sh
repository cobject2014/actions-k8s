act -j integration-test \
  -s LOCAL_KUBE_CONFIG="$(base64 < ~/.kube/config)" \
  --container-options "-v $HOME/.m2:/root/.m2"
