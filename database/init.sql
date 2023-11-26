create table if not exists vehicle_make
(
    make VARCHAR(50) primary key
);

create table if not exists vehicle_model
(
    make  VARCHAR(50),
    model VARCHAR(50),
    foreign key (make) references vehicle_make,
    primary key (make, model)
);

create table if not exists vehicle
(
    vehicle_id VARCHAR(50),
    year       INTEGER,
    make       VARCHAR(50),
    model      VARCHAR(50),
    miles      INTEGER,
    trim       VARCHAR(50),
    primary key (vehicle_id),
    foreign key (make) references vehicle_make,
    foreign key (make, model) references vehicle_model
);

create table if not exists customer
(
    customer_id   VARCHAR(50),
    first_name    VARCHAR(50),
    last_name     VARCHAR(50),
    phone_number  VARCHAR(50),
    email_address VARCHAR(50),
    primary key (customer_id)
);

insert into customer values (?,?,?,?);

create table if not exists transaction
(
    transaction_id UUID,
    sold_price     INTEGER,
    sold_date      DATE,
    vehicle_id     VARCHAR(50),
    customer_id    VARCHAR(50),
    primary key (transaction_id),
    foreign key (vehicle_id) references vehicle,
    foreign key (customer_id) references customer
);

create table if not exists bid
(
    bid_id        UUID,
    bid_timestamp          TIMESTAMPTZ,
    dollar_amount VARCHAR(50),
    customer_id   VARCHAR(50),
    email_address VARCHAR(50),
    primary key (bid_id),
    foreign key (customer_id) references customer
);

