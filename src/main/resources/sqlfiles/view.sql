-- templatedb.staging_data definition

-- Drop table

-- DROP TABLE templatedb.staging_data;

CREATE TABLE staging_data
(
    "oid"              varchar(128) NOT NULL,
    staging_data_id    varchar(128) NOT NULL,
    process_id         varchar(128) NULL,
    member_id          varchar(32) NULL,
    member_name        varchar(128) NULL,
    mobile             text NULL,
    samity_id          varchar(32) NULL,
    samity_name        varchar(128) NULL,
    samity_day         varchar(128) NOT NULL,
    mfi_id             varchar(128) NOT NULL,
    field_officer_id   varchar(32) NULL,
    field_officer_name varchar(128) NULL,
    total_member       numeric(4)   NOT NULL,
    downloaded_by      varchar(128) NULL,
    downloaded_on      timestamp NULL,
    created_by         varchar(128) NOT NULL DEFAULT 'System':: character varying,
    created_on         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         varchar(128) NULL,
    updated_on         timestamp NULL,
    process_start_time timestamp NULL,
    process_end_time   timestamp NULL,
    status             varchar NULL,
    CONSTRAINT ck_samity_day_staging_data CHECK ((((samity_day)::text = 'Saturday'::text
) OR ((samity_day)::text = 'Sunday'::text) OR ((samity_day)::text = 'Monday'::text) OR ((samity_day)::text = 'Tuesday'::text) OR ((samity_day)::text = 'Wednesday'::text) OR ((samity_day)::text = 'Thursday'::text) OR ((samity_day)::text = 'Friday'::text))),
	CONSTRAINT pk_staging_data PRIMARY KEY (oid),
	CONSTRAINT uk_staging_data_id_staging_data UNIQUE (staging_data_id)
);


-- templatedb.employee definition

-- Drop table

-- DROP TABLE templatedb.employee;

CREATE TABLE employee
(
    "oid"                    varchar(128) NOT NULL,
    employee_id              varchar(32)  NOT NULL,
    company_employee_id      varchar(32) NULL,
    person_id                varchar(32) NULL,
    login_id                 varchar(128) NULL,
    emp_name_en              varchar(128) NOT NULL,
    emp_name_bn              varchar(128) NULL,
    date_of_birth            date NULL,
    blood_group              varchar(32) NULL,
    religion                 varchar(32) NULL,
    gender                   varchar(32) NULL,
    marital_status           varchar(32) NULL,
    academic_qualification   varchar(128) NULL,
    nationality              varchar(32) NULL,
    remarks                  varchar(32) NULL,
    father_name_en           varchar(128) NULL,
    father_name_bn           varchar(128) NULL,
    mother_name_en           varchar(128) NULL,
    mother_name_bn           varchar(128) NULL,
    spouse_name_en           varchar(128) NULL,
    spouse_name_bn           varchar(128) NULL,
    spouse_contact_no        varchar(128) NULL,
    mobile                   text         NOT NULL,
    personal_email           text         NOT NULL,
    official_email           text NULL,
    res_division_id          varchar(32)  NOT NULL,
    res_district_id          varchar(32)  NOT NULL,
    res_upazila_id           varchar(32)  NOT NULL,
    res_union_id             varchar(32) NULL,
    res_ward_village_street  varchar(128) NULL,
    res_post_office_id       varchar(32) NULL,
    res_postal_code          varchar(32) NULL,
    res_address_line1       varchar(128) NOT NULL,
    res_address_line2       varchar(32) NULL,
    per_division_id          varchar(32)  NOT NULL,
    per_district_id          varchar(32)  NOT NULL,
    per_upazila_id           varchar(32)  NOT NULL,
    per_union_id             varchar(32) NULL,
    per_ward_village_street  varchar(128) NULL,
    per_post_office_id       varchar(32) NULL,
    per_postal_code          varchar(32) NULL,
    per_address_line1       varchar(128) NOT NULL,
    per_address_line2       varchar(128) NULL,
    identification_type      varchar(128) NOT NULL,
    nid_number               varchar(17) NULL,
    smart_card_id_number     varchar(10) NULL,
    nid_issue_date           date NULL,
    nid_front_doc_id         varchar(128) NULL,
    nid_back_doc_id          varchar(128) NULL,
    birth_reg_no             varchar(128) NULL,
    birth_issue_date         date NULL,
    birth_reg_doc_id         varchar(128) NULL,
    passport_no              varchar(128) NULL,
    passport_issue_date      date NULL,
    passport_expiration_date date NULL,
    passport_doc_id          varchar(128) NULL,
    driving_license_no       varchar(128) NULL,
    driving_license_doc_id   varchar(128) NULL,
    other_id_no              varchar(128) NULL,
    other_doc_name           varchar(128) NULL,
    other_id_doc_id          varchar(128) NULL,
    photo_image_id           varchar(128) NULL,
    tin_no                   text NULL,
    tin_doc_id               varchar(128) NULL,
    pay_method               varchar(128) NULL,
    bank_id                  text NULL,
    bank_branch_id           text NULL,
    bank_account_no          varchar(128) NULL,
    digital_pay_comp_id      varchar(32) NULL,
    digital_wallet_number    varchar(128) NULL,
    role_id                  varchar(128) NULL,
    empl_smt_off_map_id      varchar(128) NULL,
    mfi_id                   varchar(128) NOT NULL,
    current_version          varchar(32) NULL DEFAULT '1':: character varying,
    is_new_record            varchar(128) NULL,
    approved_by              varchar(128) NULL,
    approved_on              timestamp NULL,
    remarked_by              varchar(128) NULL,
    remarked_on              timestamp NULL,
    is_approver_remarks      varchar(32) NULL,
    approver_remarks         text NULL,
    status                   varchar(32)  NOT NULL DEFAULT 'Active':: character varying,
    migrated_by              varchar(128) NULL,
    migrated_on              timestamp NULL,
    created_by               varchar(128) NOT NULL DEFAULT 'System':: character varying,
    created_on               timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               varchar(128) NULL,
    updated_on               timestamp NULL,
    CONSTRAINT ck_is_approver_remarks_employee CHECK ((((is_approver_remarks)::text = 'Yes'::text
) OR ((is_approver_remarks)::text = 'No'::text))),
	CONSTRAINT ck_is_new_record_employee CHECK ((((is_new_record)::text = 'Yes'::text) OR ((is_new_record)::text = 'No'::text))),
	CONSTRAINT ck_status_employee CHECK ((((status)::text = 'Active'::text) OR ((status)::text = 'Inactive'::text) OR ((status)::text = 'Blacklisted'::text) OR ((status)::text = 'Exited'::text))),
	CONSTRAINT pk_employee PRIMARY KEY (oid),
	CONSTRAINT uk_employee_id_employee UNIQUE (employee_id)
);


