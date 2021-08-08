# JPA正常连接并使用数据库，需要保证以下三个包正常安装：
1. JPA
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
```
2. mysql-connector
```
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.23</version>
		</dependency>
```
3. JDBC
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jdbc</artifactId>
		</dependency>
```

# 项目分层为：
1. 控制器层 Controller
2. 服务层 Service
3. 资源层 Repository
4. Model层严格来说不能算是一个层，它只是对业务对象/数据表结构的描述

* 按照标准写法，层与层之间是应该通过接口进行通信的，
* 但是由于项目逻辑、个模块负责的功能等较为明确，后续也不会对业务等做太多修改，
* 本次将直接使用Autowired注入类对象，而不再使用接口的方式进行通信

# 数据库和JPA的连接
数据库和JPA顺利连接并执行JPA命令，需要保证：
1. 数据库、JPA、JDBC三个包都安装完成
2. 配置文件中已经对数据库的url、username、password进行了配置
3. 配置文件中对JPA的执行方式进行了配置( jpa.hibernate.ddl-auto = xxx)

# 数据库设计等问题

## 数据库设计一般遵循：
1. 根据业务需求，提取对应的业务对象作为实体表。
2. 根据业务需求，配置表与表之间的关系。
  一般表与表之间的关系分为：
  1) 1对1 如用户表与用户身份证号表(一个用户对应一个身份证号，一个身份证号对应一个用户)
    1对1一般是可以合并成1个表的，而拆分为两个表，一般是出于字段太多而进行垂直分表(切分字段到多个表中)，
  或者是将字段归类到不同的数据表中便于统一管理。
  2) 1对多 如班级表与学生表(一个班级对应很多学生，而一个学生只会对应一个班级)
    这是最常见的表与表之间的关系
  3) 多对多 如老师表与学生表(一个老师可以对应多个学生，而一个学生也会对应多个老师)
    这种表一般需要第三张表来表示其余两张表之间的对应关系。
    就像上面举的老师学生的例子中，就需要班级表作为中间表，例如老师A1带的班级有C1、C2、C3班，而学生S1所在班级为C1，
    则根据C1表就可以建立起老师与学生之间的关系了
    第三张表可以是有具体含义的，如上面的班级表，这个表中也可以有其他信息字段
    第三张表也可以是没有具体含义的，只是记录老师与学生关系对照信息。
    第三张表是选择有具体含义的还是没有具体含义的，需要根据个人需要及业务逻辑进行选择。
3. 细节调整即配置
   字段限制、长度、小数点、唯一索引等细化工作
4. 优化
    一般能在数据库设计上进行的优化，基本上是一些拆分表(建议单表记录5000w条以内)、拆分字段等、建立索引等
    更多的优化只要是在查询方式上，例如查询上使用like、<>等会使得索引失效，而大大降低了查询效率
    其他优化方法还包括使用缓存(如redis等键值对数据库)等
## OneToMany/ManyToOne
### 未配置关联关系时
一对多关系中，正常来说是不需要第三张表的，但是如果这两个表之间没有指定关联外键，那么JPA会自动生成第三张表来描述这两个表之间的关系
如Banner表中有ID字段，而BannerItem表中也有ID字段，如果没有配置外键关系的话，系统是无法获知这两个表要怎么关联，
此时他会创建第三张表BannerItems，通过banner_id字段与banner_items_id两个字段，处理Banner和BannerItem这两张表之间的关联问题
``` java
@Entity
public class Banner {
    @Id
    private Integer id;

    private String img;

    @OneToMany
    List<BannerItem> bannerItems;
}

@Entity
public class BannerItem {
    @Id
    private Long id;
}
//jpa执行结果：
// Hibernate: alter table banner_banner_items add constraint FK7gxr0vkbftvjymn4en272kusm foreign key (banner_items_id) references banner_item (id)
// Hibernate: alter table banner_banner_items add constraint FKhpthkb0cememsn6l3t2num9tn foreign key (banner_id) references banner (id)
```
### 配置关联关系
在多方添加用于管理的id:
``` java
@Entity
public class BannerItem {
    @Id
    private Long id;
    private Long bannerId;
}

```
在一方添加JoinColumn:
``` java
@Entity
public class Banner {
    @Id
    private Integer id;

    private String img;

    @OneToMany
    @JoinColumn(name = "bannerId")
    List<BannerItem> bannerItems;
}
```

* 如果Hibernate使用了create-drop的方式，按上述配置其实会报错的，而且虽然报错了但是表依然能创建成功
这个错误实际是因为create-drop的方式，在建表之前会将原始表先删除，在删表时检测出存在外键约束，因此会报错。
* 这也就是为什么不建议JPA创建数据表时直接创建物理外键的原因
* 不用物理外键时，性能会比较好，也容易维护
* 使用物理外键时，会存在强约束关系，有利于保持数据的一致性
* 使用还是不使用物理外键没有标准的结论，一般来说互联网项目是不建议使用物理外键的(数据变动较大、较频繁)

### 显示sql语句及sql格式化
需要注意的是，ddl配置是在`jpa.hibernate.ddl-auto`下
而显示sql语句及sql语句格式化是在`jpa.properties.hibernate`下的，而且show_format和format_sql中间是下划线而不是横线
```
spring
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        show_sql: true
        format_sql: true
```
其实在jpa.hibernate下也有显示sql语句的配置：`jpa.hibernate.show-sql = true`,但是要注意这个中间是横线而不是下划线
### 导航属性
一般JoinColumn不称作关联属性，而叫作导航属性

### 懒加载和急加载
#### 懒加载
当使用controller->service->repository(->model)访问一对多的一端对象时，默认使用懒加载的模式(fetch = FetchType.LAZY))
懒加载模式下，将不会主动对多端数据进行访问
懒加载模式下，只有调用了导航属性中的数据时才会对这部分数据进行加载
#### 急加载
在JoinColumn中，加入fetch = FetchType.EAGER

