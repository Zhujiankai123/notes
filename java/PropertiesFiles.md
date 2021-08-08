# 配置文件的两种格式

## yml
yml格式一定要包含application关键字，固定格式为application-xxx，读取时会以spring.profiles.active进行识别
yml放在resource文件夹下任意位置都可以，只要是application-xxx.yml格式的都可以被识别到

## properties
properties格式的文件名可以任意选取
如果放在resource文件夹下会被直接读取到，而如果放在其他子文件夹下，
则需要再控制器或其他类的上面通过类似于`@PropertySource(value = "classpath:config/injection-test.properties")`
的方式指定配置所在的路径，否则将读取失败

## 配置的读取
通常情况下格式为：
```
@Value('${xxxx.xxx.xx}')
Private String name;
```

* properties的默认字符集为ISO_8859_1,直接通过ResponseBody输出会出现乱码  
方法1: `return new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);`  
方法2: 手动修改配置文件的编码格式为utf8，在file encodings配置中，选择utf-8，开启transparent native-to-ascii conversion。
* （如果无效需重新写一遍配置文件的内容）

* 其中，transparent native-to-ascii conversion选项是起到关键作用的，不开启这个的话，即时编码类型为UTF8也会出现乱码的情况

> 1. 使用new String(xxx.getByte(codingType)，newCodingType)时，参数中的编码类型需要严格一致，否则会无法正常转换格式。因此不适用于普遍情况 
> 2. 使用修改文件编码格式的方式，一定要确保`transparent native-to-ascii conversion`选项是打上了勾的. 


# message消息
有些spring的message消息，例如内置校验类中校验失败的返回消息，可以通过配置文件直接配置，而不必通过硬编码的方式直接在校验参数中输入message内容。
* 需要注意的是，其他的配置文件，文件名可以自定义，而message配置文件的文件名必须为`ValidationMessages.properties`。  
与其他配置文件一样,在配置文件中直接写入配置内容即可(需要注意的是配置文件中不需要在值上添加引号)，也可以使用{min}、{max}等将校验参数传递进来。  
调用时，与其他配置文件一样，也是使用{}花括号进行引用。
* 这种方式修改message后，返回的异常内容可能会不是自己想要的格式，这时可以在全局异常处理中通过`e.getMessage()`方法进行调整。

## 自定义校验注解中message的问题
在已定义校验注解中，在注解调用时传入的message会覆盖default message内容。  
而我们在校验类"xxxRela.class"中会通过`constraintValidatorContext.buildConstraintViolationWithTemplate("xxxxxxx").addConstraintViolation()`来根据校验内容提供相关的消息，  
如果不设置`constraintValidatorContext.disableDefaultConstraintViolation()`，则会将上面的default message或调用时修改的message添加到消息内容中。
例如：  
如果不设置关闭DefaultConstraintViolation，在注解校验类中针对校验内容返回校验消息为"123",  
使用@xxx(message="abc")时将会返回"123，abc",而且这两个message的位置并不是固定不变的,有时message在最前，有时在最后。  
这可能不是我们想要看到的结果，所以我们一般会设置`constraintValidatorContext.disableDefaultConstraintViolation()`  
***开启`constraintValidatorContext.disableDefaultConstraintViolation()`时，一般认为注解中message是提供的附加信息，而不是校验信息。***
