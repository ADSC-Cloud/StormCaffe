#!/usr/bin/env bash


#### Start here ####
#### The following scripts download and install ZooKeeper and Storm on one cluster node ####


# IPv4 addresses (AWS internally accessible), feel free to add new supervisor nodes' IPs to CHILDNODE_IP
# NIMBUS_IP: internal IPv4 address of nimbus node
# NIMBUS_PDNS: public DNS (IPv4) address of nimbus node. Users need to get it after starting nimbus instances.
# CHILDNODE_IP: internal IPv4 addresses of supervisors

NIMBUS_IP='10.0.0.177'
NIMBUS_PDNS=''
CHILDNODE_IP=( '10.0.0.253' '10.0.0.111' '10.0.0.190' '10.0.0.221' '10.0.0.248' '10.0.0.186' )
echo "The number of supervisors: " ${#CHILDNODE_IP[@]}

# Storm and ZooKeeper binary package URL
STORM_URL='http://www-eu.apache.org/dist/storm/apache-storm-1.0.3/apache-storm-1.0.3.tar.gz'
ZOOKEEPER_URL='http://www-eu.apache.org/dist/zookeeper/zookeeper-3.4.9/zookeeper-3.4.9.tar.gz'

# define the directory of Storm and ZooKeeper
INSTALLER_HOME='/usr/local/TEST/installer'
STORM_HOME='/usr/local/TEST/storm'
ZOOKEEPER_HOME='/usr/local/TEST/zookeeper'

# create installation folders (only if they do not exist) for Storm and ZooKeeper
if [ -z "$(ls -A $INSTALLER_HOME)" ]; then
    mkdir -p $INSTALLER_HOME
else
    echo "Installation files exist!"
#else
#    echo "Installer folder not empty, removing old stuff!"
#    rm -rf $INSTALLER_HOME
#    mkdir -p $INSTALLER_HOME
fi

if [ -z "$(ls -A $STORM_HOME)" ]; then
    mkdir -p $STORM_HOME
else
    echo "Storm exists!"
#else
#    echo "Storm folder not empty, removing old stuff!"
#    rm -rf $STORM_HOME
#    mkdir -p $STORM_HOME
fi

if [ -z "$(ls -A $ZOOKEEPER_HOME)" ]; then
    mkdir -p $ZOOKEEPER_HOME
else
    echo "ZooKeeper exist!"
#else
#    echo "ZooKeeper folder not empty, removing old stuff!"
#    rm -rf $ZOOKEEPER_HOME
#    mkdir -p $ZOOKEEPER_HOME
fi

# download Storm and ZooKeeper binary package
echo "Installation files downloading..."
wget -O $INSTALLER_HOME/storm-1.0.3.tar.gz $STORM_URL
wget -O $INSTALLER_HOME/zookeeper-3.4.9.tar.gz $ZOOKEEPER_URL
echo "Installation files downloading finished!"

# extract files
echo "Unzipping..."
tar -xzf $INSTALLER_HOME/storm-1.0.3.tar.gz -C $STORM_HOME
tar -xzf $INSTALLER_HOME/zookeeper-3.4.9.tar.gz -C $ZOOKEEPER_HOME
echo "Unzipping finished!"

# add john as the owner of all files
chown -R john:john $INSTALLER_HOME $STORM_HOME $ZOOKEEPER_HOME


#### End here ####




#### Start here ####
#### The following scripts download (if necessary) and install dependencies (Java, JavaCPP, JavaCPP-Presets, CUDA 8 etc.)


#### End here ####