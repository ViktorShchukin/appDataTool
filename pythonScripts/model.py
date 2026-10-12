

class SensorData:

    def __init__(self, data: list):
        self.x = data[0]
        self.y = data[1]
        self.z = data[2]
        self.timestamp = data[3]
        self.sensorType = data[4]

    def __str__(self):
        return f"{{{self.sensorType}, {self.timestamp}, {self.x}, {self.y}, {self.z}}}"

    def __repr__(self):
        return self.__str__()