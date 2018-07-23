#!/bin/bash
set -e

# oc new-project eap-transactions --display-name="JBoss EAP Transactional EJB"
# oc import-image jboss-eap-64 --from=registry.access.redhat.com/jboss-eap-6/eap64-openshift --confirm
# git add -u && git commit -m "config" && git push origin master

oc delete is,bc,dc,service tx-server 
oc new-app jboss-eap-64~https://github.com/mmusgrov/openshift-tx.git#eap64 --context-dir='tx-server' --name='tx-server' --labels name='tx-server'

# edit the dc and service yaml to add port 4447 to the ports section of the container specification:
# oc edit dc/tx-server && oc edit svc/tx-server
# oc expose service tx-server && oc rollout latest tx-server
# oc expose svc/tx-server && oc rollout latest tx-server


# oc build-logs tx-client-1
# oc rsh `oc get pods -n tx-client | grep Running | awk '{print $1}'`
