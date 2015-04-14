# simple-scripts


The following is a brief description of files in the repo

- ``hbase-bucketcache.py`` - python script to calculate HBase configuration parameter values when configuring offheap bucketcache.
- ``hdfsdir_resource.rb`` - Chef LWRP resource definition for HDFS directory. See next item for the corresponding provider.
- ``hdfsdir_provider.rb`` - Chef LWRP provider for HDFS directory which can be used with the resource definition above.
- ``install_kafka_using_chef_solo.sh`` - Script to install a single node ``Kafka`` using ``Chef Solo``.


License
=======

[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed 
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
CONDITIONS OF ANY KIND, either expressed or implied. See the license for the specific 
language governing permissions and limitations under the license.
