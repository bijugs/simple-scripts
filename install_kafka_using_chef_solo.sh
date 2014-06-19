#
#  Script to install a single node Kafka instance using chef-solo
#  This is quick hack for development VM set-up and does its job but not robust enough.
#  Assumes that the install is on a fresh node with OS installed and nothing on it including JAVA
#  Tested on Ubuntu and can be modified for other flavors of Linux
#
# set -x

#
# Install curl if not already installed 
#
which curl >/dev/null 2>&1
if  [ $? != 0 ]; then
  apt-get install curl >/dev/null 2>&1
fi

#
# Install wget if not already installed 
#
which wget >/dev/null 2>&1
if  [ $? != 0 ]; then
  apt-get install wget >/dev/null 2>&1
fi

#
# Install git if not already installed 
#
which git >/dev/null 2>&1
if  [ $? != 0 ]; then
  apt-get install git >/dev/null 2>&1
fi

#
# Install chef if not already installed 
#
dpkg -l chef >/dev/null 2>&1
if  [ $? != 0 ]; then
  curl -L https://www.opscode.com/chef/install.sh | bash >/dev/null 2>&1
fi

#
#  If chef repo directory is not present perform steps to install Kafka
#  To re-run the script need to delete the chef-repo directory
#  Its a quick hack assuming that this is a single user machine using it for dev  
#
if [ ! -d chef-repo ]; then
  par=$(pwd)
  #
  # Install the chef starter repo
  #
  wget http://github.com/opscode/chef-repo/tarball/master >/dev/null 2>&1
  tar -zxf master >/dev/null 2>&1
  mv opscode-chef-repo* chef-repo
  rm master
  #
  # Create knife.rb file with the required parameters to run chef-solo
  #
  cd chef-repo
  mkdir .chef
  echo "cookbook_path [ '$(pwd)/cookbooks' ]" > .chef/knife.rb
  #
  # Load the required cookbooks to install Kafka
  #
  cd cookbooks
  git clone https://github.com/socrata-cookbooks/java.git >/dev/null 2>&1
  git clone https://github.com/bijugs/kafka-cookbook.git -b kafka_enhancement kafka >/dev/null 2>&1
  #
  # Change the zookeeper data directory from /tmp to /opt/kafka
  #
  sed -i 's/\/tmp/\/opt\/kafka/g' ./kafka/attributes/zookeeper.rb
  #
  # Change the port on which broker listens to 9092 which is used by default in kafka standard install 
  # For some reason the community cookbook changes to 6667 in server.properties but not on producer.properties
  #
  sed -i 's/6667/9092/g' ./kafka/attributes/default.rb
  cd ..
  #
  # Create the chef solo config file solo.rb
  #
  echo file_cache_path \"$par/chef-solo\" > solo.rb
  echo cookbook_path \"$par/chef-repo/cookbooks\" >> solo.rb
  #
  # Create the runlist and attribute overwrites to run on the node
  #
  echo { \
    \"java\": { \
        \"jdk_version\" : \"7\", \
        \"accept_license_agreement\": true, \
        \"oracle\": { \
          \"accept_oracle_download_terms\": true \
        }  \
     }, \
     \"run_list\": [ \
       \"recipe[java::oracle]\", \
       \"recipe[kafka::zookeeper]\", \
       \"recipe[kafka]\" \
     ] \
   } > kafka.json
  #
  # Run chef solo to perform the java and kafka installation
  #
  sudo chef-solo -c solo.rb -j kafka.json
fi
