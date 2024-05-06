

# Скользящая средняя по 3 значениям
def moving_average_3(coll: list) -> list:
    i = 0
    correct = list()
    correct.append(coll[0])
    while i < (len(coll)-2):
        i += 1
        correct.append((coll[i-1] + coll[i] + coll[i+1])/3)
    correct.append(0)
    return correct


def get_average(coll: list) -> int | float:
    sum_ = 0
    for i in range(len(coll)):
        sum_ += coll[i]
    return sum_/len(coll)


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
        K = self.h * p0 / (self.h * p0 * self.h + self.r)
        self.state = x0 + K*(data - self.h * x0)
        self.covariance = (1 - K * self.h) * p0
        return self.state


def get_velocity(coll_acc: list, coll_timestamp: list):
    i = 0
    velocity = list()
    velocity.append(0)
    while i < (len(coll_acc)-1):
        i += 1
        velocity_delta = (coll_acc[i] + coll_acc[i-1]) / \
            2 * (coll_timestamp[i] - coll_timestamp[i-1]) / 1e9
        velocity.append(velocity[i-1] + velocity_delta)
    return velocity


def get_distance(coll_acc: list, coll_timestamp: list):
    i = 0
    distance = list()
    distance.append(0)
    while i < (len(coll_acc)-1):
        i += 1
        distance_delta = (coll_acc[i] + coll_acc[i-1]) / \
            2 * (coll_timestamp[i] - coll_timestamp[i-1]) / 1e9
        distance.append(distance[i-1] + distance_delta)
    return distance


def correct_coll(coll: list, avg: int) -> list:
    new_coll = list()
    for i in range(len(coll)):
        new_coll.append(coll[i] - avg)
    return new_coll


def get_histogram(coll: list) -> list:
    gist = list()

    for i in range(len(coll)):
        coll[i] = int(abs(coll[i]))
    # print(coll)
    for i in range(max(coll) + 1):
        gist.append(0)
    # print(gist)
    for i in coll:
        gist[i] += 1
    return gist


def get_noise(coll: list, mult_cof: int = 1):
    new_coll = list()
    for i in range(len(coll)):
        new_coll.append(coll[i] * mult_cof)
    gist = get_histogram(new_coll)
    # print(gist)
    correct_gist = moving_average_3(gist)
    max_ = max(correct_gist)
    max_index = correct_gist.index(max_)
    q = 0.6 * max_
    i = 0
    while True:
        i += 1
        if (max_index + i) > len(correct_gist):
            break
        if correct_gist[max_index + i] < q:
            break
    return (max_index + i)/mult_cof


if __name__ == '__main__':
    # test = KalmanFilter(2.0, 15.0, 1.0, 1.0)
    # for i in range(10):
    # print(test.correct(i))

    test_coll = [3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
                 4, 4, 4, 1, 1, -1, -1, -1, -1, 0.5, 0.5, 1.2]
    res = get_histogram(test_coll)
    print(res)

    avg = get_average(test_coll)
    correct = correct_coll(test_coll, avg)
    res = get_noise(correct, 1)
    print(res)

    avg = get_average(test_coll)
    correct = correct_coll(test_coll, avg)
    res = get_noise(correct, 3)
    print(res)
