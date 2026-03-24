create table if not exists emps
(
    id          int unsigned auto_increment comment '員工編號'
        primary key,
    username    varchar(20)                                not null comment '帳號',
    password    varchar(100)                               not null comment '密碼雜湊',
    name        varchar(20)                                not null comment '姓名',
    role        tinyint unsigned default '1'               not null comment '職位(0:管理, 1:一般)',
    status      tinyint unsigned default '1'               not null comment '啟用狀態(0:停用, 1:啟用)',
    entry_date  date                                       not null comment '入職日期',
    create_id   int unsigned                               not null comment '建立人員 id',
    create_time datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id   int unsigned                               not null comment '更新人員 id',
    update_time datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint emp_pk_username
        unique (username)
)
    comment '員工表';

create table if not exists products
(
    id           int unsigned auto_increment comment '產品 id'
        primary key,
    title        varchar(20)                                not null comment '產品名稱',
    product_type tinyint unsigned                           not null comment '產品類型(0:葷飯糰, 1:素飯糰, 2:飲品)',
    price        int unsigned                               not null comment '價格',
    status       tinyint unsigned default '1'               not null comment '啟用狀態(0:停用, 1:啟用)',
    create_id    int unsigned                               not null comment '建立人員 id',
    create_time  datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id    int unsigned                               not null comment '更新人員 id',
    update_time  datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint product_pk_title
        unique (title)
)
    comment '產品表';

create table if not exists options
(
    id          int unsigned auto_increment comment 'id'
        primary key,
    title       varchar(20)                                not null comment '選項名稱',
    option_type tinyint unsigned                           not null comment '選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)',
    is_default  tinyint unsigned                           not null comment '是否為選項類別預設值(0:否, 1:是)',
    price       int unsigned     default '0'               not null comment '價格',
    status      tinyint unsigned default '1'               not null comment '啟用狀態(1:啟用, 0:停用)',
    create_id   int unsigned                              not null comment '建立人員 id',
    create_time datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id   int unsigned                              not null comment '更新人員 id',
    update_time datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint options_pk
        unique (title)
)
    comment '產品細節選項表';

create table if not exists orders
(
    id          int unsigned auto_increment comment 'ID'
        primary key,
    order_no    varchar(20)                                not null comment '訂單編號(yyyyMMdd+流水號)',
    pickup_time datetime                                   not null comment '預計取餐時間',
    total       int unsigned                               not null comment '訂單總金額',
    status      tinyint unsigned default '0'               not null comment '訂單狀態(0:製作中, 1:待領取, 2:已領取, 3:已取消)',
    create_id   int unsigned                              not null comment '建立人員 id',
    create_time datetime         default CURRENT_TIMESTAMP not null comment '建立時間',
    update_id   int unsigned                              not null comment '更新人員 id',
    update_time datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間',
    constraint orders_pk_2
        unique (order_no)
)
    comment '訂單表';

create table if not exists order_items
(
    id            int unsigned auto_increment comment '訂單商品 ID'
        primary key,
    order_id      int unsigned not null comment '訂單 ID',
    product_type  int unsigned not null comment '產品類型(0:葷食, 1:素食, 2:飲料)',
    product_title varchar(20)  not null comment '產品名稱',
    product_id    int unsigned not null comment '產品 ID',
    product_price int unsigned not null comment '單一商品售價快照（未含額外選項）',
    quantity      int unsigned not null comment '訂購數量',
    line_total    int unsigned not null comment '項目總金額(含 order_item_option)'
)
    comment '訂單商品明細表';

create table if not exists order_item_options
(
    id            int unsigned auto_increment comment '訂單商品設定明細 id'
        primary key,
    order_item_id int unsigned     not null comment '訂單商品 id',
    option_id     tinyint unsigned not null comment '細節選項 id',
    option_type   tinyint unsigned not null comment '選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)',
    option_title  varchar(20)      not null comment '明細選項名稱',
    option_price  int unsigned     not null comment '明細選項售價',
    quantity      int unsigned     not null comment '數量',
    constraint order_item_option_pk_2
        unique (option_id,
                order_item_id)
)
    comment '訂單商品設定明細表';