### 一对多的三种形式

一对多一般分为:
1. 单向一对多
   1) 导航属性配置在一方：一方 --> 多方 (OneToMany)
   2) 导航属性配置在多方：多方 --> 一方 (ManyToOne)
2. 双向一对多   
    一方和多方都有导航属性：一方 <--> 多方 (OneToMany & ManyTo One)
    * 双向一对多中，一般称呼多方为关系维护端，称呼一方为关系被维护端  


#### 单向一对多
单向一对多一般就是看是从哪方到哪一方  
例如配置了从banner到多个bannerItem的一对多关系以后，就能够通过访问banner同时访问到全部的bannerItem  
同样的道理，如果配置的是从bannerItem到banner的一对多关系，那么访问bannerItem时就能直接获取到它所属的banner的信息  

但是反过来，如果配置了从banner到bannerItem的一对多，此时访问bannerItem是无法获取到banner的信息的
从bannerItem到banner的一对多也是同样的道理。  

如果需要从任意方访问到对方，那么就需要配置双向一对多  

#### 双向一对多
注意事项：
1. 要明确关系维护端和关系被维护端，一般在一对多关系中，默认多方为维护端，一方为被维护端。  
在一方的bannerItems成员属性上打上@OneToMany注解  
在多方的banner成员属性上打上@ManyToOne注解  
2. 按照一般处理方式，要在关系维护端(多方)的关联字段上打@JoinColumn，并配置外键字段
    ``` java
    //BannerItem.java
    @ManyToOne
    @JoinColumn(name = "bannerId")
    private Banner banner;
    ```
3. 此外，按照一般处理方式，还要在被维护方的OneToMany注解中配置mappedBy,内容为对应关系维护端(bannerItem)中导航属性的名字：
    ```java
        //Banner.java
        @OneToMany(mappedBy = "banner", fetch = FetchType.EAGER)
        private List<BannerItem> banner_item;
4. DuplicateMappingException:   
双向一对多时，外键(外键一般是被控方的id标识，如bannerId)会由系统自动生成，此时如果手动在多方配置了外键,如在BannerItem中配置` private Long bannerId;`，会提示映射字段重复的异常。  
5. 正常情况下，如果不使用JoinColumn来配置外键，也是可以成功运行的。但是由于没有指明外键，无法明确两个表之间的对应关系，此时jpa是会生成第三张表来表示banner和bannerItem两张表的对应关系的。   ```
6. 实际上，外键字段bannerId是可以不显式声明的，甚至是JoinColumn字段都可以不写(但是被维护端的mappedBy还是要有的)：
    ```java
    public class BannerItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id; // 主键
    
        //private Long bannerId; // 外键,指明该item所属的banner的id
    
        private String img;
        private String name;
        private String type;
    
        @ManyToOne
        //@JoinColumn(name = "bannerId")
        private Banner banner;
    }
    ```
    这时系统依然是会自动生成外键，同时由于被维护端存在mappedBy配置，也只会生成两张表而不会生成三张表。
    
    但是很多时候，我们是需要显式声明外键字段的，因为其实我们实际使用中，jpa并不会用来生成数据表，而多用于数据表的操作，如果字段隐式显示，那么会不利于我们观察字段及相关操作。
7. 强制使用显式声明外键字段：  
    配置JoinColumn注解的updatable = false,insertable = false  
    以下为一个完整的一对多关系配置示例
    ``` java
    // BannerItem.java
    @Entity
    @Table(name = "banner_item")
    @Getter
    public class BannerItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id; // 主键


        private Long bannerId; // 外键,指明该item所属的banner的id


        private String img;
        private String name;
        private String type;
    
        @ManyToOne
        @JoinColumn(name = "bannerId", updatable = false, insertable = false)
        private Banner banner;
    }
    
    // Banner.java
    @Entity
    @Table(name = "banner")
    @Getter
    public class Banner {
        @Id
        private Long id;
    
        private String name;
        private String title;
        private String img;
    
        @OneToMany(mappedBy = "banner", fetch = FetchType.EAGER)
        private List<BannerItem> banner_item;
    }
    ```

## ManyToMany
* 多对多与一对多一样，也是包括三种方式：
1. 单向多对多 A -> B 或 B -> A
2.  双向多对多 A <-> B



### 单向多对多
在某张表上打上@ManyToMany，在另一张表不打该注解，即配置了单向多对多。  
一般来说JPA会根据默认规则配置第三张表及相关外键的名称  
也可以通过@JoinTable(name = "xxx", JoinColumns= @JoinColumn(name="xxx"),inverseJoinColumns= @JoinColumn(name="xxx"))来自定义第三张表的名称及外键字段
* joinColumns是可以使用多主键的，但一般来说不建议使用多外键、联合外键、联合主键等。

### 多向多对多
* 需要注意的是，同一对多的配置类似，如果我们不添加JoinColumn的话，他会无法确定两个表是以哪个字段作为外键进行关联的。  
此时，同一对多未配置JoinColumn会生成三张表是类似的，是会自动生成四张表的，两张外键配置表，两张原始表。
* 双向多对多的关系维护方和关系被维护方是没有固定限制的，一般按照业务要求处理即可。
* 同双向一对多一样，需要在被维护端配置mappedBy=维护端的导航属性的名称

### 双向关系中维护端和被维护端的讨论
与一对多种维护端与被维护端是固定的不同，在双向多对多中，关系的维护端和被维护端是可以任意选择的，维护和被维护一般是不具有特别明确的意义的。  
但是要注意的是无论谁是维护端或被维护端，他们之间的关联都是外键关联的，此时进行删除操作时可能会涉及到级联的问题。  

### 物理外键
sql在执行删除操作时，会先扫描当前数据库的全部外键配置信息，如果发现需要删除的表存在外键约束，将会拒绝执行删除命令  
因此，一般互联网项目中，很少使用物理外键。
配置不生成物理外键，一般又以下几种方法： 
1.  ```java
    @ManyToMany(mappedBy = "spuList")
    @org.hibernate.annotations.ForeignKey(name = "null")
    List<Theme> themeList;
    ```
