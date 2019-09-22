#!/usr/bin/env python3

from flask import Flask, jsonify
from os import getenv
app = Flask(__name__)

@app.route('/')
def root():
    return jsonify(hello="world")

if __name__ == '__main__':
   app.run(host="0.0.0.0", port=getenv("PORT"))
