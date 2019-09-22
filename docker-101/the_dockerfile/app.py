# This is where you'll eventually put all that wonderful Python code!
from flask import Flask
from os import getenv
app = Flask(__name__)

@app.route('/')
def root():
    return 'My color is ' + getenv('COLOR', 'clear')

if __name__ == '__main__':
    print('Hello from python')
    app.run(host='0.0.0.0')
