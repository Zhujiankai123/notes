***除了通过类似于findAll这样的命名规则查询以外，还可以使用写JPQL及原生SQL的方式来完成查询***
# JPQL
>JPQL:java-persistence-query-language
> 
>与原生SQL不一样的是，JPQL是用于操作model数据对象的，而原生SQL操作的是数据库的数据表

具体要求如下:
1. 里面不能出现表名,列名,只能出现java的类名,属性名，区分大小写
2. 出现的sql关键字是一样的意思,关键字不区分大小写
3. 不能写select * 要写select 别名  

例如在ThemeRepository中自定义JPQL查询：  
```java
@Query("select t from Theme t where t.name in (:name)")
List<Theme> findByNames(@Param("names") List<String> names);
```
在上面这个例子中，  
* 不能写Theme的数据库表名(theme),而要写成Theme;
* 不能写select * ,要写Theme的别名，即select t;
* 实际上findByNames这个名称已经和命名规则无关了，函数名称是可以随意选取的。
* (:names)表示通过@Param绑定的变量名称;
* 如果参数名称与(:)中的名称相同的话，@Param实际上是可以不写的
  
  
**注意： JPQL实际上支持的关键字是比原生SQL要少的**

# 原生SQL
在@Query中，有一个nativeQuery参数，默认是false，手动将其修改为true后，就可以在这里面写原生SQL了(不建议)。
```java
// Model:
@Entity
@Getter
@Setter
@Where(clause = "delete_time is null and online = 1")
public class Theme extends BaseEntity{
    private Items items;
}


// Repository: 
public interface ThemeRepository extends JpaRepository<Theme,Long> {
@Query(nativeQuery=true,value = "select * from theme where name in (:var_names)")
List<Theme> getByNames1(@Param("var_names") List<String> names);
}
```
上面例子中，使用原生SQL完成select * from theme where theme in (theme-names)  
在控制台中可见其JPA查询语句为: select * from theme where name in ( ?, ? )  
可以看到，使用原生sql后，将会不再处理Entity中@Where("delete_time is null and online = 1")，  
因此在使用原生SQL时一定要小心，避免条件遗漏