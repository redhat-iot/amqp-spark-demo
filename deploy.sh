#!/bin/bash

# startup OpenShift Origin cluster
oc cluster up

# create an "oshinko" ServiceAccount, used by Oshinko to write to the OpenShift API.
echo '{"apiVersion": "v1", "kind": "ServiceAccount", "metadata": {"name": "oshinko"}}' | oc create -f -

# authorize the "oshinko" ServiceAccount to write to the OpenShift API.
oc policy add-role-to-user admin -z oshinko

# create the Oshinko template.
curl https://raw.githubusercontent.com/radanalyticsio/oshinko-rest/master/tools/server-ui-template.yaml | oc create -f -

# launch Oshinko in the current project.
oc new-app oshinko

# deploy an Apache Artemis instance into the same cluster
curl https://raw.githubusercontent.com/redhat-iot/amqp-spark-demo/master/cluster/artemis-rc.yaml | oc create -f -
