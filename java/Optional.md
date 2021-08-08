***Optional 和 if else没有什么本质区别，只是在原有的基础上，为我们的判空操作的提供了一个标准写法***

通过某些方法获取到的数据，可能会存在空值的情况。我们需要手动编写if xxx is null throw new Exception这样的语句。  
但是由于我们不清楚哪些数据会存在空值，所以很可能会忘记对返回结果的判空操作，这可能会导致返回结果有误。  
而通过Optional，相当于是强制要求你对空值进行判断，也就避免了忘记空值判断的问题。  
>注意：
>1. Optional是强制你去考虑空值的情况，并不是说有了Optional你就不需要判空操作了
>
>2. Optional<xxx>.orElseThrow()中是返回(生产、提供)一个异常，而不是throw一个异常。
> 因此，正确的写法是返回一个异常`()->new xxxException()`
> 可以用Statement Lambda: `t1.orElseThrow(()->{return new RuntimeException("t1 is null");})`
> 也可以使用Expression Lambda `t1.orElseThrow(()->new RuntimeException("t1 is null"))`

## Supplier、Consumer、Function、Predicate、Runnable
### Supplier:
向接口提供数据，Optional常见参数为Supplier的接口有`orElseGet`、`orElseThrow`  
例如
```
        Optional<String> t1 = Optional.empty();
        String s = "abc";
        String res = t1.orElseGet(()-> s.substring(0,2));
        System.out.println(res);
```
在orElseGet中传入一个lambda表达式，lambda表达式返回一个值给orElseGet

### Consumer
接口为我们提供一个值，例如Optional中的`ifPresent`
```
        t2.ifPresent(t-> System.out.println(t2.get()));
```
ifPresent接口为我们提供一个值，我们定义它为t，然后可以在lambda中对t进行操作(如打印t的内容等)

### Function
参数为Function的，有`map`、`flatMap`  
Function的特点是接口为我们提供一个值，我们可以对这个值进行操作,然后再提供一个值返回给接口
```
    @Test
    // map
    public void testOptional9(){
        Optional<String> t1 = Optional.of("abc");
        String s2 = t1.map(s-> s+"a").get();
        System.out.println(s2);
        
        // map的lambda函数返回值并不限制一定要和Optional的泛型类型(或者说lambda函数的参数的类型)一致
        Integer s3 = t1.map(s-> s.length()+"a".length()).get();
        System.out.println(s2);
    }

    @Test
    // flatmap
    public void testOptional10(){
        Optional<String> t1 = Optional.of("abc");
        String s2 = t1.flatMap(s->Optional.ofNullable(s+"d")).get();
        System.out.println(s2);

    }
```

#### map和flatmap的区别：

* map：  
`public<U> Optional<U> map(Function<? super T, ? extends U> mapper) `  
map接收的Function函数中，函数的参数s是T类型的，返回值是U类型的  
即输入输出的类型可以不一致，他们可以是任意数据类型的。

* flatMap:
`public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper)`  
flatMap接收的Function函数中，函数的参数s是T类型的，但是返回值是Optional<U>类型的  
也就是说，输入的类型是T类型的，但是输出结果一定要用Optional进行封装。


> ? super T: 下届通配符，即所有T的父类及T类本身  
> ? extends U: 上届通配符，即所有T的子类及T类本身

### Predicate
lambda需要返回一个布尔值给接口，接口通过布尔值确定输出结果。Optional中`filter`接口的参数为Predicate
```
       Optional<String> t1 = Optional.of("abc");
       String s2 = t1.filter(s->s.length()==3).get();
       System.out.println(s2);
```

### Runnable
无输入，无输出

> 注意，Optional中的map、filter方法并不能像函数式编程中的map、reduce那样当成"映射"来使用。  
> 例如在list<Integer> a = [1,2,3]，在函数式编程中可以用map方法，传入一个lambda表达式(s->{do something})，同时对列表中的所有元素进行操作。  
> 而在Optional中，map、filter只是个普通函数，lambda表达式(s->{do something})中s表示的是整个a数组，而不是像函数式编程中s表示a数组中的各个元素。  
> 因此类似于`Optional<List<Integer>> optional1 = Optional.of(a);optional1.map(s->s+1)`这样的操作是无效的：
> `Operator '+' cannot be applied to 'java.util.List<java.lang.Integer>', 'int'`


# Optional的链式调用
`flatMap`、`map`、`filter`这三个方法返回的是Optional而不是Get值，可以进行链式调用，执行一系列需要的操作。