2.  ```java
    @ManyToMany
    @JoinTable(name = "theme_spu",
            joinColumns = @JoinColumn(name = "theme_id"),
            inverseJoinColumns = @JoinColumn(name = "spu_id"),
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    List<Spu> spuList;
    ```
    这种方法存在bug，目前还无法正常使用
3. 在配置文件中，配置自定义sql-dialect(sql方言)
4. 不使用通过实体模型的方式创建表，而改用可视化工具、sql-ddl建表语句等

### 逻辑外键
物理外键属于强约束，因此一般互联网项目很少使用物理外键。
逻辑外键一般指的就是通过join/inner join/left join/right join等进行关联操作时的关联字段。  
进行关联的两个表之间没有强约束关系，只是通过字段值相等的关系建立联系。

# 项目整体编写顺序
一般来说，应当先编写O端CMS(ToO)，再编写C端应用程序。
* CMS在很多语言中如PHP是不分前后端的。由于现在前后端分离为主，因此CMS端也应当分开前后端。

1. CMS后端 java spring + MyBatis
2. CMS前端 VUE
3. ToC后端 java spring + JPA 
4. ToC前端 微信小程序

# 不使用JPA生成数据表
我们将不使用JPA的ORM来生成数据表，而是使用可视化管理工具、ddl等创建数据表。  
我们将不会手动编写ORM模型类，而是通过idea的持久化工具来反向生成数据表模型类。  
配置文件中，ddl-auto配置为none：`spring.jpa.hibernate.ddl-auto = none `

idea中开启Persistence模块的方法:
1) File -> Project Structure -> Project Settings -> Modules -> 添加Hibernate -> 添加hibernate.cfg.xml  
    也可以通过File -> Project Structure -> Project Settings -> Modules -> 添加JPA -> 配置default JPA provider ->hibernate来开启Persistence模块 
2) View -> Tool Windows -> 打开Persistence
3) idea中打开数据库，并连接到对应数据库
4) 在Persistence中右键，选择Generate Persistence Mapping -> By DataBase Scheme
5) 在打开的窗口中选择所在的数据表，选择输出模型类到那个package下，并勾选需要输出的表
6) 注意，自动生成的表会包括get、set等方法，并重写equals、tostring等，并配置了表名称，此外默认字段类型也不一定满足要求，可根据需求自行增删处理。

# 配置json序列化的格式
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE/CAMEL_CASE
    serialization: 
      WRITE_DATES_AS_TIMESTAMPS: true

# 字段扩展的几种方式
1. 通过1对多，将需要扩展的字段放到其他的表里
2. 横向添加字段一般是较为繁琐的，而纵向添加记录则是很方便的。因此可以将字段扩展改为记录扩展。
    即，例如号码a需要新增b1、b2属性，如果直接扩展b1、b2字段会带来很多问题，而如果创建一个属性字段b，新增b1和b2两条记录，也相当于对字段做了扩展
3. 将扩展字段统一提取到配置表里，通过key、config、tbname进行关联(一对多)，得到新增的字段
4. 相同类型的，也可以通过分隔符作为连接方式，例如产品a有规格b1、b2、b3，那么可以通过b1#b2#b3连合并三个规格

# 静态资源托管的几种方式
1. nginx代理服务器托管
2. spring托管(thymeleaf:spring-boot-starter-thymeleaf<)
3. oss等对象存储服务，包括阿里oss、七牛、码云、git等

# 多个spu接口
spu接口包括了spu详情接口与spu概要接口，需要分别进行处理，减少无意义的数据库查询。
概要信息主要是指的瀑布流中各个产品的图片、价格(大致价格)、标题、副标题等内容
详情信息指的是某个产品的详细信息，包括该产品的产品轮播展示图、sku信息、spu详情内容(一般为多图展示)等
* 概要信息由于是用在瀑布流中，因此是需要返回一组数据的，而且由于引入了分组加载机制，所以在处理时还要通过排序等方法保证每次只返回指定数量的数据。
* spu详情页面是指的某一个产品的详情信息，因此只返回一条记录，而且这条记录是spu表与spuimg、sku等多个表进行一对多关联的结果。

* 数据表之间的关系可能会很复杂，一般采用延迟思考的方式，  
例如设计spu详情页接口时，spu与spu_img、detail_img、sku有一对多的关系，而sku可能还会与规格等具有一对多的关系。  
在这个时候就把所有关系考虑完整会很困难。而事实上我们在做spu接口时，可以不去关注sku与其他表的关系，只关注本层的信息。  
导航关系的配置原则是需要用到的时候再做配置，而不是一下子把所有的关系考虑完整。

# VO
直接通过一对多等获得的数据，会关联很多内容，一般要使用VO（View Object）来进行简化处理

# BO
由Service业务层到Controller控制器层之间的，或存在于业务层的数据对象

