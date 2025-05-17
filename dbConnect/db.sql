create type fuelType as enum(
    'antimatter',
    'electricity',
    'diesel'
);

create table collection (
    id serial primary key,
    name varchar(25) not null,
    coordinateX bigint not null CHECK ( coordinateX > -818 ),
    coordinateY bigint not null CHECK ( coordinateY < 730 ),
    enginePower float,
    capacity float not null,
    distanceTravelled int not null,
    fuelType fuelType not null
);

insert into collection(name, coordinateX, coordinateY, enginePower, capacity, distanceTravelled, fuelType)
values (
        'pidor',
        34,
        56,
        4252.0,
        3242,
        24,
        'diesel'
);