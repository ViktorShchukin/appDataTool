CREATE TABLE experiment(
    id UUID NOT NULL,
    name VARCHAR NOT NULL,
    phone_brand VARCHAR NOT NULL, --android.os.build.BRAND
    phone_model VARCHAR NOT NULL, --android.os.build.MODEL
    phone_serial VARCHAR NOT NULL, --android.os.build.getSerial()
    ex_timestamp TIMESTAMP NOT NULL,
    comment VARCHAR,

    CONSTRAINT experiment_pk PRIMARY KEY id,
    CONSTRAINT experiment_unique UNIQUE (phone_brand, phone_model, ex_timestamp)
    )

--

CREATE TABLE experiment_data(
    id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    --acc: acceleration
    acc_x DOUBLE PRECISION NOT NULL,
    acc_y DOUBLE PRECISION NOT NULL,
    acc_z DOUBLE PRECISION NOT NULL,
    value_timestamp BIGINT NOT NULL,

    CONSTRAINT experiment_data_pk PRIMARY KEY id,
    CONSTRAINT experiment_data_fk FOREIGN KEY experiment_id REFERENCES experiment(id)
    )