#
#  Chef LWRP provider for hdfs directory resource
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
require 'chef/log'
require 'webhdfs'
#
# This method is required for LWRP so that any notifications from 
# resources in the LWRP will be considered from LWRP resource collection
# and not from the individual resource which is notifying.
#
use_inline_resources
#
# To enable -W/--why-run option of chef-client
#
def whyrun_supported?
   true
end
#
# Method automatically called by Chef during the client execution phase
# Can be used to initialize variables and also verify the current state
#
def load_current_resource
  #
  # Not taking the typical approach of creating a current_resource will make
  # this LWRP usable with any cookbook without changes. We are creating 
  # instance variables for each attribute which would be available in the
  # @current_resource object if we had created one.
  #
  #-- @current_resource = Chef::Resource::BcpcHdfsdir.new(new_resource.name)
  #-- new_resource.user == nil ? @current_resource.user = ENV['USER'] : @current_resource.user = new_resource.user
  new_resource.user == nil ? @user = ENV['USER'] : @user = new_resource.user
  nnaddress = new_resource.namenode
  nnport = new_resource.nnport
  @client = WebHDFS::Client.new(nnaddress,nnport,@user)
  if (!validnn?())
    raise RuntimeError, "Invalid namenode provided or HDFS not available"
  end
  @omode = new_resource.mode
  new_resource.mode == nil ? @mode = "0750" : @mode = new_resource.mode
  @path = new_resource.path
  @tpath = new_resource.tpath
  @tgroup = new_resource.tgroup
  @tuser = new_resource.tuser
end
#
# Method to check whether the namenode provided is valid
#
def validnn?()
  return dir_exists?("/") ? true : false
end
#
# Method to check whether the dir currently exists
#
def dir_exists?(path)
  begin
    @meta_data = @client.stat(path)
    if (@meta_data["type"] == "DIRECTORY")
      return true
    else
      return false
    end
  rescue
    return false
  end 
end
#
# Action to create a directory in HDFS
#
action :create do
  if (dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } exits; create action not taken")
  else
    converge_by("Create #{ @new_resource }") do
      @client.mkdir(@path,'permission' => @mode)
    end
    new_resource.updated_by_last_action(true)
  end
end
#
# Action to delete a directory in HDFS
#
action :delete do
  if (!dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } doesn't exist; delete action not taken")
  else
    converge_by("Delete #{ @new_resource }") do
     @client.delete(@path)
    end
    new_resource.updated_by_last_action(true)
  end
end
#
# Action to change owner:group of a directory in HDFS
#
action :chown do
  if @tuser == nil
    Chef::Log::fatal "target user need to be provided to perform chown"
  elsif (!dir_exists?(@path))
    Chef::Log::Error("Directory #{ @path } doesn't exist; chown action not taken")
  else
    if @tgroup == nil
      @tgroup = @meta_data["group"]
    end
    converge_by("chown #{ @new_resource }") do
     @client.chown(@path, 'owner' => @tuser, 'group' => @tgroup)
    end
    new_resource.updated_by_last_action(true)
  end
end
#
# Action to change mode of a directory in HDFS
#
action :chmod do
  if @omode == nil
    Chef::Log::fatal "target mode need to be provided to perform chmod"
  elsif (!dir_exists?(@path))
    Chef::Log::Error("Directory #{ @path } doesn't exist; chmod action not taken")
  else
    converge_by("chmod #{ @new_resource }") do
     @client.chmod(@path, @mode)
    end
    new_resource.updated_by_last_action(true)
  end
end
#
# Action to change group of a directory in HDFS
#
action :chgrp do
  if @tgroup == nil
    Chef::Log::fatal "target group need to be provided to perform chgrp"
  elsif (!dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } doesn't exist; chmod action not taken")
  else
    converge_by("chgrp #{ @new_resource }") do
     @client.chown(@path, 'group' => @tgroup)
    end
    new_resource.updated_by_last_action(true)
  end
end
#
# Action to rename a directory in HDFS
#
action :rename do
  if @tpath == nil
    Chef::Log.Fatal "Target path is empty and need to be set for rename action"
  elsif (!dir_exists?(@path))
    Chef::Log::Error("Source directory #{ @path } doesn't exist; rename action not taken")
  else
    converge_by("rename #{ @new_resource }") do
      @client.rename(@path, @tpath)
    end
    new_resource.updated_by_last_action(true)
  end
end