-- templatedb.staging_account_data definition

-- Drop table

-- DROP TABLE templatedb.staging_account_data;

CREATE TABLE staging_account_data
(
    "oid"                          varchar(128) NOT NULL,
    staging_account_data_id        varchar(128) NOT NULL,
    member_id                      varchar(32) NULL,
    loan_account_id                varchar(32) NULL,
    product_code                   varchar(32) NULL,
    product_name                   varchar(128) NULL,
    loan_amount                    numeric(10, 2) NULL DEFAULT 0,
    service_charge                 numeric(10, 2) NULL DEFAULT 0,
    installments                   text NULL,
    total_due                      numeric(10, 2) NULL DEFAULT 0,
    total_principal_paid           numeric(10, 2) NULL DEFAULT 0,
    total_principal_remaining      numeric(10, 2) NULL DEFAULT 0,
    total_service_charge_paid      numeric(10, 2) NULL DEFAULT 0,
    total_service_charge_remaining numeric(10, 2) NULL DEFAULT 0,
    savings_account_id             varchar(128) NULL,
    savings_product_code           varchar(128) NULL,
    savings_product_name           varchar(128) NULL,
    savings_product_type           varchar(128) NULL,
    target_amount                  numeric(10, 2) NULL DEFAULT 0,
    balance                        numeric(10, 2) NULL DEFAULT 0,
    last_deposit_amount            numeric(10, 2) NULL DEFAULT 0,
    total_deposit                  numeric(10, 2) NULL DEFAULT 0,
    total_withdraw                 numeric(10, 2) NULL DEFAULT 0,
    accrued_interest_amount        numeric(10, 2) NULL DEFAULT 0,
    deposit_scheme_detail          text NULL,
    created_by                     varchar(128) NOT NULL DEFAULT 'System':: character varying,
    created_on                     timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                     varchar(128) NULL,
    updated_on                     timestamp NULL,
    CONSTRAINT pk_staging_account_data PRIMARY KEY (oid),
    CONSTRAINT uk_staging_account_data_id_staging_account_data UNIQUE (staging_account_data_id)
);


-- templatedb.collection_staging_data definition

-- Drop table

-- DROP TABLE templatedb.collection_staging_data;

