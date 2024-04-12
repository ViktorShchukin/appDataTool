import psycopg2

from model import SensorData


#get data by experiment name
def get_data(name: str, sensor_type: str, y_threshold: float = 0.3):
    conn = psycopg2.connect(dbname = "sale_adviser",
                            user = "anorisno",
                            password = "GYAGG",
                            host = "localhost",
                            options = "-c search_path=android_app_tool")

    cur = conn.cursor()

    cur.execute("""select ed.acc_x , ed.acc_y , ed.acc_z , ed.value_timestamp, ed.sensor_type  from experiment_data ed 
                    join experiment e on ed.experiment_id = e.id 
                    where e.name = %s
                    order by ed.value_timestamp;""", (name,))
    timestamp = list()
    accx = list()
    accy = list()
    accz = list()
    for record in cur:
        if (record[4] == sensor_type):
            timestamp.append(record[3])
            accx.append(record[0])
            accy.append(record[1])
            accz.append(record[2])

    conn.rollback()

    return accx, accy, accz, timestamp

def get_data_row(name: str):
    conn = psycopg2.connect(dbname = "sale_adviser",
                            user = "anorisno",
                            password = "GYAGG",
                            host = "localhost",
                            options = "-c search_path=android_app_tool")

    cur = conn.cursor()

    cur.execute("""select ed.acc_x , ed.acc_y , ed.acc_z , ed.value_timestamp, ed.sensor_type  from experiment_data ed 
                    join experiment e on ed.experiment_id = e.id 
                    where e.name = %s;""", (name,))
    # result = cur.fetchall()
    dataList = list()
    for row in cur:
        dataList.append(SensorData(row))
    return dataList


if __name__ == '__main__':
    result = get_data_row(name = "row_data_gyro_6")
    print(result)