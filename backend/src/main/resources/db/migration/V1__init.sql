    create table bookings (
        is_active boolean not null,
        created_at timestamp(6),
        end_time timestamp(6) not null,
        start_time timestamp(6) not null,
        updated_at timestamp(6),
        id uuid not null,
        resource_id uuid not null,
        service_id uuid not null,
        tenant_id uuid not null,
        customer_email varchar(255) not null,
        customer_name varchar(255) not null,
        customer_phone varchar(255),
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','CANCELED','COMPLETED')),
        primary key (id)
    );

    comment on column bookings.is_active is
        'Soft-delete indicator';

    create table resources (
        capacity integer not null,
        is_active boolean not null,
        created_at timestamp(6),
        updated_at timestamp(6),
        id uuid not null,
        tenant_id uuid not null,
        image_url varchar(255),
        name varchar(255) not null,
        primary key (id)
    );

    comment on column resources.is_active is
        'Soft-delete indicator';

    create table service_resources (
        resource_id uuid not null,
        service_id uuid not null,
        primary key (resource_id, service_id)
    );

    create table services (
        is_active boolean not null,
        duration_minutes integer not null,
        created_at timestamp(6),
        updated_at timestamp(6),
        id uuid not null,
        tenant_id uuid not null,
        image_url TEXT,
        name varchar(255) not null,
        primary key (id)
    );

    comment on column services.is_active is
        'Soft-delete indicator';

    create table tenants (
        is_active boolean not null,
        created_at timestamp(6),
        updated_at timestamp(6),
        id uuid not null,
        name varchar(255) not null,
        slug varchar(255) not null unique,
        config jsonb,
        primary key (id)
    );

    comment on column tenants.is_active is
        'Soft-delete indicator';

    create table users (
        is_active boolean not null,
        created_at timestamp(6),
        updated_at timestamp(6),
        id uuid not null,
        tenant_id uuid,
        email varchar(255) not null unique,
        first_name varchar(255),
        last_name varchar(255),
        password varchar(255) not null,
        role varchar(255) not null check (role in ('SUPER_ADMIN','TENANT_ADMIN','CUSTOMER')),
        primary key (id)
    );

    comment on column users.is_active is
        'Soft-delete indicator';

    create index idx_booking_tenant_id 
       on bookings (tenant_id);

    create index idx_booking_service_id 
       on bookings (service_id);

    create index idx_booking_resource_id 
       on bookings (resource_id);

    create index idx_booking_start_time 
       on bookings (start_time);

    create index idx_resource_tenant_id 
       on resources (tenant_id);

    create index idx_service_tenant_id 
       on services (tenant_id);

    create index idx_tenant_slug 
       on tenants (slug);

    create index idx_user_tenant_id 
       on users (tenant_id);

    create index idx_user_email 
       on users (email);

    alter table if exists bookings 
       add constraint FK9nmcopsvhdggf4ywp5190ytb4 
       foreign key (resource_id) 
       references resources;

    alter table if exists bookings 
       add constraint FKjcwbou2jlblfwu14uoxs65b25 
       foreign key (service_id) 
       references services;

    alter table if exists bookings 
       add constraint FKju89l1g6fe8swctxof1g0wwk8 
       foreign key (tenant_id) 
       references tenants;

    alter table if exists resources 
       add constraint FKi48s3yt6v7mbp8l32w5mavi0j 
       foreign key (tenant_id) 
       references tenants;

    alter table if exists service_resources 
       add constraint FKb2pls8t76bfmmxb53rgjpkf81 
       foreign key (resource_id) 
       references resources;

    alter table if exists service_resources 
       add constraint FKl53quxeymv97m47vqgsdf4mqb 
       foreign key (service_id) 
       references services;

    alter table if exists services 
       add constraint FKt68rlejcbkhade8mmcw0vvup1 
       foreign key (tenant_id) 
       references tenants;

    alter table if exists users 
       add constraint FK21hn1a5ja1tve7ae02fnn4cld 
       foreign key (tenant_id) 
       references tenants;
