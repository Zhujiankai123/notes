# 分类方式
## 一般分为Error和Exception，都是继承于throwable基类的。
    但是Error一般指的更多是错误，而不是异常
## Exception分为Checked Exception和Runtime Exception
    Checked Exception是必须在代码中进行处理的，否则编译时无法通过。
    Runtime Exception是运行时异常，多为用户输入等未知情况，一般无法在代码中进行处理。
## 处理手段
    往上抛、记录日志、返回到前端等

## Runtime Exception
继承于Exception基类

## Checked Exception
并没有Checked Exception这个类，一般来说如果不是Runtime Exception，那么它就是一个Checked Exception，对应Exception类。

* 如果对于一个异常我们带代码中可以处理的，例如数据库账号密码等配置错误无法正常登陆，
    那么应该定义为一个Checked Exception(可以认为是一个Bug)
* 而如果在代码中无法处理，例如在用户查询时没有找到对应的数据库记录，
    那么应该定义为一个Runtime Exception(严格来说并不是一个Bug，应该认为是一种正常的情况)

# 另一种分类方式
 * 已知异常 
    主动throw出来的异常，例如判断除法分母是否为零，为零则抛出异常，这个异常就是已知异常。
 * 未知异常
    未主动抛出的异常，例如没有判断分母是否为零，而程序内部计算时发现分母为零了，会自动抛出一个异常，这个异常就是未知异常。
 
# 全局异常处理机制
1. 主动抛出异常，或自动生成异常 
2. 自定义异常类(xxxException.class)/默认异常(Exception.class) 
3. GlobalExceptionAdvice全局异常处理(类) ,通过ControllerAdvice截获请求，通过ExceptionHandler对指定异常进行处理
4. 通过ExceptionCodeConfiguration读取异常类型的配置文件，通过@PropertySource指定配置文件所在路径，
    并通过ConfigurationProperties指定前缀(prefix)。
    例如properties文件内容为lin.codes[12345] = xxx，
    认为lin即为前缀codes[12345] = xxx可以映射为一个map<key,val>类型的数据，12345为键，xxx为值。
    要注意定义的map数据的变量名称要于properties中的一致，即codes。
5. 如果配置文件中存在中文，要使用new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)进行格式转换
6. 创建一个UnifyResponse类，用于指定返回的数据格式，即返回哪些字段。
7. 在GlobalExceptionAdvice中配置好UnifyResponse，并做返回。

 