alter table users
    add constraint ck_users_id_format
        check (id ~ '^usr-[A-Za-z0-9]+$');

alter table users
    add constraint ck_users_email_format
        check (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$');

alter table accounts
    add constraint ck_accounts_account_number_format
        check (account_number ~ '^01\d{6}$');
