

#Скользящая средняя по 3 значениям
def moving_average_3(coll: list): 
	i = 0
	correct = list()
	correct.append(coll[0])
	while i < (len(coll)-2):
		i += 1
		correct.append((coll[i-1] + coll[i] + coll[i+1])/3)
	correct.append(0)
	return correct

class KalmanFilter:
	"""docstring for KalmanFilter"""
	def __init__(self, q, r, f, h):
		self.q = q
		self.r = r
		self.f = f
		self.h = h
		self.firstValue = True

	def set_state(self, state, covariance):
		self.state = state
		self.covariance = covariance

	def correct(self, data):
		if (self.firstValue):
			self.set_state(data, 0.1)
			self.firstValue = False
			return self.state

		# time update - prediction
		# predicted state
		x0 = self.f * self.state
		# predicted covariance
		p0 = self.f * self.covariance * self.f + self.q

		# measurement update - correction
		K = self.h * p0 /(self.h * p0 * self.h + self.r);
		self.state = x0 + K*(data - self.h * x0);
		self.covariance = (1 - K* self.h)* p0;
		return self.state;

def get_velocity(coll_acc: list, coll_timestamp: list):
	i = 0
	velocity = list()
	velocity.append(0)
	while i < (len(coll_acc)-1):
		i += 1
		velocity_delta = (coll_acc[i] + coll_acc[i-1]) / 2 * (coll_timestamp[i] - coll_timestamp[i-1]) / 1e9
		velocity.append(velocity[i-1] + velocity_delta)
	return velocity

def get_distance(coll_acc: list, coll_timestamp: list):
	i = 0
	distance = list()
	distance.append(0)
	while i < (len(coll_acc)-1):
		i += 1
		distance_delta = (coll_acc[i] + coll_acc[i-1]) / 2 * (coll_timestamp[i] - coll_timestamp[i-1]) / 1e9
		distance.append(distance[i-1] + distance_delta)
	return distance

def get_velocity_delta():
	pass



if __name__ == '__main__':
	test = KalmanFilter(2.0, 15.0, 1.0, 1.0)
	for i in range(10):
		print(test.correct(i))
		