# JPA的多种查询方式
[ref](https://docs.spring.io/spring-data/jpa/docs/2.2.x/reference/html/#preface)

## 方式1，通过JPARepository命名方法
<table class="tableblock frame-all grid-all fit-content">
<caption class="title">Table 3. Supported keywords inside method names</caption>
<colgroup>
<col>
<col>
<col>
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Keyword</th>
<th class="tableblock halign-left valign-top">Sample</th>
<th class="tableblock halign-left valign-top">JPQL snippet</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Distinct</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findDistinctByLastnameAndFirstname</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>select distinct …​ where x.lastname = ?1 and x.firstname = ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>And</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameAndFirstname</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname = ?1 and x.firstname = ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Or</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameOrFirstname</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname = ?1 or x.firstname = ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Is</code>, <code>Equals</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstname</code>,<code>findByFirstnameIs</code>,<code>findByFirstnameEquals</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname = ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Between</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateBetween</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate between ?1 and ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeLessThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &lt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeLessThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &lt;= ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeGreaterThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeGreaterThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &gt;= ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>After</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateAfter</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate &gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Before</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateBefore</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate &lt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsNull</code>, <code>Null</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAge(Is)Null</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age is null</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsNotNull</code>, <code>NotNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAge(Is)NotNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age not null</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Like</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameNotLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname not like ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>StartingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameStartingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound with appended <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>EndingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameEndingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound with prepended <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Containing</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameContaining</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound wrapped in <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>OrderBy</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeOrderByLastnameDesc</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age = ?1 order by x.lastname desc</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Not</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameNot</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname &lt;&gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>In</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeIn(Collection&lt;Age&gt; ages)</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age in ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotIn</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeNotIn(Collection&lt;Age&gt; ages)</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age not in ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>True</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByActiveTrue()</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.active = true</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>False</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByActiveFalse()</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.active = false</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IgnoreCase</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameIgnoreCase</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where UPPER(x.firstname) = UPPER(?1)</code></p></td>
</tr>
</tbody>
</table>

<table class="tableblock frame-all grid-all stretch">
<caption class="title">Table 8. Query keywords</caption>
<colgroup>
<col style="width: 25%;">
<col style="width: 75%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Logical keyword</th>
<th class="tableblock halign-left valign-top">Keyword expressions</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>AND</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>And</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>OR</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Or</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>AFTER</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>After</code>, <code>IsAfter</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>BEFORE</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Before</code>, <code>IsBefore</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>CONTAINING</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Containing</code>, <code>IsContaining</code>, <code>Contains</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>BETWEEN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Between</code>, <code>IsBetween</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ENDING_WITH</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>EndingWith</code>, <code>IsEndingWith</code>, <code>EndsWith</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>EXISTS</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Exists</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>FALSE</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>False</code>, <code>IsFalse</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GREATER_THAN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThan</code>, <code>IsGreaterThan</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GREATER_THAN_EQUALS</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThanEqual</code>, <code>IsGreaterThanEqual</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>In</code>, <code>IsIn</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IS</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Is</code>, <code>Equals</code>, (or no keyword)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IS_EMPTY</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsEmpty</code>, <code>Empty</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IS_NOT_EMPTY</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsNotEmpty</code>, <code>NotEmpty</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IS_NOT_NULL</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotNull</code>, <code>IsNotNull</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IS_NULL</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Null</code>, <code>IsNull</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LESS_THAN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThan</code>, <code>IsLessThan</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LESS_THAN_EQUAL</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThanEqual</code>, <code>IsLessThanEqual</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LIKE</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Like</code>, <code>IsLike</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NEAR</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Near</code>, <code>IsNear</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NOT</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Not</code>, <code>IsNot</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NOT_IN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotIn</code>, <code>IsNotIn</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NOT_LIKE</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotLike</code>, <code>IsNotLike</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>REGEX</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Regex</code>, <code>MatchesRegex</code>, <code>Matches</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>STARTING_WITH</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>StartingWith</code>, <code>IsStartingWith</code>, <code>StartsWith</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>TRUE</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>True</code>, <code>IsTrue</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>WITHIN</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Within</code>, <code>IsWithin</code></p></td>
</tr>
</tbody>
</table>

## 方法2，通过手写sql查询语句
```java
public interface GoodsRepository extends JpaRepository<Goods, Long> {
    @Query(value = "select * from goods g where g.price between :startPrice and :endPrice", nativeQuery = true)
    List<Goods> findByPriceBetween(Double startPrice, Double endPrice);
}
```

# message消息
有些spring的message消息，例如内置校验类中校验失败的返回消息，可以通过配置文件直接配置，而不必通过硬编码的方式直接在校验参数中输入message内容。
* 需要注意的是，其他的配置文件，文件名可以自定义，而message配置文件的文件名必须为`ValidationMessages.properties`。  
与其他配置文件一样,在配置文件中直接写入配置内容即可(需要注意的是配置文件中不需要在值上添加引号)，也可以使用{min}、{max}等将校验参数传递进来。  
调用时，与其他配置文件一样，也是使用{}花括号进行引用。
* 这种方式修改message后，返回的异常内容可能会不是自己想要的格式，这时可以在全局异常处理中通过`e.getMessage()`方法进行调整。


# SKU、SPU、SPECS关系配置
## specs_key & specs_val的一对多关系
spec_key: 规格名称  
spec_val: 规格值  
例如，颜色是一个规格名，红色、绿色是颜色规格的不同规格值

## spu & sku的一对多关系
spu: 产品  
sku: 单品  
例如，iphone11手机是一个产品,它会对应很多不同的单品，如国行64g灰色iphone11手机、国行128g红色手机等

## spu & spec_key的多对多关系
例如，iphone11手机有内存大小、屏幕大小等不同的规格(名)，而内存大小、屏幕尺寸不是只有iphone11的独有规格，它可能也属于小米note8等其他产品

## sku & spec_val的多对多关系
例如，国行64g灰色iphone11手机有多个规格值(国行、64g、灰色)，而这些规格值并不是只属于这个单品的属性值，其他的单品也可能存在这个属性值。


# json序列化问题
在model实体中，数据类型只能为基础数据类型，不能为自定义类型。因此若某个字段为其他专用类型(如规格Specs)，会序列化为字符串为不是我们想要的json的形式:
``` java
// 已隐藏无关字段
public class Sku extends BaseEntity{
/** specs内容:
 {
	"key": "颜色",
	"value": "金属灰",
	"key_id": 1,
	"value_id": 45
} 
*/
    @Id
    private Long id;
    private String specs;
}

response:    
"specs": "[{\"key\": \"颜色\", \"value\": \"金属灰\", \"key_id\": 1, \"value_id\": 45}, {\"key\": \"图案\", \"value\": \"七龙珠\", \"key_id\": 3, \"value_id\": 9}, {\"key\": \"尺码\", \"value\": \"小号 S\", \"key_id\": 4, \"value_id\": 14}]",

```


## 解决思路1: 使用返回Object类型  
使用Object首先会遇到的问题是Object的序列化反序列化会涉及到装箱问题，将严重拖慢运行速度。  
其次是在当前的spring中，Object类型不会标红色波浪线，但是编译时无法通过:property mapping has wrong number of columns: com.zjk.spring.model.Sku.specs type: object

 
## 解决思路2: 返回一个自定义数据类型  
由于是非导航属性，因此如果某字段配置为自定义属性时，会直接报错提示返回的是一个非基础数据类型
> 除了导航属性以外，其他属性(字段)是不可以使用：  
> 非基础数据类型、实体('Basic' attribute type should not be 'Persistence Entity' less...)  
> List等其他类型('Basic' attribute type should not be a container less... )  
> Map等其他类型('Basic' attribute type should not be a map less... (⌘F1) )  
  
  
## 解决思路3: 通过private属性的get和set方法来获取  
当一个属性为private时，在读取和写入的时候，会通过其get和set方法进行处理，因此我们可以在这里进行干预，手动执行序列化和反序列化。 
创建一个jackson序列化parser，并通过mapper进行格式化，返回格式化后的内容：
``` java
public class Sku extends BaseEntity{
    @Id
    private Long id;
    private String specs;
    public List<Specs> getSpecs() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jsonParser = new JsonFactory().createParser(specs);
        List<Specs> listSpecs = mapper.readValue(jsonParser, new TypeReference<List<Specs>>() {
        });

        return listSpecs;
    }
}
测试结果:   
GET: 127.0.0.1:8081/v1/test/sku_to_json_test1?id=999
RESPONSE: 
    "specs": [
        {
            "key": "颜色",
            "value": "金属灰",
            "key_id": 1,
            "value_id": 45
        },
        {
            "key": "图案",
            "value": "七龙珠",
            "key_id": 3,
            "value_id": 9
        },
        {
            "key": "尺码",
            "value": "小号 S",
            "key_id": 4,
            "value_id": 14
        }
    ],

GET: 127.0.0.1:8081/v1/test/sku_to_json_test1?id=9999
RESPONSE: 
    "specs": [
        {
            "key": "颜色",
            "value": "金属灰",
            "key_id": 1,
            "value_id": 45
        }
    ],
```


## 解决思路4: 
方案3可以大体解决由具体类型到json的序列化问题，但是实际操作中，由于需要对每一个实体类中需要序列化为json的字段进行手动编写get/set方法，处理起来较为繁琐  
因此引入思路4，通过converter转换器
``` java
MapAndJson.class(ListAndJson同理):

package com.zjk.spring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjk.spring.exception.http.ServerErrorException;
import jdk.internal.org.objectweb.asm.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 6/8/21 5:11 PM
 */
public class MapAndJson implements AttributeConverter<Map<String,Object>, String> {
    /**
     * 完成json与不同数据类型之间的序列化与反序列化操作
     * 由于list与Map存在类型差异，需要分别创建MapAndJson和ListAndJson
     * X: Entity Attribute, Y: DateBase Column
     */

    @Autowired
    ObjectMapper mapper;

    @Override
    public String convertToDatabaseColumn(Map<String, Object> stringObjectMap) {
        try {
            return mapper.writeValueAsString(stringObjectMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerErrorException(9999); // 序列化有误时，是和前端无关的异常情况，返回服务器异常通知即可
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String s) {
        try {
            return mapper.readValue(s, HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerErrorException(9999);
        }
    }

}

Sku.class:
package com.zjk.spring.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zjk.spring.utils.MapAndJson;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 6/1/21 12:08 AM
 */

@Entity
@Getter
@Setter
public class Sku extends BaseEntity{
    @Id
    private Long id;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private boolean online;
    private String img;
    private String title;
    private Long spuId;
    private String code;
    private int stock;
    private Long categoryId;
    private Long rootCategoryId;
    private String specs;

    @Convert(converter = MapAndJson.class)
    private Map<String,Object> test1;
    
}

Response:
{
    "id": 9999,
    "price": 77.76,
    "discount_price": null,
    "online": true,
    "img": null,
    "title": "specs实体测试2_一条记录(Map)",
    "spu_id": 2,
    "code": "2$1-45",
    "stock": 5,
    "category_id": 17,
    "root_category_id": 3,
    "specs": "[{\"key\": \"颜色\", \"value\": \"金属灰\", \"key_id\": 1, \"value_id\": 45}]",
    "test1": {
        "key_id": 1,
        "value_id": 45,
        "value": "金属灰",
        "key": "颜色"
    }
}
```
* 一定要注意，如果字段存在空值，那么通过AttributeConverter的convertToEntityAttribute重构方法进行反序列化处理时，会自动抛出异常。
* 此时可以针对实际情况进行处理，可以反回空值(null)，也可以抛出运行时异常

## 解决思路5
### 解决方案4存在的问题
通过ListAndJson和MapAndJson两个工具类，可以将任意数据类型按Map(单条记录)或List(多条记录)方式进行映射，并做序列化处理。  
但是这样会存在一个问题，我们在处理时，将自定义的数据类型视为了Map或者是List，将会失去原有数据类型的成员方法。
*虽然说理论上不应该在模型实体类中写业务方法，但是很多时候如果将业务方法外置(放到模型类外,通过classB.methodXXX(modelClassA)的方式将模型类传与外置方法做关联)会导致代码有过多的冗余。  
>例如：
>```java
>Specs.class:
>@Getter
>@Setter
>public class Specs {
>    String key;
>    String value;
>    Long key_id;
>    Long value_id;
>
>    public String concatKeyValueId(){
>        return key_id+"-"+value_id;
>    }
>
>}
>Sku.class:
>    @Convert(converter = ListAndJson.class)
>    private List<Object> specs;
>    
>```
>这是我们如果想使用specs的concatKeyValueId方法，将会变得很复杂。
>而如果我们使用特定的转换器，例如  
>```
>@Convert(converter = SpecAndJson.class)
>private List<Spec> specs;
>```
>则又回到了上面的问题，即不具有通用性，每个需要做序列化反序列化的字段都需要单独写一个这样的工具类

因此一个比较理想的思路是通过泛型的方式，并使用类似的思路进行序列化反序列化转换处理。

使用泛型进行转换，即将上面的Map替换为代表自定义类型的泛型T，将List<Object>替换为List<T>  

* 但是这又存在一些问题：
* 一是java中很难获取到泛型类的元类(T.class)，而反序列化时convertToEntityAttribute是必须传入元类参数的
* 二是我们很难将泛型的真正类型传入进来，因为我们在实际使用中，是通过@Convert(Converter=xxx.class)注解来指定的，而不是直接调用这个类

### 解决思路
按照之前DozerPaging的思路，我们可以在参数中将元类传入进来，但是由于继承AttributeConverter的类，其convertToEntityAttribute方法的参数列表是固定的，因此我们需要手动创建转换工具类  
并且由于我们需要将真实类型传入进来，因此也不能使用Convert注解处理，需要在字段的get和set方法中进行转换操作。

> 泛型的抹除机制：
> 泛型在编译后将会抹除掉类型参数信息，如Array<String>在编译后会变成Array，即时在编译文件中依然存在<String>签名，但是它是无效的，无法通过反射动态获取泛型实例的具体实参

1. 单一实体
``` java
    public static <T> T jsonToObject(String s) {
        try {
            return s==null ? null : mapper.readValue(s, new TypeReference<T>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerErrorException(9999);
        }
    }
```
2. List<ClassXXX>
处理List有两种方式，其一是将List<ClassXXX>整体看做是一个泛型T，另一种是将List内的作为一个泛型，即List<T>
但是由于泛型抹除机制的存在，List<T>实际上会被抹去T，内部的数据会用HashMap表用
因此一个正确的思路是将List<ClassXXX>整体看做是一个泛型T
而将其看做是一个泛型T的话，实际处理时与单一泛型实体的处理方式是完全一样的，即我们可以将<List<ClassXXX>>与<ClassXXX>正两种看做是一种情况进行处理
```java
    public static <T> T jsonToObject(String s, TypeReference<T> tr) {
        // 泛型T可以指代一个具体的类型，也可以指代List<ClassXXX>这种多个元素的情况
        try {
            return s==null ? null : mapper.readValue(s, tr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerErrorException(9999);
        }
    }
```
* 注意，不能写成以下形式，以下形式也会输出为HashMap而不是List<Specs>
```java
public static <T> T jsonToObject(String s)             
return s==null ? null : mapper.readValue(s, new TypeReference<List<Specs>>(){});
```

## 解决思路6
使用自定义注解，但是实际上自定义注解只是提供了参数信息，具体的业务执行还是需要通过拦截器等机制进行处理，在处理中也非常繁琐。  
而且由于拦截的最基本要求是它必须是一个方法/函数，所以我们打在属性上是无法被拦截器捕获的，还需要通过外层注解、get/set方法注解等去进入到拦截器。  
另外由于entity实体实际上是有ioc管理的，它和aspect创建的代理类不是同一个bean，因此实际上还是无法进入到拦截器，可能还需要在外层创建vo对象，在vo对象中通过set/get等进入拦截器  
即，使用自定义注解处理起来可能会更复杂。

## 解决思路7
使用hibernate-types进行处理。
添加依赖如下，要注意org.hibernate的版本要正确，  
5.4、5.3、5.2对应的是hibernate-types-52，  
5.1、5.0对应hibernate-types-51，  
4.3对应hibernate-types-43，  
4.2、4.1对应hibernate-types-4
```
		<dependency>
			<groupId>com.vladmihalcea</groupId>
			<artifactId>hibernate-types-52</artifactId>
			<version>2.9.7</version>
		</dependency>
```
示例如下：
```java
package com.zjk.spring.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.zjk.spring.utils.GenericAndJson;
import com.zjk.spring.utils.ListAndJson;
import com.zjk.spring.utils.MapAndJson;
import com.zjk.spring.utils.ObjectAndJsonTest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@TypeDef(name = "json",typeClass = JsonStringType.class)
public class Sku extends BaseEntity{
    @Id
    private Long id;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private boolean online;
    private String img;
    private String title;
    private Long spuId;
    private String code;
    private int stock;
    private Long categoryId;
    private Long rootCategoryId;


    @Type(type = "json")
    private List<Specs> specs;

    public void test(){
        specs.forEach(s-> System.out.println(s.concatKeyValueId()));
    }

    @Convert(converter = MapAndJson.class)
    private Map<String,Object> test1;

    @Convert(converter = ListAndJson.class)
    private List<Object> test2;

}


@Autowired
SkuService skuService;
@GetMapping("/sku_to_json_test1")
public Sku sku2jsonTest1(@RequestParam @Positive Long id){
    Sku sku = skuService.getSkuById(id);
    if (sku == null){
        throw new NotFoundException(30002);
    }
    sku.test();
    return sku;
}
    
@Getter
@Setter
public class Specs {
    String key;
    String value;
    Long key_id;
    Long value_id;

    public String concatKeyValueId(){
        return key_id+"-"+value_id;
    }
}

```
实际测试，可以在entity实体中保留Specs类的成员方法和属性信息，我们在test函数中调用了它的方法，并在controller接收到sku后执行test方法，测试功能达到预计效果:
```
1-45
3-9
4-14
```

> 特殊：
> 安装了hibernate-types之后，在启动时会打印很多其他信息如logo等，可通过限制日志等级进行隐藏
> ```logging:
>  level:
>    com.vladminhalcea.hibernate: error
>    '[Hibernate Types]': error
>  ```

# 条件语句
在互联网项目中，一般会使用软删除机制而不会采用物理删除数据库数据，因为数据库的数据收集比较困难，一般不会对已收集来的数据做删除操作，而只是在提取是不提取这部分而已。  
一般可以采用的方法，有:
1. 在Repository上使用sql语句 `@Query("select xxx from xxx where xxx")`
2. 在Entity上使用@Where注解 `@Where(clause = 'xxx')`
3. 在Repository上使用命名函数 `例如：FindAllByIsOnline(1)`
使用命名函数的话，需要再单独在接口里面多写一个相关的命名函数，比较繁琐，一般不使用。

# 循环序列化问题
循环序列化，指的是在双向导航关系中，A导航到B，而B又导航到A，A再次通过导航属性关联到B，这样循环下去  
> 实际上会报一个"Could not write JSON: Infinite recursion (StackOverflowError)错误，但是它只是打印在控制台，并不会影响实际的查询和输出"
首先要明确一点，配置了双向导航属性时，不是因为JSON序列化才触发了"循环"的问题,而是因为双向导航关系本身就是循环查询的，这个查询是由"双向导航"导致的。  
之所以在查询的时候以及中间实现Service层各种业务逻辑的时候没有出现这种情况，是因为导航属性默认都是懒加载的`fetch = FetchType.LAZY`,因此在我们不查询导航属性的的时候，  
他是不会加载导航属性(即另一张表)的内容的。而序列化的时候如果不做限制，它会将所有的内容全部序列化出来，因此就会触发所有的导航属性，导致循环序列化的产生。  

* 但是要注意，循环序列化不是一种错误，它只是一种需要尽可能避免的查询、输出方式，因为我们大部分业务逻辑都是只需要A表join到B表就够了，不需要B表再次Join到A表。
* 例如，A表表示学生表，B表为老师表，我们想获取学生是哪些老师带课的，用学生表关联老师表，获取当前学生拥有的所有老师就行了，我们一般不需要再去知道这个老师除了你还有哪些学生，因为这就完全属于另外一个需求。
* 同样的，我们想知道老师带了哪些学生，我们只要用老师表关联学生表获取老师拥有的学生就行了，至于老师t1它拥有的学生s1、s2等他们还有什么老师带课，这就又是另外一个需求的了。

即，我们及时不做限制，获知是在查询的时候就直接使用急加载`fetch = FetchType.EAGER`，依然是能够成功查询到结果的。  
但是由于这样查询到的数据量普遍较大，而且一般我们不会有这样的业务需求，所以我们要尽可能避免循环序列化甚至是循环查询的出现。

那思路就很简单了，既然循环查询是由于查询了导航属性才触发的，这个导航属性的查询可能是Service业务层的逻辑有查询到这部分，也有可能是序列化的时候查询到了这部分。  
但是总之，循环序列化的产生归根到底就是因为查询了导航属性。

>举个例子，如果A和B有双向关系，A表有id、name以及导航属性B_字段，B表也是有id、name以及导航属性A_字段，
>1) 如果我们根据A表的id查A表，然后直接进行序列化，那么必然会触发循环序列化
>2) 如果我们根据A表的id查A表，然后业务层中查询B表的id、name字段，那么并不会触发循环序列化，因为没有涉及到B表的导航属性A_的查询。
>3) 如果我们根据A表的id查A表，然后业务层中查询B表的id、name、A_，一定会触发循环序列化，因为在A中查了B，然后B中又查了A，就会形成回路。
>4) 如果我们根据A表的id查A表，然后业务层不涉及B表内容的查询，那么返回结果一定不会触发循环序列化，因为压根就没有发生对A表关联字段B_的查询，没有关联到B表，就更别说返回来关联形成回路了。
>5) 通过B表id查询B表的情况与上述类似，就不再赘述了。

