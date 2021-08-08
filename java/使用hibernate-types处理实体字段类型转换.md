使用hibernate-types进行处理。
添加依赖如下，要注意org.hibernate的版本要正确(org.hibernate.hibernate-core版本)，  
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

# Sku.class:
package com.xxx.spring.model;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.xxx.spring.utils.GenericAndJson;
import com.xxx.spring.utils.ListAndJson;
import com.xxx.spring.utils.MapAndJson;
import com.xxx.spring.utils.ObjectAndJsonTest;
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
	
	  // Specs自定义类型的业务方法测试
    public void test(){
        specs.forEach(s-> System.out.println(s.concatKeyValueId()));
    }

# controller(部分)
@Autowired
SkuService skuService;

@GetMapping("/sku_to_json_test1")
public Sku sku2jsonTest1(@RequestParam @Positive Long id){
    Sku sku = skuService.getSkuById(id);
    if (sku == null){
        throw new NotFoundException(30002);
    }
    // 执行sku的test方法，测试Specs的成员方法能否正常调用
    sku.test();
    return sku;
}
    
# Specs自定义类型
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

  

> 注意：
> 安装了hibernate-types之后，在启动时会打印很多其他信息如logo等，可通过限制日志等级进行隐藏
>
> ```logging:
> level:
> com.vladminhalcea.hibernate: error
> '[Hibernate Types]': error
> ```