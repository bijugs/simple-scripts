import json
from flask import Flask
from flask import Flask,request,Response
from kazoo.client import KazooClient

zk_map = {'zk1':'jost:port','zk2','host:port'}
app = Flask(__name__)

@app.route('/')
def need_cluster_name():
    return 'Please include cluster name to the URL\n'+\
           200, {'Content-Type': 'text/plain; charset=utf-8'}

@app.route('/<pathVariable>/')
def cluster_state(pathVariable):
    try:
        zk_host = zk_map[pathVariable]
        zk = KazooClient(hosts=zk_host, read_only=True)
        zk.start()
        if pathVariable.find('kafka') > 0:
            nodes = zk.get_children('/brokers/ids')
            brokers = ""
            for id in nodes:
                data, stat = zk.get('/brokers/ids/'+id)
                jdata = json.loads(data)
                brokers += jdata['host']+"\n"
            return 'There are '+str(len(nodes))+\
                   ' brokers running\nids: '+\
                   ','.join(nodes)+'\nbrokers:'+\
                   brokers+'\nZK:'+zk_host+\
                   '\nThe cluster looks healthy. ', 200, {'Content-Type': 'text/plain; charset=utf-8'}
        else:
            data, stat = zk.get('/hbase/master')
            start = data.find('bach-')
            end = data.find('.bloomberg')
            hmaster = data[start:end]
            data = zk.get_children('/hbase/rs')
            rs = ""
            for node in data:
               rs += node+"\n"
            return "Its a hadoop cluster\n"+\
            'hmaster :'+hmaster+\
            '\nRegionServers :'+ rs+\
            '\nZK: '+zk_host+\
            '\nThe cluster looks healthy.', 200, {'Content-Type': 'text/plain; charset=utf-8'}
        zk.stop()
    except:
        return 'Cluster seems down'

if __name__ == '__main__':
    app.run(host='0.0.0.0')