所以综上分析，如果双向关系想要避免出现的循环序列化，就需要在业务层对输出、查询内容进行限制，**至少有一边**不能够查询导航属性。

# stream流处理
list等许多数据类型都具有stream流处理方式，将它转化为流之后，可以使用很多类似于map、reduce的函数式编程的方法
> Java8新了stream API，需要注意的是Stream和I/O中的流是没有关系的，这个stream主要是要来处理集合数据的，可以将其看作一个高级迭代器。在Collection接口中新增了非抽象的stream方法来获取集合的流。

# 用户、权限与分组
***一般来说，用户不应该与权限由直接的联系，比较好一点的方案是用户属于某个分组，一个具体的分组具有某些权限***
需要考虑的问题包括：
1. 权限的粒度，即权限能够到控制器api还是到数据库的某个字段  
一般来说这种问题可以通过动态sql查询，即前端指定查询的字段给服务器api，但是这样会存在字段泄露的风险，而且使用起来也较为繁琐。  
其次还可以使用创建只查询某些特定字段的接口，前端直接访问这个接口，但是这样同样会导致处理起来变得繁琐。
2. 此外还需要考虑的问题是权限的配置和回收问题
配置指的是用户可访问的api的组别权限规则，例如无需任何权限的公共api、需要特定权限的api  
回收问题争议较多，例如当A组别中的用户为B组别添加了权限，那么当管理员回收A组权限时，B组权限应当如何处理。

