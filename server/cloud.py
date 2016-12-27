# coding: utf-8

from leancloud import Engine
from leancloud import Object
import Nn
# import re

from app import app


engine = Engine(app)

mac_list = []


# 测试函数
@engine.define
def hello(**params):
    print(params)
    for param in params:
        print(params[param])
    if 'mac2' in params:
        return 'Hello, {}!'.format(params['mac2'])
    else:
        return 'Hello, LeanCloud!'


# 从云端读取指纹信息
@engine.define
def query():
    location_list = []
    finger_print = Object.extend('fingerprint')
    my_query = finger_print.query
    my_query.select('name', 'info')
    query_list = my_query.find()
    for test in query_list:
        temp_dict = {}
        name = test.get('name')
        fp = test.get('info')
        temp_dict['Tag'] = name
        temp_dict['FingerPrint'] = eval(fp)
        # print(temp_dict['FingerPrint'])
        location_list.append(temp_dict)
        # print(name, fp)
    return location_list


@engine.define
def query2():
    location_list = []
    finger_print = Object.extend('fingerprint')
    my_query = finger_print.query
    my_query.select('name', 'info')
    query_list = my_query.find()
    for test in query_list:
        temp_dict = {}
        name = test.get('name')
        fp = test.get('info')
        temp_dict['Tag'] = name
        temp_dict['FingerPrint'] = eval(fp)
        # print(temp_dict['FingerPrint'])
        location_list.append(temp_dict)
        # print(name, fp)
    for location in location_list:
        # if re.search('[7][0-9]{3}', location['Tag']):
            for fp in location['FingerPrint']:
                if fp['mac'] not in mac_list:
                    mac_list.append(fp['mac'])
    for location in location_list:
        for mac in mac_list:
            flag = 1
            for fp in location['FingerPrint']:
                if mac == fp['mac']:
                    flag = 0
                    break
            if flag:
                temp_dict = {'mac': mac, 'rss': '-99'}
                location['FingerPrint'].append(temp_dict)
    return location_list


def assemble_finger_print(**params):
    count = int(params['count'])
    fp_list = []
    for i in range(1, count + 1):
        temp_dict = {}
        tag1 = 'mac' + str(i)
        tag2 = 'rss' + str(i)
        temp_dict['mac'] = params[tag1]
        temp_dict['rss'] = params[tag2]
        fp_list.append(temp_dict)
        # print(temp_dict)
    # print(fp_list)
    return fp_list


@engine.define
def get_location2(**params):
    params = eval(params['info'])
    # return len(params)
    if params['count'] == '0':
        return 'params error'
    # print(params)
    fp_list = assemble_finger_print(**params)
    data_list = query()
    # print(fp_list)
    # print(data_list[0]['FingerPrint'])
    location_num = len(data_list)
    # print(location_num)
    location = Nn.Nearest().classify(data_list, fp_list, location_num)
    print(location)
    return location


def assemble_finger_print2(**params):
    count = int(params['count'])
    fp_list = []
    for i in range(1, count + 1):
        temp_dict = {}
        tag1 = 'mac' + str(i)
        tag2 = 'rss' + str(i)
        temp_dict['mac'] = params[tag1]
        temp_dict['rss'] = params[tag2]
        fp_list.append(temp_dict)
    for mac in mac_list:
        flag = 1
        for fp in fp_list:
            if mac == fp['mac']:
                flag = 0
                break
        if flag:
            temp_dict = {'mac': mac, 'rss': '-99'}
            fp_list.append(temp_dict)
        # print(temp_dict)
    print(len(fp_list))
    return fp_list


@engine.define
def get_location(**params):
    params = eval(params['info'])
    # return len(params)
    if params['count'] == '0':
        return 'params error'
    # print(params)
    data_list = query2()
    fp_list = assemble_finger_print2(**params)
    # print(fp_list)
    # print(data_list[0]['FingerPrint'])
    location_num = len(data_list)
    # print(location_num)
    location = Nn.Nearest().classify(data_list, fp_list, location_num)
    print(location)
    return location
