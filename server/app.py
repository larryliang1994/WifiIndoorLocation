# coding: utf-8

from datetime import datetime

from flask import Flask
from flask import request
from flask import render_template
from flask_sockets import Sockets

from views.todos import todos_view

app = Flask(__name__)
sockets = Sockets(app)

# 动态路由
app.register_blueprint(todos_view, url_prefix='/todos')


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/time')
def time():
    return str(datetime.now())


@app.route('/get_json', methods=['GET'])
def get_json():
    user_name = request.form['user_name']
    return r'{"hello": "' + user_name + r'"}'


@sockets.route('/echo')
def echo_socket(ws):
    while True:
        message = ws.receive()
        ws.send(message)
