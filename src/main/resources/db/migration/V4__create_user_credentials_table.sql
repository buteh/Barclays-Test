create table if not exists user_credentials (
    user_id varchar(50) primary key,
    password_hash varchar(200) not null,
    created_timestamp timestamptz not null,
    updated_timestamp timestamptz not null,
    constraint fk_user_credentials_user
    foreign key (user_id)
    references users(id)
    on delete cascade
    );