## 组别的设计
方法1. 使用@自定义组别名称的方式，将其加入到特定api控制器上。例如将@Admin、@Vip加入到CouponController的`getById`上，即表示只有属于Admin、Vip用户组的才能访问这个接口。  
方法2. 使用Scope，即为用户分配一个权限等级，当等级大于Scope(例如用户Scope=8，控制器Scope=4)时才可访问，否则拒绝访问。

方法1就是一般的分组机制，将用户分为各种不同的组别，然后不同的组别有不同的权限。
方法2则是使用一种权限级别的中间机制来代替组别，减少复杂度。

## 登录方式
1. 账号 密码
2. 电邮 密码
3. 手机号 验证码
4. 第三方登录
5. 微信静默登录   
(小程序依附于微信本身，微信已经验证过账号密码了，可以直接通过微信的机制进行静默登录)

### 票据
票据简单理解就是一个用户的身份，传统网页中一般是将票据写在cookie中的，它有一个过期时间，可以起到一段时间免登陆的作用。
前后端分离的vue、app、小程序等多使用令牌的方式。一朵使用较多的是jwt令牌，jwt令牌处理可以写入用户信息以外，还可以写入很多自定义内容，例如用户uid、用户常驻ip端等等。
* 不管是什么样的票据，都是具有时效性的