CREATE TABLE collection_staging_data
(
    "oid"                      varchar(128) NOT NULL,
    collection_staging_data_id varchar(128) NOT NULL,
    samity_id                  varchar(128) NOT NULL,
    staging_data_id            varchar(128) NOT NULL,
    account_type               varchar(128) NULL,
    loan_account_id            varchar(128) NULL,
    savings_account_id         varchar(128) NULL,
    amount                     numeric(10, 2) NULL DEFAULT 0,
    payment_mode               varchar(128) NULL,
    collection_type            varchar(128) NULL,
    submitted_on               timestamp NULL,
    submitted_by               varchar(128) NULL,
    is_uploaded                varchar(128) NULL,
    uploaded_on                timestamp NULL,
    uploaded_by                varchar(128) NULL,
    approved_on                timestamp NULL,
    approved_by                varchar(128) NULL,
    current_version            varchar(32) NULL,
    status                     varchar(32) NULL,
    created_by                 varchar(128) NOT NULL DEFAULT 'System':: character varying,
    created_on                 timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 varchar(128) NULL,
    updated_on                 timestamp NULL,
    CONSTRAINT ck_account_type_collection_staging_data CHECK ((((account_type)::text = 'Loan'::text
) OR ((account_type)::text = 'Savings'::text))),
	CONSTRAINT ck_collection_type_collection_staging_data CHECK ((((collection_type)::text = 'Regular'::text) OR ((collection_type)::text = 'Special'::text))),
	CONSTRAINT ck_is_uploaded_collection_staging_data CHECK ((((is_uploaded)::text = 'Yes'::text) OR ((is_uploaded)::text = 'No'::text))),
	CONSTRAINT ck_status_collection_staging_data CHECK ((((status)::text = 'Staged'::text) OR ((status)::text = 'Approved'::text))),
	CONSTRAINT pk_collection_staging_data PRIMARY KEY (oid)
);






INSERT INTO employee
("oid", employee_id, company_employee_id, person_id, login_id, emp_name_en, emp_name_bn, date_of_birth, blood_group, religion, gender, marital_status, academic_qualification, nationality, remarks, father_name_en, father_name_bn, mother_name_en, mother_name_bn, spouse_name_en, spouse_name_bn, spouse_contact_no, mobile, personal_email, official_email, res_division_id, res_district_id, res_upazila_id, res_union_id, res_ward_village_street, res_post_office_id, res_postal_code, res_address_line1, res_address_line2, per_division_id, per_district_id, per_upazila_id, per_union_id, per_ward_village_street, per_post_office_id, per_postal_code, per_address_line1, per_address_line2, identification_type, nid_number, smart_card_id_number, nid_issue_date, nid_front_doc_id, nid_back_doc_id, birth_reg_no, birth_issue_date, birth_reg_doc_id, passport_no, passport_issue_date, passport_expiration_date, passport_doc_id, driving_license_no, driving_license_doc_id, other_id_no, other_doc_name, other_id_doc_id, photo_image_id, tin_no, tin_doc_id, pay_method, bank_id, bank_branch_id, bank_account_no, digital_pay_comp_id, digital_wallet_number, role_id, empl_smt_off_map_id, mfi_id, current_version, is_new_record, approved_by, approved_on, remarked_by, remarked_on, is_approver_remarks, approver_remarks, status, migrated_by, migrated_on, created_by, created_on, updated_by, updated_on)
VALUES('MRA-IMS-Employee-Oid-0007', 'EMP-0007', 'C-EMP-0007', 'MRA-PER-0007', 'abul', 'Abul Kalam', 'আবুল কালাম', '1980-05-13', NULL, 'Islam', 'Male', 'Unmarried', 'Diploma in Engineering', 'Bangladeshi', NULL, 'Abul Hasan', 'আবুল হাসান', 'Munira Khatun', 'মুনিরা খাতুন', '-', '-', NULL, '[{"contact":"1","contactNo":"01914012488"},{"contact":"2","contactNo":"01810176831"}]', '[{ "email": "1", "emailAddress": "abul.kalam@template.com" }]', '[{ "email": "1", "emailAddress": "abul.kalam@template.com" }]', 'BD-KHU', 'DIST-37', 'THANA-412', 'UNION-00001', 'ViLL-000002', 'PO-00876', '7610', 'Boro Udas, Sreepur', NULL, 'BD-KHU', 'DIST-37', 'THANA-412', 'UNION-00001', 'ViLL-000002', 'PO-00876', '7610', 'Boro Udas, Sreepur', NULL, 'NID', NULL, '1234567890', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'M1001', '1', 'No', NULL, NULL, NULL, NULL, NULL, NULL, 'Active', NULL, NULL, 'System', '2023-06-26 13:35:26.207', NULL, NULL);





