alter table co_author_publication
    drop foreign key FKgslksmu4s0vxdhcbhvy4a95su;
alter table co_author_publication
    drop foreign key FKbpds1uhqjvisdo65s8vur409l;
alter table paper
    drop foreign key FKlxgkbiaorjje4w9kjy44og249;
alter table publication
    drop foreign key FK1xggwc0yt6qxs5uevpipq0svj;
alter table user_affiliation
    drop foreign key FKmeu7ac9b8narjh42h5dpkj00g;
alter table user_affiliation
    drop foreign key FKijj70sojvyp87q8dhyyc1lyk5;
alter table user_contract_usr
    drop foreign key FKkyh6df0mtdi0onqruig6dyw4g;
alter table user_contract_usr
    drop foreign key FKixjnqsha16aorupveug3hv6xk;
alter table user_role
    drop foreign key FKfpm8swft53ulq2hl11yplpr5;

drop table if exists affiliation;
drop table if exists co_author;
drop table if exists co_author_publication;
drop table if exists paper;
drop table if exists publication;
drop table if exists user_affiliation;
drop table if exists user_contract_usr;
drop table if exists user_role;
drop table if exists user_contract;
drop table if exists usr;

create table affiliation
(
    affiliation_id     bigint not null auto_increment,
    city               varchar(255),
    country            varchar(255),
    organization_full  varchar(255),
    organization_short varchar(255),
    primary key (affiliation_id)
);

create table co_author
(
    co_author_id          bigint not null auto_increment,
    co_author_affiliation varchar(255),
    co_author_city        varchar(255),
    co_author_name        varchar(255),
    primary key (co_author_id)
);

create table co_author_publication
(
    publication_id bigint,
    co_author_id   bigint not null,
    primary key (co_author_id)
);

create table paper
(
    paper_id         bigint not null auto_increment,
    paper_filename   varchar(255),
    paper_name       varchar(255),
    paper_permission varchar(255),
    paper_review     varchar(255),
    state            varchar(255),
    user_id          bigint,
    primary key (paper_id)
);

create table publication
(
    publication_id   bigint not null auto_increment,
    accept_thesis    bit    not null,
    filename         varchar(255),
    permission       varchar(255),
    poster           varchar(255),
    publ_type        varchar(255),
    publication_name varchar(255),
    publ_section     varchar(255),
    user_id          bigint,
    primary key (publication_id)
);

create table user_affiliation
(
    affiliation_id bigint not null,
    user_id        bigint not null,
    primary key (affiliation_id, user_id)
);

create table user_contract_usr
(
    user_contractn_id bigint not null,
    usr_id            bigint not null,
    primary key (user_contractn_id)
);

create table user_role
(
    user_id bigint not null,
    roles   varchar(255)
);

create table user_contract
(
    id                bigint not null auto_increment,
    check_filename    varchar(255),
    contract_filename varchar(255),
    contract_type     varchar(255),
    invoice_filename  varchar(255),
    primary key (id)
);

create table usr
(
    id              bigint not null auto_increment,
    activation_code varchar(255),
    active          bit    not null,
    degree          varchar(255),
    username        varchar(255),
    first_name      varchar(255),
    last_name       varchar(255),
    middle_name     varchar(255),
    password        varchar(255),
    position        varchar(255),
    telephone       varchar(255),
    young           bit    not null,
    primary key (id)
);

alter table user_affiliation
    add constraint UK_fyj7c5rcmd0oaeugrv1lwnaxn unique (user_id);
alter table co_author_publication
    add constraint FKgslksmu4s0vxdhcbhvy4a95su foreign key (publication_id) references publication (publication_id);
alter table co_author_publication
    add constraint FKbpds1uhqjvisdo65s8vur409l foreign key (co_author_id) references co_author (co_author_id);
alter table paper
    add constraint FKlxgkbiaorjje4w9kjy44og249 foreign key (user_id) references usr (id);
alter table publication
    add constraint FK1xggwc0yt6qxs5uevpipq0svj foreign key (user_id) references usr (id);
alter table user_affiliation
    add constraint FKmeu7ac9b8narjh42h5dpkj00g foreign key (affiliation_id) references affiliation (affiliation_id);
alter table user_affiliation
    add constraint FKijj70sojvyp87q8dhyyc1lyk5 foreign key (user_id) references usr (id);
alter table user_contract_usr
    add constraint FKkyh6df0mtdi0onqruig6dyw4g foreign key (user_contractn_id) references user_contract (id);
alter table user_contract_usr
    add constraint FKixjnqsha16aorupveug3hv6xk foreign key (usr_id) references usr (id);
alter table user_role
    add constraint FKfpm8swft53ulq2hl11yplpr5 foreign key (user_id) references usr (id);