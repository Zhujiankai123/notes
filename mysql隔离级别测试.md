1. 登录mysql: `mysql -uroot -p`

2. 创建数据库test1: `create database test1;`

3. 使用数据库test1: `use test1;`

4. 新开一个窗口，同样登录mysql并使用test1数据库

5. 查看两个窗口的自动提交设置: `show variables like "%autocommit%";`

   > +---------------+-------+
   >
   > | Variable_name | Value |
   >
   > +---------------+-------+
   >
   > | autocommit  | ON  |
   >
   > +---------------+-------+    

   修改为不自动提交: `set autocommit=0;` 并再次查看是否配置成功:

   > +---------------+-------+
   >
   > | Variable_name | Value |
   >
   > +---------------+-------+
   >
   > | autocommit  | OFF  |
   >
   > +---------------+-------+

6. 查看全局/会话的隔离级别: 

   `select @@global.transaction_isolation;`

   > ​	+--------------------------------+
   >
   > | @@global.transaction_isolation |
   >
   > +--------------------------------+
   >
   > | READ-COMMITTED         |
   >
   > +--------------------------------+

   select @@transaction_isolation;

   > ​	+--------------------------------+
   >
   > | @@global.transaction_isolation |
   >
   > +--------------------------------+
   >
   > | READ-COMMITTED         |
   >
   > +--------------------------------+

   * 需要注意的是，如果通过`set blobal transaction isolation level`配置全局会话级别，只会影响到新启动的会话，当前已经存在的会话不会受到任何影响。当前启动的会话只会受到session的隔离级别影响。
   * 即，如果我们配置`set blobal transaction isolation level read uncommitted;`的话，我们通过`session start`或者是`begin`命令新启动的任何事务，都还是`read committed`的，并没有受到全局配置的影响。只有我们新开一个窗口(session)登录mysql，这时才会成功使用到`read uncommitted`
   * 所以，当我们作为管理员身份时，可以使用global保证各机器新启动的会话都是指定隔离级别的。而由于全局配置无法即时反映到当前会话中，因此在测试时最好是单独配置各个会话的隔离级别。

7. 四种隔离级别，及各种不同的隔离级别可能会导致的问题:

|                                              | 脏读 | 不可重复读 | 幻读 | [解释]                                                       |
| -------------------------------------------- | ---- | ---------- | ---- | ------------------------------------------------------------ |
| Read Uncommitted <br />(读未提交/RU)         | ✔️    | ✔️          | ✔️    | 在当前session的事务里可以读取到其他事务中尚未提交的数据      |
| Read Committed<br />(读已提交/不可重复读/RC) |      | ✔️          | ✔️    | 在当前session的事务里只可以读取到其他事务中已提交的数据。    |
| Repeatable Read<br />(可重复读/RR)           |      |            | ✔️    | 通过MVCC(多版本并发控制/multi version concurrent control)<br />保证了各个session中读取到的数据是不会受到其他session中的事务影响的 |
| Serializable<br />(序列化)                   |      |            |      | 通过锁机制保证了只有一个事务在读写数据，<br />其他事务则处于阻塞状态。 |



7-1、脏读(dirty read)

处于read uncommitted状态时，其他事务中的任何操作，即时未做commit提交，也会影响到当前事务的读取结果。即**读取到了其他事务中未尚提交的数据**。

```sql
#创建数据表并插入数据
drop table if exists tbl;
create table tbl(id int primary key, name varchar(10));
insert into tbl values(1,'aaa'),(2,"bbb"),(3,"ccc");
commit;

#先查看一下数据
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在两个session中分别配置为读未提交,并开启事务: 
set session transaction isolation level read uncommitted;
begin;

#在session-1中修改数据,但不做提交(commit):
update tbl set name = "aaa123" where id=1;

#在session-1中查看数据:
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#在session-2中查看数据(脏读):
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#在两个session中结束事务:
commit;
```

7-2、不可重复读(unrepeatable read)

当处于read committed级别时，当前事务可以读取到其他事务中已经做了commit的数据。若某一时刻我们执行了一条查询语句，随后另外一个事务修改了数据然后做了提交，这时候我们再次执行同样的查询语句会发现读取到的数据和上一次查询到的内容是不一样的，也就是无法重复读取到上一次的查询结果。

