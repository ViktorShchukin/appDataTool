import psycopg2


#get data by experiment name
def get_data(name: str):
    conn = psycopg2.connect(dbname = "sale_adviser",
                            user = "anorisno",
                            password = "GYAGG",
                            host = "localhost",
                            options = "-c search_path=android_app_tool")

    cur = conn.cursor()

    cur.execute("""select ed.id , ed.acc_x , ed.acc_y , ed.acc_z , ed.value_timestamp  from experiment_data ed 
                    join experiment e on ed.experiment_id = e.id 
                    where e.name = %s;""", (name,))
    timestamp = list()
    accx = list()
    accy = list()
    accz = list()
    for record in cur:
        timestamp.append(record[4])
        accx.append(record[1])
        accy.append(record[2])
        accz.append(record[3])

    conn.rollback()

    return accx, accy, accz, timestamp
