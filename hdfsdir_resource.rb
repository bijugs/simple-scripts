#
#  Chef LWRP resource definition for hdfs directory
#
#  Author: Biju Nair
#  Github: https://github.com/bijugs
#
#  License
#  =======
#
#  [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0)
#
#  Unless required by applicable law or agreed to in writing, software distributed
#  under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
#  CONDITIONS OF ANY KIND, either expressed or implied. See the license for the specific
#  language governing permissions and limitations under the license.
#
actions :create, :delete, :chown, :chmod, :rename, :chgrp
default_action :create
#
# fqdn or ip address of the name node server
#
attribute :namenode, :kind_of => String, :required => true
#
# port number of the namenode
#
attribute :nnport, :kind_of => String, :required => true
#
# Directory path on which actions need to be taken
#
attribute :path, :kind_of => String, :name_attribute => true, :required => true
#
# User id to connect to HDFS. 
# If none provided the id of the current user will be used
#
attribute :user, :kind_of => String, :required => false
#
# access rights of users and groups to be set during create and chmod
# Note it is a string
#
attribute :mode, :kind_of => String, :required => false
#
# user id to which the owner ship of the directory to be changed to
#
attribute :tuser, :kind_of => String, :required => false
#
# group to which the directory belongs adter chown or chgrp
#
attribute :tgroup, :kind_of => String, :required => false
#
# target directory (path) name for rename action
#
attribute :tpath, :kind_of => String, :required => false
