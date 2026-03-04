create table if not exists emps
(
    id          int unsigned auto_increment comment '員工編號'
        primary key,
    username    varchar(20)                                not null comment '帳號',
    password    varchar(100)                               not null comment '密碼雜湊',
    name        varchar(20)                                not null comment '姓名',
    role        tinyint unsigned default '1'               not null comment '角色權限',
    status      tinyint unsigned default '1'               not null comment '啟用狀態(1=啟用,0=停用)',
    entry_date  date                                       not null comment '入職日期',
    create_id   int unsigned                               not null,
    create_time datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id   int unsigned                               not null,
    update_time datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint emp_pk_username
        unique (username)
)
    comment '員工表';

create table if not exists order_detail
(
    id                int unsigned auto_increment comment 'ID'
        primary key,
    order_id          varchar(20)  not null comment '訂單編號',
    product_type      int unsigned not null comment '產品類型  (0=葷飯糰, 1=素飯糰, 2=飲品)',
    product_id        int unsigned not null comment '產品 ID',
    product_price     int unsigned not null comment '單一商品售價快照(未含額外選項)',
    quantity          int unsigned not null comment '訂購數量',
    line_total_amount int unsigned not null comment '項目總金額(包含 order_detail_option)'
)
    comment '訂單明細表';

create table if not exists order_detail_option
(
    order_detail_id int unsigned             not null comment '訂單明細 ID',
    option_id       int unsigned             not null comment '額外選項 ID',
    option_price    int unsigned default '0' not null comment '單一選項售價快照',
    quantity        int unsigned default '1' not null comment '訂購數量',
    primary key (option_id,
                 order_detail_id)
)
    comment '額外選項明細表';

create table if not exists orders
(
    id             int unsigned auto_increment comment 'id'
        primary key,
    order_id       varchar(20)                                not null comment '訂單編號',
    operate_emp_id int unsigned                               not null comment '操作人員 ID',
    amount         int unsigned                               not null comment '訂單金額',
    status         tinyint unsigned default '0'               not null comment '訂單狀態(0=未完成, 1=已完成, 2=已取消)',
    create_id      int unsigned                               not null,
    create_time    datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id      int unsigned                               not null,
    update_time    datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint orders_pk_2
        unique (order_id)
)
    comment '訂單表';

create table if not exists products
(
    id           int unsigned auto_increment comment '產品 id'
        primary key,
    title        varchar(20)                                not null comment '產品名稱',
    product_type tinyint unsigned                           not null comment '產品類型 (0=葷飯糰, 1=素飯糰, 2=飲品)',
    price        int unsigned                               not null comment '價格',
    status       tinyint unsigned default '1'               not null comment '啟用狀態(1=啟用, 0=停用)',
    create_id    int unsigned                               not null,
    create_time  datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id    int unsigned                               not null,
    update_time  datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint product_pk_title
        unique (title)
);

create table if not exists options
(
    id          int unsigned auto_increment comment 'id'
        primary key,
    title       varchar(20)                                not null comment '選項名稱',
    option_type tinyint unsigned                           not null comment '選項類型(0=米飯種類, 1=飯量, 2=辣度, 3=加料)',
    price       int unsigned     default '0'               not null comment '價格',
    status      tinyint unsigned default '1'               not null comment '啟用狀態(1=啟用, 0=停用)',
    create_id   int unsigned                               not null,
    create_time datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id   int unsigned                               not null,
    update_time datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint product_option_pk_2
        unique (title,
                option_type)
)
    comment '產品細節選項表';

create table if not exists product_type_option_type_mapping
(
    product_type_id int unsigned                 not null comment '產品類型 id',
    option_type_id  int unsigned                 not null comment '選項類型 id',
    is_required     tinyint unsigned default '1' not null comment '1=必填, 0=選填',
    primary key (product_type_id,
                 option_type_id)
)
    comment '產品、設定類型中間表';

INSERT INTO product_type_option_type_mapping (product_type_id, option_type_id, is_required) VALUES
                                                                                          (0, 0, 1),  -- 葷食-米飯種類(必填)
                                                                                          (0, 1, 1),  -- 葷食-飯量(必填)
                                                                                          (0, 2, 0), -- 葷食-辣度(選填)
                                                                                          (0, 3, 0), -- 葷食-加料(選填)

                                                                                          (1, 0, 1),  -- 素食-米飯種類(必填)
                                                                                          (1, 1, 1),  -- 素食-飯量(必填)
                                                                                          (1, 2, 0), -- 素食-辣度(選填)
                                                                                          (1, 3, 0), -- 素食-加料(選填)

                                                                                          (2, 4, 1);  -- 飲料-溫度(必填)