## 无感知二次登陆
由于所有的票据都是具有时效性的，例如7天，当票据即将过期的时候，如6天23小时59分时登陆，  
发送请求由于是需要时间的，可能到达服务器进行验证的时候会发现已经超时了，这时是需要小程序进行二次登陆的。  
同样的，如果长期不登陆，超过了7天，也许要重新登录的。
这时候我们的解决方法有：
1. 直接返回给小程序提示已过期，重定向到登录界面让用户重新登录。但是这样会使得用户体验下降。
2. 增加票据的有效时间，从7天延长至更久的时间。但是这样实际上仍未解决问题，此外还会存在风险问题。
3. 使用双jwt令牌机制，当失败的时候尝试再次获取票据。即，当后端验证到票据已失效，那么前端再次通过小程序的机制再次生产JWT令牌，并将其保存在缓存中。 (C小程序使用的是该机制)
4. 另一种双令牌机制，即同时存在一个access_token，一个refresh_token，自动对有效期进行延长。 (O端cms使用的该机制)

### 微信小程序静默登录的流程

具体流程如下：  
1. 在小程序登录(wx.login)后，通过wx.login的回调函数success可以获取res中的code码
```
 wx.login({
      success: (res) => {
        if (res.code) {
            do-something
        }
      }
    })
```
> 这个`code`码，是用户登录凭证（有效期五分钟）。  
> 开发者需要在开发者服务器后台调用 `auth.code2Session`，使用 `code `换取 `openid` 和 `session_key `等信息
> `session_key` 的作用之一是将小程序前端从微信服务器获取到的`encryptedData` 解密出来，获取到`openId` 和`unionId` 等信息。
> 但是在登录过程中开发者服务器是能够直接拿到用户的openId 信息的，而且`unionId` 也是有其他获取途径的，所以`session_key` 在这里的作用看起来有点鸡肋。
> `openid`是一个用户在一个小程序中的唯一标识,即它的有效作用域只在某一个特定的小程序中，对应的是用户表中的一个用户。
> 但是实际使用中一般是不建议将`openid`发到前端的，一般更多的是使用用户表的uid(user-id)。
> * `openid`是只在某个小程序中的有效的，即用户A在小程序1和小程序2中会存在两个不同的openid
> `unionId`则是一个用户相对于所有微信小程序的唯一标识，例如用户A在小程序1和小程序2小程序3等等中，他的`unionId`都是同一个。
> `unionId`的应用场景一般是旗下有多个产品是，使用unionId标识同一个用户，用于多个产品间共享用户信息。

