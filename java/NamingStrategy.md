
post-body中，json键名会用到"-"、"_"、大写驼峰(UpperCamel)、小写驼峰(lowerCamel)等多种方式，
而java中的属性一般只会使用下划线及小写驼峰(lowerCamel)两种方式命名。
NamingStrategy命名规则的目的就是指示从json键名到java属性(字段)直接的对应关系，
一般习惯为java字段统一使用驼峰方式，而json使用什么规则一般是前后端约定好的。  

* 需要注意，只有在java属性中使用lowerCamel方式，以下规则才会生效。
* 若java属性为snake_case方式，那么json中必须以下划线(snake_case)作为分隔符，
* 例如java属性为`private LoginType login_type;`时，无论打的JsonNaming注解是什么，都只能识别`"login_type" : "xxx"`这一种方式  
   只有java属性为`private LoginType loginType;`时，JsonNaming注解才能生效，假设此时配置的是`KebabCaseStrategy.class`，就能识别到`"login-type" : "xxx"`
* 没有JsonNaming注解时，以全局配置(`spring.jackson. property-naming-strategy: xxx`)为准，若全局没有做配置时，执行默认策略，即lowerCamel


> 需要区分jackson命名策略和JPA命名策略  
> * jackson命名策略，只是指示前端json数据和后端模型、实体、字段(属性)的对应关系，  
>  这个对应关系可以是前端传json给后端然后解析成字段，也可以是后端字段序列化并写出到前端json数据,但是规则的目的都是指示json键名以什么形式进行展示  
>  命名策略是只跟json的键名有关的，和内容无关。  
>  例如配置为snake_case时json数据为,"a_b": "cD"，或"a_b": "c-d"，java解析到的属性的值依然是"cD"或"c-d"，并不受snake_case策略影响    
> * 而JPA命名规则，则是为了指示数据库字段和entity实体之间的对应关系。  
>  默认的JPA命名规则为下划线方式(ImprovedNamingStrategy)，即entity为lowerCamel(或snake_case)形式，对应的数据库字段为snake_case方式  
>  如果一定要强制使用其他方式，需要配置`@Column(name="abcDef")`，  
>  或在全局配置`spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl`  

前端传入json的键名能否正确被识别成java的属性，主要是看对于命名规则的配置
配置可以写在配置文件中，例如`spring.jackson. property-naming-strategy: SNAKE_CASE`，即全局配置
也可以通过`@JsonNaming`注解单独为某个类配置命名规则，如`@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)`

> 常用的命名规则方式有:KEBAB_CASE("-"连接符，如abc-def)  
>                    SNAKE_CASE(下划线连接，如abc_def)  
>                    LOWER_CASE(小写且无连接符，如abcdef)  
>                    LOWER_CAMEL_CASE(词之间没有连接符，第一个词的首字母小写，后面每个词的首字母大写，如abcDef)  
>                    UPPER_CAMEL_CASE(词之间没有连接符，每一个单词的首字母都大写，如AbcDef)  
> 
> 对应的PropertyNamingStrategy为:  
>                    PropertyNamingStrategy.KebabCaseStrategy.class  
>                    PropertyNamingStrategy.SnakeCaseStrategy.class  
>                    PropertyNamingStrategy.LowerCaseStrategy.class  
>                    PropertyNamingStrategy.UpperCamelCaseStrategy.class  
> 
> * 不存在`PropertyNamingStrategy.LowerCamelCaseStrategy.class`这种方式，默认值即为lower驼峰:
>   `PropertyNamingStrategy.class`
> 
> // 配置json以"-"作为分隔符: "login-type" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)`  
> // 配置json无分隔符，单词全部为小写: "logintype" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.LowerCaseStrategy.class)`  
> // 配置json以"."作为分隔符，单词全部为小写: "login.type" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.LowerDotCaseStrategy.class)`  
> // 配置json以"_"作为分隔符: "login_type" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)`  
> // 配置json以upper驼峰方式传输: "LoginType" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)`  
> // 配置json以默认方式(lower驼峰)方式传输: "loginType" : "USER_WX"  
> `@JsonNaming(PropertyNamingStrategy.class)`  


测试：
(注意，当属性名称为snake_case时，无论配置使用任何命名规则，都只能识别snake_case这一种json键名)  
当配置为下划线规则(@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class))时，前端只有传入以"_"作为分隔符的键名，才能正确转换为对应的java属性:
``` 
  // java
  private String testT1;
  private String test_t2;
  private LoginType testT3;
  private LoginType test_t4;
  private int testT5;
  private int test_t6;
  
  // json
  String testT1:
  请求 testT1 = "USER_WX"   返回: null
  请求 test-t1 = "USER_WX"  返回: null
  请求 test_t1 = "USER_WX"  返回: "USER_WX"

  String test_t2:
  请求 testT2 = "USER_WX"   返回: null
  请求 test-t2 = "USER_WX"  返回: null
  请求 test_t2 = "USER_WX"  返回: "USER_WX"

  LoginType testT3:
  请求 testT3 = "USER_WX"   返回: null
  请求 test-t3 = "USER_WX"  返回: null
  请求 test_t3 = "USER_WX"  返回: {LoginType@11175}

  LoginType test_t4:
  请求 testT4 = "USER_WX"   返回: null
  请求 test-t4 = "USER_WX"  返回: null
  请求 test_t4 = "USER_WX"  返回: {LoginType@11175}

  int testT5:
  请求 testT5 = 123   返回: 0
  请求 test-t5 = 123  返回: 0
  请求 test_t5 = 123  返回: 123

  int test_t6:
  请求 testT6 = 123   返回: 0
  请求 test-t6 = 123  返回: 0
  请求 test_t6 = 123  返回: 123
```
