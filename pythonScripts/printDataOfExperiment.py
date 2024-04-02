
import matplotlib.pyplot as plt

import accessdata as ad
import dataprocessing as dp



def print_fig(x: list, y: list, label_new: str, title: str):
	#default for settings
	fig, ax = plt.subplots()
	ax.grid(color = 'grey', linestyle = '--', linewidth = 0.5)
	ax.set_xlabel("timestamp[ns]")
	ax.set_ylabel("acceleration[m/s^2]")

	ax.plot(x, y, label = label_new)

	ax.legend()
	ax.set_title(title)
	plt.show()

if __name__ == '__main__':
	for j in range(1,7):
		accx, accy, accz, timestamp = ad.get_data(name = "row_data_100_2_" + str(j))
		accy_correct = list()
		kalman_filter = dp.KalmanFilter(q = 0.1, r = 0.5, f = 1.0, h = 1.0)
		for i in range(len(accy)):
			accy_correct.append(kalman_filter.correct(accy[i]))
		velocity_y = dp.get_velocity(accy_correct, timestamp)
		distance_y = dp.get_distance(velocity_y, timestamp)


		# print_fig(x = timestamp, y = accy_correct, label_new = "accy_correct", title = "accy 100cm on floor\ncorrect samle: 1")

		fig, ax = plt.subplots()
		ax.grid(color = 'grey', linestyle = '--', linewidth = 0.5)
		ax.set_xlabel("timestamp[ns]")
		ax.set_ylabel("acceleration[m/s^2]")
		# xmin, xmax = -5, 5
		ax.set_ylim(top = 2, bottom = -1.5)

		# ax.plot(timestamp, accy, label = "accy", marker = "o")
		ax.plot(timestamp, accy_correct, label = "accy_correct", marker = "d")
		ax.plot(timestamp, distance_y, label = "distance", color = "r")

		ax2 = ax.twinx()
		ax2.plot(timestamp, velocity_y, label = "velocity_y", marker = "*", color = "k")
		ax2.set_ylabel("velocity[m/s]")
		ax2.set_ylim(top = 2, bottom = -1.5)

		ax.legend()
		ax2.legend()
		ax.set_title("accy + correct 100cm on floor\nsamle: 1")
		plt.show()















