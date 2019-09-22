#!/usr/bin/env python3

from flask import Flask, request
from pathlib import Path
db = Flask(__name__)

@db.route('/')
def root():
    return """\
I'm a key/value store.

GET         /keys  to list all keys
GET         /<key> to read values
POST or PUT /<key> to write values
"""

@db.route('/keys')
def list():
    path = Path(f'/data')
    return "\n".join(map(lambda p: p.name, sorted(path.glob("*"))))

@db.route('/<key>', methods=['GET'])
def read(key):
    path = Path(f'/data/{key}')
    if path.is_file():
        return path.read_text()
    else:
        return "No data written", 404

@db.route('/<key>', methods=['PUT', 'POST'])
def write(key):
    data = request.get_data(as_text=True)
    path = Path(f'/data/{key}')
    path.write_text(data)
    return data

if __name__ == '__main__':
   db.run(host="0.0.0.0", port="8000")
