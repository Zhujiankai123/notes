# 拦截器的分类
Filter : servlet服务的拦截器，一般称之为过滤器.
         Filter 粒度最大，一般做字符编码、过滤不需要的参数、简单的校验(如是否登录状态)
Interceptor : spring拦截器,多用于验证等操作
            :Interceptor粒度一般，多用在方法、请求中的拦截操作。
AOP : spring对面向切面的一种实现，可以具体到对一个类、一个方法进行拦截操作

* request: 一个请求进来以后，先进入Filter,再进入Interceptor，最后进入AOP
* response: 返回结果时，先进入AOP，再进入Interceptor，最后进入Filter

# @ControllerAdvice、@ExceptionHandler
主要是处理异常为主，通过`public ResponseEntity<UnifyResponse> handleHttpException(HttpServletRequest req, HttpException e){}`异常处理函数，对捕获到的异常进行相关操作。

# 通过实现WebMvcRegistrations接口
这个接口主要是对路由注册进行拦截操作的，一般多用于路由前缀修改等操作
```
public class AutoPrefixConfiguration implements WebMvcRegistrations {
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new AutoPrefixUrlMapping();
    }
}
```

# 通过实现HandlerInterceptor接口或是继承HandlerInterceptorAdapter(deprecated)
实现HandlerInterceptor接口，并重写preHandle、postHandle、afterCompletion方法。  
在一个应用中或者是在一个请求中可以同时存在多个Interceptor，每个Inteceptor的调用都会按照它的声明顺序依次执行，  
而且最先执行的Interceptor的preHandler方法，所以可以在这个方法中进行一些前置初始化操作或者是对当前请求的一个预处理，也可以在这个方法中进行一些判断是否要继续进行下去。   
该方法的返回值是Boolean类型的，当它返回为false时，表示请求结束，后续的Interceptor和Controller都不会再执行；  
当返回值为true 时就会继续调用下一个Interceptor的preHandle方法，如果已经是最后一个Interceptor的时候就会是调用当前请求的Controller方法。  
除了原生的Interceptor，还可以使用springboot的ControllerAdverse等  

除了实现相关方法以外，还需要在配置类中进行拦截器注册
```
@Component
public class InterceptorConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PermissionInterceptor());
    }
}
```
