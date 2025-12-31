

create table if not exists users (
    id varchar(50) primary key,
    name varchar(200) not null,
    line1 varchar(200) not null,
    line2 varchar(200),
    line3 varchar(200),
    town varchar(100) not null,
    county varchar(100) not null,
    postcode varchar(20) not null,
    phone_number varchar(20) not null,
    email varchar(200) not null,
    created_timestamp timestamptz not null,
    updated_timestamp timestamptz not null
    );

create unique index if not exists ux_users_email on users(email);

-- Accounts
create table if not exists accounts (
    account_number varchar(8) primary key,
    sort_code varchar(8) not null,
    name varchar(200) not null,
    account_type varchar(20) not null,
    balance numeric(12,2) not null,
    currency varchar(3) not null,
    user_id varchar(50) not null references users(id),
    created_timestamp timestamptz not null,
    updated_timestamp timestamptz not null
    );

create index if not exists ix_accounts_user_id on accounts(user_id);

-- Transactions
create table if not exists transactions (
    id varchar(50) primary key,
    account_number varchar(8) not null references accounts(account_number),
    amount numeric(12,2) not null,
    currency varchar(3) not null,
    type varchar(20) not null,
    reference varchar(200),
    created_timestamp timestamptz not null
    );

create index if not exists ix_transactions_account_number on transactions(account_number);


