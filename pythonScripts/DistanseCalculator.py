"""
 p0 = [x y z] = [0 0 0]

 p = p0 * M1 * M2 * M3 ...

orintation = [0 0 0]
orintation[n] = orintation[n-1] + integral(gyro now)

вращение вокруг оси на угол O:
x -> [         x*1          (y*cosO - z*sinO)  (y*sinO + z*cosO) ]

y -> [ (x*cosO + z*sinO)          y          (z*cosO - x*sinO) ]

z -> [ (x*cosO - y*sinO)  (x*sinO + y*cosO)          z         ]

"""
from math import sin, cos

import accessdata
from model import SensorData


class DistanseCalculator:

    def __init__(self):
        # angles of phone position in rad
        self.orintation = [0, 0, 0]
        self.lastVelosity = [0, 0, 0]
        self.distanse = [0, 0, 0]

        self.lastGyroscopeData: SensorData
        self.lastAccelerationData: SensorData

        self.listAcceleration = [[], [], []]
        self.listVelosity = []
        self.listDistance = []
        self.listTimestamp = []

        self.isFirstDataGyro: bool = True
        self.isFirstDataAcc: bool = True
        self.isFirstDataVelo: bool = True

    def saveData(self, data: SensorData):
        match data.sensorType:
            case "android.sensor.gyroscope":
                if self.isFirstDataGyro is True:
                    self.lastGyroscopeData = data
                    self.isFirstDataGyro = False
                    return
                self.evalueteGyroscope(self.lastGyroscopeData, data)
                self.lastGyroscopeData = data
            case "android.sensor.linear_acceleration":
                if self.isFirstDataAcc is True:
                    self.lastAccelerationData = self.rotate(data)
                    self.listAcceleration[0].append(
                        self.lastAccelerationData.y)
                    self.listAcceleration[1].append(
                        self.lastAccelerationData.timestamp)
                    self.listAcceleration[2].append(
                        self.lastAccelerationData.x)
                    # print("x= {}, y= {}, z=  {}".format(self.lastAccelerationData.x, self.lastAccelerationData.y, self.lastAccelerationData.z))
                    self.isFirstDataAcc = False
                    return
                self.evalueteAcceleration(
                    self.lastAccelerationData, self.rotate(data))
                self.lastAccelerationData = self.rotate(data)
                self.listAcceleration[0].append(self.lastAccelerationData.y)
                self.listAcceleration[1].append(
                    self.lastAccelerationData.timestamp)
                self.listAcceleration[2].append(self.lastAccelerationData.x)

    def integral(self, valueBefore: int, valueLast: int, timestampBefore: int, timestampLast: int):
        delta = (valueLast + valueBefore)/2 * \
            (timestampLast - timestampBefore)/1e9
        return delta

    def evalueteGyroscope(self, dataBefore: SensorData, dataLast: SensorData):
        delta_x = self.integral(dataBefore.x, dataLast.x,
                                dataBefore.timestamp, dataLast.timestamp)
        delta_y = self.integral(dataBefore.y, dataLast.y,
                                dataBefore.timestamp, dataLast.timestamp)
        delta_z = self.integral(dataBefore.z, dataLast.z,
                                dataBefore.timestamp, dataLast.timestamp)
        self.orintation[0] += delta_x
        self.orintation[1] += delta_y
        self.orintation[2] += delta_z

    def evalueteAcceleration(self, dataBefore: SensorData, dataLast: SensorData):

        delta_velocity_x = self.integral(
            dataBefore.x, dataLast.x, dataBefore.timestamp, dataLast.timestamp)
        delta_velocity_y = self.integral(
            dataBefore.y, dataLast.y, dataBefore.timestamp, dataLast.timestamp)
        delta_velocity_z = self.integral(
            dataBefore.z, dataLast.z, dataBefore.timestamp, dataLast.timestamp)

        delta_distance_x = self.integral(
            self.lastVelosity[0], self.lastVelosity[0] + delta_velocity_x, dataBefore.timestamp, dataLast.timestamp)
        delta_distance_y = self.integral(
            self.lastVelosity[1], self.lastVelosity[1] + delta_velocity_y, dataBefore.timestamp, dataLast.timestamp)
        delta_distance_z = self.integral(
            self.lastVelosity[2], self.lastVelosity[2] + delta_velocity_z, dataBefore.timestamp, dataLast.timestamp)
        self.lastVelosity[0] += delta_velocity_x
        self.lastVelosity[1] += delta_velocity_y
        self.lastVelosity[2] += delta_velocity_z
        self.distanse[0] += delta_distance_x
        self.distanse[1] += delta_distance_y
        self.distanse[2] += delta_distance_z

        self.listVelosity.append(self.lastVelosity[1]+delta_velocity_y)
        self.listDistance.append(self.distanse[1]+delta_distance_y)
        self.listTimestamp.append(dataLast.timestamp)

    def getRotationMatrix(self):
        rot_x = [
            [1, 0, 0],
            [0, cos(self.orintation[0]), sin(self.orintation[0])],
            [0, -sin(self.orintation[0]), cos(self.orintation[0])]
        ]
        rot_y = [
            [cos(self.orintation[1]), 0, -sin(self.orintation[1])],
            [0, 1, 0],
            [sin(self.orintation[1]), 0, cos(self.orintation[1])]
        ]
        rot_z = [
            [cos(self.orintation[2]), sin(self.orintation[2]), 0],
            [-sin(self.orintation[2]), cos(self.orintation[2]), 0],
            [0, 0, 1]
        ]
        rot_xy = mult_matrix(rot_x, rot_y)
        rot_xyz = mult_matrix(rot_xy, rot_z)
        return rot_xyz

    def rotate(self, data: SensorData) -> SensorData:
        vec = [[data.x, data.y, data.z]]
        res = mult_matrix(vec, self.getRotationMatrix())
        # print(res, "======================")
        return SensorData((res[0][0], res[0][1], res[0][2], data.timestamp, data.sensorType))