INSERT INTO staging_data
("oid", staging_data_id, process_id, member_id, member_name, mobile, samity_id, samity_name, samity_day, mfi_id, field_officer_id, field_officer_name, total_member, downloaded_by, downloaded_on, created_by, created_on, updated_by, updated_on, process_start_time, process_end_time, status)
VALUES
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e38', '76cea171-ab03-4b04-85b5-a9574136118b-1', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1001', 'POI Akter Rume', '1234567890', '1018-101', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 12, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e39', '76cea171-ab03-4b04-85b5-a9574136118b-2', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1002', 'ABC Akter Rume', '1234567890', '1018-101', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 12, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e31', '76cea171-ab03-4b04-85b5-a9574136118b-3', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1003', 'DEF Akter Rume', '1234567890', '1018-102', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 22, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e32', '76cea171-ab03-4b04-85b5-a9574136118b-4', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1004', 'GHK Akter Rume', '1234567890', '1018-102', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 22, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e33', '76cea171-ab03-4b04-85b5-a9574136118b-5', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1005', 'JKL Akter Rume', '1234567890', '1018-102', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 22, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e34', '76cea171-ab03-4b04-85b5-a9574136118b-6', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1006', 'MNB Akter Rume', '1234567890', '1018-103', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 25, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL),
    ('3c34b8aa-96de-483e-9d13-d25fd21b2e35', '76cea171-ab03-4b04-85b5-a9574136118b-7', '0355360c-7593-4e0e-ad5c-cbd02597b2d5', '1018-101-1007', 'XCV Akter Rume', '1234567890', '1018-103', 'Shyamoli Kallyan Samitee (SKS)', 'Tuesday', 'M1001', 'EMP-0007', 'Abul Kalam', 25, NULL, NULL, '1234', '2023-06-25 18:38:59.442', NULL, NULL, NULL, NULL, NULL)
;



INSERT INTO staging_account_data
("oid", member_id, loan_account_id, product_code, product_name, loan_amount, service_charge, installments, total_due, total_principal_paid, total_principal_remaining, total_service_charge_paid, total_service_charge_remaining, savings_account_id, savings_product_code, savings_product_name, savings_product_type, target_amount, balance, last_deposit_amount, total_deposit, total_withdraw, accrued_interest_amount, deposit_scheme_detail, staging_account_data_id)
VALUES
    ('bcc21d61-cd11-42b8-a194-eddfa931d946', '1018-101-1001', 'AGRO-1018-101-1001-1', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-21'),
    ('bcc21d61-cd11-42b8-a194-eddfa931d947', '1018-101-1001', 'AGRO-1018-101-1001-2', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-31'),
    ('bcc21d61-cd11-42b8-a194-eddfa931d948', '1018-101-1003', 'AGRO-1018-101-1001-3', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-43'),
    ('bcc21d61-cd11-42b8-a194-eddfa931d949', '1018-101-1003', 'AGRO-1018-101-1001-4', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-53'),
    ('bcc21d61-cd11-42b8-a194-eddfa931d941', '1018-101-1004', 'AGRO-1018-101-1001-5', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-64'),
    ('bcc21d61-cd11-42b8-a194-eddfa931d942', '1018-101-1005', 'AGRO-1018-101-1001-6', 'L0002', 'Agrosor', 5000.00, 1206.33, NULL, 483.00, 0.00, 5000.00, 0.00, 1206.33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '76cea171-ab03-4b04-85b5-a9574136118b-75')

;


INSERT INTO collection_staging_data
("oid", collection_staging_data_id, samity_id, staging_data_id, account_type, loan_account_id, savings_account_id, amount, payment_mode, collection_type, submitted_on, submitted_by, is_uploaded, uploaded_on, uploaded_by, approved_on, approved_by, current_version, status, created_by, created_on, updated_by, updated_on)
VALUES
    ('59a86e17-1dda-4178-8652-2c8e2693ff22', 'f821007c-07ff-4a47-991c-eaf2ae699bc3', '1018-101', '76cea171-ab03-4b04-85b5-a9574136118b-1', 'Loan', 'loan-account-id-1', 'AGRO-1018-101-1001-1', 1250.00, 'CASH', 'Regular', NULL, NULL, 'No', NULL, NULL, '2023-06-26 21:38:35.752', 'login-id-1', '1', 'Approved', 'login-id-1', '2023-06-26 20:50:22.284', NULL, NULL);










