#!/bin/bash
set -e

BOX_ID="${BOX_ID:-64}"
PROC_CG="init"
ISOLATE_CG="isolate"

mkdir -p "/sys/fs/cgroup/${PROC_CG}" "/sys/fs/cgroup/${ISOLATE_CG}"

for pid in $(cat /sys/fs/cgroup/cgroup.procs); do
    echo "$pid" > "/sys/fs/cgroup/${PROC_CG}/cgroup.procs" 2>/dev/null || true
done

echo '+cpu +memory +pids' > /sys/fs/cgroup/cgroup.subtree_control
echo '+cpu +memory +pids' > "/sys/fs/cgroup/${ISOLATE_CG}/cgroup.subtree_control"

mkdir -p /run/isolate/locks

if grep -q '^cg_root' /etc/isolate; then
    sed -i "s|^cg_root.*|cg_root = /sys/fs/cgroup/${ISOLATE_CG}|" /etc/isolate
else
    echo "cg_root = /sys/fs/cgroup/${ISOLATE_CG}" >> /etc/isolate
fi

## add -DLOG_FORMAT=ASYNC_JSON in java -jar... if needed
exec java -Xms32m -Xmx48m -XX:MaxMetaspaceSize=64m -XX:+UseG1GC -jar app.jar