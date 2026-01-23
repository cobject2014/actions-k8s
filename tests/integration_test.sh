#!/bin/bash
set -e

NAMESPACE="ci-test"
ECHO_POD_NAME="echo-server"

echo "Waiting for Echo Server to be ready..."
kubectl wait --for=condition=Ready pod/$ECHO_POD_NAME -n $NAMESPACE --timeout=120s

echo "Port forwarding Echo Server..."
# Run port-forward in background
kubectl port-forward pod/$ECHO_POD_NAME 8086:8086 -n $NAMESPACE &
PF_PID=$!
sleep 5

echo "Test 1: Send Request"
TEST_DATA="CI_TEST_DATA_$(date +%s)"
RESPONSE=$(curl -s -X POST -d "$TEST_DATA" http://localhost:8086)
echo "Response: $RESPONSE"

if [[ "$RESPONSE" == "$TEST_DATA" ]]; then
    echo "PASS: Echo response matches."
else
    echo "FAIL: Echo response mismatch. Expected '$TEST_DATA', got '$RESPONSE'"
    kill $PF_PID
    exit 1
fi

echo "Test 2: Check Redis"
# Redis Auth is disabled in CI
# REDIS_PASSWORD=$(kubectl get secret --namespace $NAMESPACE redis-test -o jsonpath="{.data.redis-password}" | base64 -d)

# Find redis pod
REDIS_POD=$(kubectl get pod -n $NAMESPACE -l app.kubernetes.io/name=redis,app.kubernetes.io/component=master -o jsonpath="{.items[0].metadata.name}")

echo "Checking Redis Pod: $REDIS_POD"

# Check if key exists and print it
LAST_ACCESS=$(kubectl exec -n $NAMESPACE $REDIS_POD -- redis-cli get lastAccessTime)
echo "Redis lastAccessTime: $LAST_ACCESS"

if [[ -n "$LAST_ACCESS" ]]; then
    echo "PASS: Redis key exists."
else
    echo "FAIL: Redis key missing."
    kill $PF_PID
    exit 1
fi

kill $PF_PID
echo "All tests passed."
