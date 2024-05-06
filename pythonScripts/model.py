

class SensorData:

    def __init__(self, data: list):
        self.x = data[0]
        self.y = data[1]
        self.z = data[2]
        self.timestamp = data[3]
        self.sensorType = data[4]