# 测试脚本
```java
package com.zjk.spring.optional;

import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * @Author wqzjk393
 * @Description:
 * @Date 6/20/21 6:49 PM
 */
public class OptionalTest {

    @Test
    // Optional如果为空值，直接get会提示没有元素可供提取
    //java.util.NoSuchElementException: No value present
    public void testOptional1(){
        Optional<String> empty = Optional.empty(); // 构建空的Option对象
         empty.get();
    }

    @Test
    // 通过Optional.of传入数值时，传入的不能为空值
    // java.lang.NullPointerException
    //	at java.util.Objects.requireNonNull(Objects.java:203)
    public void testOptional2(){
        Optional<String> t1ofNullable = Optional.ofNullable(null); // 如果一定要传入空值，可以使用ofNullable
        System.out.println(t1ofNullable.orElse("is empty"));

        Optional<String> t = Optional.of(null); // 通过of构建Option对象 - 传入一个空值
    }

    @Test
    // 通过of传入非空数据
    // 通过get方法取值
    public void testOptional3(){
        Optional<String> t = Optional.of("abc"); // 通过of构建Option对象 - 传入一个非空值
        System.out.println(t.get());
    }

    // 通过get方法取值其实是没有意义的，
    // 因为如果一个返回数据有空值的话，在取值的时候就会报错，而如果没有空值的话，封装Optional对象就没有意义了
    // 空指针由于是一种隐藏性的错误，如果不立即捕获的话，可能会随着堆栈层数变深以后，变得很难找到最开始出现错误的地方。
    // 因此Optional对于get结果为空值立马报错的原因就是在于避免空指针问题往后延伸

    @Test
    // isPresent
    // 这其实和传统的if else判空写法一样，并没有用到Optional的特性
    public void testOptional4(){
        Optional<String> t = Optional.empty(); // 通过of构建Option对象 - 传入一个非空值
        if (t.isPresent()){
            System.out.println(t.get());
        }
        else System.out.println("t is empty");
    }

    @Test
    // ifPresent
    // 如果一定要手动判断是否有值，比较好一点的写法是ifPresent+consumer
    // 这其实和传统的if else判空写法一样，并没有用到Optional的特性
    public void testOptional5(){
        Optional<String> t1 = Optional.empty(); // 通过of构建Option对象 - 传入一个非空值
        t1.ifPresent(t-> System.out.println(t1.get()));

        Optional<String> t2 = Optional.of("abc"); // 通过of构建Option对象 - 传入一个非空值
        t2.ifPresent(t-> System.out.println(t2.get()));
    }

    @Test
    // orElse
    // abc.orElse(xxx)相当于if abc is not null then return abc.get(),else return xxx
    public void testOptional6(){
        Optional<String> t1 = Optional.empty(); // 通过of构建Option对象 - 传入一个非空值
        System.out.println(t1.orElse("t1 is null"));

        Optional<String> t2 = Optional.of("abc"); // 通过of构建Option对象 - 传入一个非空值
        System.out.println(t2.orElse("t2 is null"));
    }

    @Test
    // orElseThrow
    // abc.orElse(new xxxException())相当于if abc is not null then return abc.get(),else throw new xxxException()
    public void testOptional7(){
        class MyException extends RuntimeException{
            String message = "";
            public MyException(String message){
                this.message = message;
            }
        };
        Optional<String> t1 = Optional.empty(); // 通过of构建Option对象 - 传入一个非空值
        // Expression Lambda
        System.out.println(t1.orElseThrow(()->new RuntimeException("t1 is null")));

        // Statement Lambda
        // System.out.println(t1.orElseThrow(()->{return new RuntimeException("t1 is null");}));

        // java8中新增的静态构造方法
        // System.out.println(t1.orElseThrow(RuntimeException::new));
    }

    @Test
    // orElseGet
    public void testOptional8(){
        Optional<String> t1 = Optional.empty();
        String s = "abc";
        String res = t1.orElseGet(()-> s.substring(0,2));
        System.out.println(res);
    }

    @Test
    // map
    public void testOptional9(){
        Optional<String> t1 = Optional.of("abc");
        String s2 = t1.map(s-> s+"a").get();
        System.out.println(s2);
        
        // map的lambda函数返回值并不限制一定要和Optional的泛型类型(或者说lambda函数的参数的类型)一致
        Integer s3 = t1.map(s-> s.length()+"a".length()).get();
        System.out.println(s2);
    }

    @Test
    // flatmap
    public void testOptional10(){
        Optional<String> t1 = Optional.of("abc");
        String s2 = t1.flatMap(s->Optional.ofNullable(s+"d")).get();
        System.out.println(s2);

    }


    @Test
    // filter
    public void testOptional11(){
        Optional<String> t1 = Optional.of("abc");
        String s2 = t1.filter(s->s.length()==3).get();
        System.out.println(s2);

    }


    @Test
    // 链式调用
    public void testOptional12(){
       Optional<String> t1 = Optional.empty();
       String res = t1.map(s-> s+"d").orElse("else");
        System.out.println(res);
       t1.map(s-> s+"d").orElse("else");
    }
}

```