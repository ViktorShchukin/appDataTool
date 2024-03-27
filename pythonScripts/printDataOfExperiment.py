
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
	accx, accy, accz, timestamp = ad.get_data(name = "row_data_100_1")
	accy_correct = dp.moving_average_3(accy)

	# print_fig(x = timestamp, y = accy_correct, label_new = "accy_correct", title = "accy 100cm on floor\ncorrect samle: 1")

	fig, ax = plt.subplots()
	ax.grid(color = 'grey', linestyle = '--', linewidth = 0.5)
	ax.set_xlabel("timestamp[ns]")
	ax.set_ylabel("acceleration[m/s^2]")

	ax.plot(timestamp, accy, label = "accy")
	ax.plot(timestamp, accy_correct, label = "accy_correct")

	ax.legend()
	ax.set_title("accy + correct 100cm on floor\nsamle: 1")
	plt.show()















