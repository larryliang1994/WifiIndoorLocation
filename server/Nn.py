import numpy as np
import sys


class Nearest:
    def __init__(self):
        self.location_fp_nums = 0
        self.classify_fp_nums = 0
        self.result = '数据库中无此位置信息'

    def classify(self, data_list, fp, location_nums):
        self.classify_fp_nums = len(fp)
        minimum_distance = np.sqrt(99 * 99 * self.classify_fp_nums)
        print(minimum_distance)
        # print(self.classify_fp_nums)
        for i in range(location_nums):
            # 第i次循环计算第i个位置与待定位指纹的距离
            # 并更新最短距离与定位结果
            # print(i)
            distance = 0
            self.location_fp_nums = len(data_list[i]['FingerPrint'])
            for j in range(self.classify_fp_nums):
                # 第j次循环计算第i个位置中的第j个指纹与
                # 待定位指纹的距离平方，并累加到当前距离中
                # 默认距离为 - 99
                temp = - 99
                for k in range(self.location_fp_nums):
                    # 只有匹配到相同mac地址时才会更新temp
                    # print(k)
                    # print(fp[k])
                    # print(data_list[i]['FingerPrint'][j]['mac'])
                    # print(i, j)
                    if fp[j]['mac'] == data_list[i]['FingerPrint'][k]['mac']:
                        temp = float(fp[j]['rss']) - float(data_list[i]['FingerPrint'][k]['rss'])
                        # print(temp)
                        break
                # print('temp:', j, temp)
                distance += temp * temp
            # print('dis:', k, distance)
            distance = np.sqrt(distance)
            if distance < minimum_distance:
                minimum_distance = distance
                self.result = data_list[i]['Tag']
            print('dis_to', data_list[i]['Tag'], distance)
        print('min_dis:', self.result, minimum_distance)
        return self.result

classify = Nearest()