```sql
#创建数据表并插入数据
drop table if exists tbl;
create table tbl(id int primary key, name varchar(10));
insert into tbl values(1,'aaa'),(2,"bbb"),(3,"ccc");
commit;

#先查看一下数据
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在两个session中分别配置为读已提交,并开启事务: 
set session transaction isolation level read committed;
begin;

#在session-1中修改数据,但不做提交(commit):
update tbl set name = "aaa123" where id=1;

#在session-1中查询结果:
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#在session-2中查询结果(解决了脏读问题):
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在session-1中进行提交:
commit;

#在session-2中查询结果:
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#结束事务:
commit;

# 读取到了其他事务中提交的结果，或者换句话说此时由于其他事务的提交，导致我们当前事务的前后两次查询结果不一致产生了不可重复读的问题
# 可重复读: 同一条查询语句只要当前事务没有修改,那么任何时刻的查询都应该是返回同样的结果的。
# 不可重复读: 同一条查询语句,虽然在当前事务中对数据没有做修改，但是在不同时候执行时返回了不同的结果)
```

7-3、幻读(phantom read)

当处于repeatable read隔离级别是可以解决不可重复读问题，但是仍然存在幻读的情况。不可重复读指的是由于其他事务的提交导致当前事务前后两次执行查询语句时的返回结果内容不一致，而幻读则是说由于其他事务对数据的增删操作，导致当前事务的前后查询结果中出现某些原本不存在的数据，抑或是某些数据突然消失的情况。

实际上，在mysql中通过多版本并发控制(mvcc)可以解决这种最常见的幻读问题。解决方法即通过区分快照读(select)和当前读(update、insert等)，只有当前读操作的时候才会用到最新的数据，而一般的select等快照读操作都是对事务开始前的数据进行读取操作。

```sql
#创建数据表并插入数据
drop table if exists tbl;
create table tbl(id int primary key, name varchar(10));
insert into tbl values(1,'aaa'),(2,"bbb"),(3,"ccc");
commit;

#先查看一下数据
select * from tbl;

/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在两个session中分别配置为可重复读,并开启事务: 
set session transaction isolation level repeatable read;
begin;

#在session-1中插入一条新的数据,但不做提交(commit):
insert into tbl values(4,"ddd");

#在session-1中查询结果:
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
|  4 | ddd  |
+----+------+
*/

#在session-2中查询结果:
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在session-1中执行提交:
commit;

#在session-2中再次查询结果:
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#尝试在session-2中新增一条id为4的记录
#发生了错误，提示已经有id为4的数据了，但是上面的查询中又看不大这条数据，即出现了幻读
insert into tbl values(4,"ggg");
/*
ERROR 1062 (23000): Duplicate entry '4' for key 'PRIMARY'
*/

#尝试在session-2中修改这条id为4的记录
#此时我们发现虽然之前查询时我们找不到这个id为4的记录，但是此时却能正常修改
update tbl set name="ggg" where id=4;
/*
Rows matched: 1  Changed: 1  Warnings: 0
*/

#再次查看，发现出现了一条id为4的记录
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
|  4 | ggg  |
+----+------+
*/


#即我们在session-2中通过select快照读的方式无法查看到其他其他事务中insert等数据操作，但是通过update等方式我们发现这个新加入的数据是存在的。
```

我们在看看repeatable read解决不可重复读的测试情况:

```sql
#创建数据表并插入数据
drop table if exists tbl;
create table tbl(id int primary key, name varchar(10));
insert into tbl values(1,'aaa'),(2,"bbb"),(3,"ccc");
commit;

#先查看一下数据
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在两个session中分别配置为可重复读,并开启事务: 
set session transaction isolation level repeatable read;
begin;

#在session-1中修改数据,暂时不做提交(commit;):
update tbl set name="aaa123" where id=1;

#在session-1中查询结果:
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#在session-2中查询结果:
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#在session-1将事务中对数据的修改操作进行提交
commit;

#在session-1中查询结果:
select * from tbl;
/*
+----+--------+
| id | name   |
+----+--------+
|  1 | aaa123 |
|  2 | bbb    |
|  3 | ccc    |
+----+--------+
*/

#在session-2中查询结果:
select * from tbl;
/*
+----+------+
| id | name |
+----+------+
|  1 | aaa  |
|  2 | bbb  |
|  3 | ccc  |
+----+------+
*/

#即session-2中的数据不会因为session-1中的commit操作而发生内容的改变，
#但是结合上一个例子可以知道，如果是插入删除这种导致数据量发生改变的操作，是会使得查询结果发生不一致的情况出现的。
```

  

8. serializable(序列化)

   序列化是隔离等级最高的一个级别，这种级别下，某一事务对数据表的操作会直接对其上锁，使得其他事务在访问该数据表时处于阻塞状态，必须等事务结束后，阻塞状态才会取消，进而执行对应的其余操作。