def mult_matrix(a: list[list], b: list[list]):
    if len(a[0]) != len(b):
        raise ArithmeticError(
            "cannot multiplicate matrix because of different row and kolumn number")
    newMatrix = list()
    for i in range(len(a)):
        newMatrix.append(list())
        for j in range(len(b[i])):
            sum_ = 0
            for k in range(len(a[i])):
                sum_ += (a[i][k]*b[k][j])
            newMatrix[i].append(sum_)
    return newMatrix


if __name__ == '__main__':
    cal = DistanseCalculator()
    dataList = accessdata.get_data_row(name="rotate_old_1_3")
    for i in range(len(dataList)):
        cal.saveData(dataList[i])

    # gyro1 = SensorData((0, 0, 1.58, 1e9, "android.sensor.gyroscope"))
    # gyro2 = SensorData((0,0,1.58, 2e9, "android.sensor.gyroscope"))
    # cal.saveData(gyro1)
    # cal.saveData(gyro2)

    # acc1 = SensorData((0,1,0,5e9, "android.sensor.linear_acceleration"))
    # acc2 = SensorData((0,1,0,7e9, "android.sensor.linear_acceleration"))
    # cal.saveData(acc1)
    # cal.saveData(acc2)

    print(cal.orintation)
    print(cal.distanse)

    import matplotlib.pyplot as plt
    import dataprocessing as dp

    fig, ax = plt.subplots()
    ax.grid(color='grey', linestyle='--', linewidth=0.5)
    ax.set_xlabel("timestamp[ns]")
    ax.set_ylabel("acceleration[m/s^2]")

    velocity_y = dp.get_velocity(
        cal.listAcceleration[0], cal.listAcceleration[1])

    ax.plot(cal.listAcceleration[1],
            cal.listAcceleration[0], label="accy_y", marker="d")
    ax.plot(cal.listTimestamp, cal.listVelosity, label="vel_y", marker="o")
    ax.plot(cal.listTimestamp, cal.listDistance, label="dist_y", marker="^")

    plt.show()
    # print(cal.getRotationMatrix())
    # m1 = [[3,5],[2,1]]
    # m2 = [[8,2,3],[1,7,2]]
    # res = mult_matrix(m1, m2)
    # print(res)

    # m1 = [[11, 2, 3, 5]]
    # m2 = [[1,3], [5,2], [7,8], [9, 22]]
    # res = mult_matrix(m1, m2)
    # print(res)
