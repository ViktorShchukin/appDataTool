
import matplotlib.pyplot as plt

import accessdata as ad
import dataprocessing as dp

Y_CORRECTION_COFFECIENT = -0.012510325012303423


def print_fig(x: list, y: list, label_new: str, title: str):

    # default for settings
    fig, ax = plt.subplots()
    ax.grid(color='grey', linestyle='--', linewidth=0.5)
    ax.set_xlabel("timestamp[ns]")
    ax.set_ylabel("acceleration[m/s^2]")

    ax.plot(x, y, label=label_new)

    ax.legend()
    ax.set_title(title)
    plt.show()


def correct_signal(coll: list):
    avg = dp.get_average(coll)
    correct = dp.correct_coll(coll, avg)
    noise = dp.get_noise(correct, 1000)
    new_coll = list()
    for i in range(len(coll)):
        if abs(correct[i]) < noise:
            new_coll.append(0)
        else:
            new_coll.append(correct[i])
    return new_coll

def plot_velocity(ax, timestamp, acc, velocity, distance):
    ax.grid(color='grey', linestyle='--', linewidth=0.5)
    ax.set_xlabel("timestamp[ns]")
    ax.set_ylabel("acceleration[m/s^2]")
    # xmin, xmax = -5, 5
    ax.set_ylim(top=2, bottom=-1.5)

    # ax.plot(timestamp, accy, label = "accy", marker = "o")
    ax.plot(timestamp, acc, label="accy_y", marker="d")
    # ax.plot(timestamp, accx, label = "accy_y", marker = "d")
    ax.plot(timestamp, distance, label="distance", color="r")

    ax2 = ax.twinx()
    ax2.plot(timestamp, velocity, label="velocity", marker="*", color="k")
    ax2.set_ylabel("velocity[m/s]")
    ax2.set_ylim(top=2, bottom=-1.5)

    ax.legend()
    ax2.legend()
    # ax.set_title("accy + correct 100cm on floor\nsamle: 1")

def plot_gyro(ax, timestamp, velocity, angle):
    ax.grid(color='grey', linestyle='--', linewidth=0.5)

    ax.plot(timestamp, velocity, label="velocity", marker="d")
    ax.plot(timestamp, angle, label="angle", color="r")

if __name__ == '__main__':
    # for j in range(1,7):
    # android.sensor.linear_acceleration/gyroscope
    experimnet_name = "aroundme_3"# "zigzag_1"
    accx, accy, accz, acc_timestamp = ad.get_data(
        name=experimnet_name,
        sensor_type="android.sensor.linear_acceleration")

    radx, rady, radz, gyro_timestamp = ad.get_data(
    	name = experimnet_name,
    	sensor_type = "android.sensor.gyroscope")

    # for i in range(len(accy)):
    # accy[i] -= Y_CORRECTION_COFFECIENT
    accy_correct = list()
    # kalman_filter = dp.KalmanFilter(q=2, r=15, f=1.0, h=1.0)
    # for i in range(len(accy)):
    # accy_correct.append(kalman_filter.correct(accy[i]))
    accy_correct_x = correct_signal(accx)
    accy_correct_y = correct_signal(accy)
    accy_correct_z = correct_signal(accz)

    velocity_x = dp.get_velocity(accy_correct_x, acc_timestamp)
    velocity_y = dp.get_velocity(accy_correct_y, acc_timestamp)
    velocity_z = dp.get_velocity(accy_correct_z, acc_timestamp)

    distance_x = dp.get_distance(velocity_x, acc_timestamp)
    distance_y = dp.get_distance(velocity_y, acc_timestamp)
    distance_z = dp.get_distance(velocity_z, acc_timestamp)

    angle_x = dp.get_distance(radx, gyro_timestamp)
    angle_y = dp.get_distance(rady, gyro_timestamp)
    angle_z = dp.get_distance(radz, gyro_timestamp)

    # print_fig(x = timestamp, y = accy_correct, label_new = "accy_correct", title = "accy 100cm on floor\ncorrect samle: 1")

    fig, ax = plt.subplots(3, 2)

    plot_velocity(
    	ax = ax[0,0],
    	timestamp = acc_timestamp,
    	acc = accx,
    	velocity = velocity_x,
    	distance = distance_x)
    plot_velocity(
    	ax = ax[1,0],
    	timestamp = acc_timestamp,
    	acc = accy,
    	velocity = velocity_y,
    	distance = distance_y)
    plot_velocity(
    	ax = ax[2,0],
    	timestamp = acc_timestamp,
    	acc = accz,
    	velocity = velocity_z,
    	distance = distance_z)

    plot_gyro(
    	ax = ax[0,1],
    	timestamp = gyro_timestamp,
    	velocity = radx,
    	angle = angle_x)
    plot_gyro(
    	ax = ax[1,1],
    	timestamp = gyro_timestamp,
    	velocity = rady,
    	angle = angle_y)
    plot_gyro(
    	ax = ax[2,1],
    	timestamp = gyro_timestamp,
    	velocity = radz,
    	angle = angle_z)
    plt.show()
