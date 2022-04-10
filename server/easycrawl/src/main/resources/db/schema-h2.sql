CREATE TABLE IF NOT EXISTS job(
id varchar(36) NOT NULL COMMENT '主键id',
spider_id varchar(36),
spider_name varchar(64),
spider_table_name varchar(64),
status char(32),
start_time datetime,
end_time datetime,
time_cost bigint(20),
create_time datetime,
modify_time datetime,
PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS configurable_spider(
id varchar(36) NOT NULL COMMENT '主键id',
name varchar(64) NOT NULL,
table_name varchar(64) NOT NULL,
list_regex varchar(512) NOT NULL,
entry_url varchar(1024) NOT NULL,
content_xpath varchar(255),
fields_json text NOT NULL,
content_fields_json text,
is_dynamic int(20),
thread_num int(20),
sleep_time int(20),
retry_times int(20),
retry_sleep_time int(20),
cycle_retry_times int(20),
time_out int(20),
proxy_channel_id varchar(36),
create_time datetime NOT NULL,
modify_time datetime NOT NULL,
PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS proxy_channel(
  id varchar(36) NOT NULL,
  name varchar(64),
  alias varchar(64),
  code varchar(64),
  token varchar(64),
  create_time datetime,
  modify_time datetime,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;