2. 前端将code码通过特定api发送给后端，后端访问小程序接口`code2session: https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code`  
从接口返回值中获取用户的openid、unionId等。

3. 访问用户表，查询用户中是否存在一个对应openid的用户，如果不存在，则认为是新用户，执行注册新用户流程。  
如果存在对应openid，则提取对应的uid、scope等相关信息。

4. 将用户的uid、scope、开始时间、结束时间通过java的jjwt、auth0等工具生成jwt令牌(String字符串)，返回给前端小程序。

5. 前端收到jwt后，将其保存在缓存中供以后使用

### 前端验证过程
前端在登录以后，还需要进行jwt令牌的校验，主要是验证是否是一个有效的jwt令牌。
实际上小程序前端在本部分的处理是：  
1. app.js中，在打开时就执行token.verify
```
onLaunch() {
        const token = new Token()
        token.verify()
}
```
其中token.verify函数中分别执行了获取缓存中的jwt令牌、获取失败时通过服务器api再次生成jwt令牌并保存在小程序缓存、如果jwt存在则在服务器api校验jwt是否有效。
```
class Token {
    // 1. 携带Token
    // server 请求Token

    // 登录 token -> storage

    // token 1. token不存在 2.token 过期时间
    // 静默登录

    constructor() {
        this.tokenUrl = config.apiBaseUrl + "token"
        this.verifyUrl = config.apiBaseUrl + "token/verify"
    }

    async verify() {
        const token = wx.getStorageSync('token')
        if (!token) {
            await this.getTokenFromServer()
        } else {
            await this._verifyFromServer(token)
        }
    }

    async getTokenFromServer() {
        // code
        const r = await wx.login()
        const code = r.code

        const res = await promisic(wx.request)({
            url: this.tokenUrl,
            method: 'POST',
            data: {
                account: code,
                type: 0
            },
        })
        wx.setStorageSync('token', res.data.token)
        return res.data.token
    }

    async _verifyFromServer(token) {
        const res = await promisic(wx.request)({
            url: this.verifyUrl,
            method: 'POST',
            data: {
                token
            }
        })

        const valid = res.data.is_valid
        if (!valid) {
            return this.getTokenFromServer()
        }
    }

}
```

### 接口api的权限校验
当jwt校验成功时，即表示数据库中存在这个用户，此时在前端封装的HTTP.request方法中将token(jwt令牌)写入到header中，  
由于所有api接口都是通过这个封装的方法进行访问的，因此每次访问api都会携带有jwt令牌。  
后端Controller接收到api请求之后，首先通过Interceptor机制拦截该访问，  
通过读取打在当前Controller上的scope注解，与jwt中的scope进行比对，判断是否访问公共api、是否具有访问特定api的权限等执行放行、拒绝访问等操作。

