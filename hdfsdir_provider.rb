#
# LWRP provider for hdfs directory resource
#
require 'chef/log'
require 'webhdfs'
#
# To enable -W/--why-run option of chef-client
#
def whyrun_supported?
   true
end
#
# Method automatically called by Chef during the client execution phase
# Can be used to initialize variables and also verify the current state
# Need to be updated to dybnamically pass the name node IP and port
#
def load_current_resource
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
  end
end
#
# Action to change owner:group of a directory in HDFS
#
action :chown do
  if @tuser == nil
    Chef::Log::info "user and group need to be provided to perform chown"
  elsif (!dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } doesn't exist; chown action not taken")
    if @tgroup == nil
      @tgroup = @meta_data["group"]
    end
  else
    converge_by("chown #{ @new_resource }") do
     @client.chown(@path, 'owner' => @tuser, 'group' => @tgroup)
    end
  end
end
#
# Action to change mode of a directory in HDFS
#
action :chmod do
  if @omode == nil
    Chef::Log::info "mode need to be provided to perform chmod"
  elsif (!dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } doesn't exist; chmod action not taken")
  else
    converge_by("chmod #{ @new_resource }") do
     @client.chmod(@path, @mode)
    end
  end
end
#
# Action to change group of a directory in HDFS
#
action :chgrp do
  if @tgroup == nil
    Chef::Log::info "target group need to be provided to perform chgrp"
  elsif (!dir_exists?(@path))
    Chef::Log::info("Directory #{ @path } doesn't exist; chmod action not taken")
  else
    converge_by("chgrp #{ @new_resource }") do
     @client.chown(@path, 'group' => @tgroup)
    end
  end
end
#
# Action to rename a directory in HDFS
#
action :rename do
  if @tpath == nil
    Chef::Log.info "Target path is empty and need to be set for rename action"
  elsif (!dir_exists?(@path))
    Chef::Log::info("Source directory #{ @path } doesn't exist; rename action not taken")
  else
    converge_by("rename #{ @new_resource }") do
      @client.rename(@path, @tpath)
    end
  end
end
