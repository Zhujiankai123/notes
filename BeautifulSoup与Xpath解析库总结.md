# BeautifulSoup与Xpath解析库总结



## 一、BeautifulSoup解析库

### 　　1、快速开始

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
html_doc = """
<html><head><title>The Dormouse's story</title></head>
<body>
<p class="title"><b>The Dormouse's story</b></p>

<p class="story">Once upon a time there were three little sisters; and their names were
<a href="http://example.com/elsie" class="sister" id="link1">Elsie</a>,
<a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
<a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
and they lived at the bottom of a well.</p>

<p class="story">...</p>
"""
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
from bs4 import BeautifulSoup
soup = BeautifulSoup(html_doc)

print(soup.prettify()) # 结构化输出文档
print(soup.title) # 获取title标签
print(soup.title.name) # 获取title标签名称 
print(soup.title.parent.name)
print(soup.p['class']) 
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

　　从文档中找到所有<a>标签的链接：

```
for link in soup.find_all('a'):
    print(link.get('href'))
```

　　从文档中获取所有文字内容：

```
　print(soup.get_text())
```

### 　　2、标签选择器

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
#1、标签选择器：即直接通过标签名字选择，选择速度快，如果存在多个相同的标签则只返回第一个
#2、获取标签的名称
#3、获取标签的属性
#4、获取标签的内容
#5、嵌套选择
#6、子节点、子孙节点
#7、父节点、祖先节点
#8、兄弟节点
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

![img](https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif) 示例

### 　　3、标准选择器

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
#find与findall:用法完全一样,可根据标签名,属性,内容查找文档，但是find只找第一个元素
html_doc = """
<html><head><title>The Dormouse's story</title></head>
<body>
<p class="title">
    <b>The Dormouse's story</b>
    Once upon a time there were three little sisters; and their names were
    <a href="http://example.com/elsie" class="sister" id="link1">
        <span>Elsie</span>
    </a>
    <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
    <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
    and they lived at the bottom of a well.
</p>
<p class="story">...</p>
"""
from bs4 import BeautifulSoup
soup=BeautifulSoup(html_doc,'lxml')

#1、按照标签名查找
# print(soup.find_all('a'))
# print(soup.find_all('a',id='link3'))
# print(soup.find_all('a',id='link3',attrs={'class':"sister"}))
#
# print(soup.find_all('a')[0].find('span')) #嵌套查找


#2、按照属性查找
# print(soup.p.find_all(attrs={'id':'link1'})) #等同于print(soup.find_all(id='link1'))
# print(soup.p.find_all(attrs={'class':'sister'}))
#
# print(soup.find_all(class_='sister'))


#3、按照文本内容查找
print(soup.p.find_all(text="The Dormouse's story")) # 按照完整内容匹配（是==而不是in）,得到的结果也是内容

#更多：https://www.crummy.com/software/BeautifulSoup/bs4/doc/index.zh.html#find
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

### 　　4、Css选择器

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
##该模块提供了select方法来支持css
html_doc = """
<html><head><title>The Dormouse's story</title></head>
<body>
<p class="title">
    <b>The Dormouse's story</b>
    Once upon a time there were three little sisters; and their names were
    <a href="http://example.com/elsie" class="sister" id="link1">
        <span>Elsie</span>
    </a>
    <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
    <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
    <div class='panel-1'>
        <ul class='list' id='list-1'>
            <li class='element'>Foo</li>
            <li class='element'>Bar</li>
            <li class='element'>Jay</li>
        </ul>
        <ul class='list list-small' id='list-2'>
            <li class='element'><h1 class='yyyy'>Foo</h1></li>
            <li class='element xxx'>Bar</li>
            <li class='element'>Jay</li>
        </ul>
    </div>
    and they lived at the bottom of a well.
</p>
<p class="story">...</p>
"""
from bs4 import BeautifulSoup
soup=BeautifulSoup(html_doc,'lxml')

#1、CSS选择器
print(soup.p.select('.sister'))
print(soup.select('.sister span'))

print(soup.select('#link1'))
print(soup.select('#link1 span'))

print(soup.select('#list-2 .element.xxx'))

print(soup.select('#list-2')[0].select('.element')) #可以一直select,但其实没必要,一条select就可以了

# 2、获取属性
print(soup.select('#list-2 h1')[0].attrs)

# 3、获取内容
print(soup.select('#list-2 h1')[0].get_text())
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

### 　　5、总结

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
# 总结:
#1、推荐使用lxml解析库
#2、讲了三种选择器:标签选择器,find与find_all，css选择器
    1、标签选择器筛选功能弱,但是速度快
    2、建议使用find,find_all查询匹配单个结果或者多个结果
    3、如果对css选择器非常熟悉建议使用select
#3、记住常用的获取属性attrs和文本值get_text()的方法
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

## 二、Xpath解析库

### 　　1、绝对路径与相对路径

　　如果"/"处在XPath表达式开头则表示文档根元素，（表达式中间作为分隔符用以分割每一个步进表达式）如：/messages/message/subject是一种绝对路径表示法，它表明是从文档根开始查找节点。假设当前节点是在第一个message节点【/messages/message[1]】，则路径表达式subject（路径前没有"/"）这种表示法称为相对路径，表明从当前节点开始查找。具体请见下面所述的"表达式上下文"。

### 　　2、表达式上下文

　　上下文其实表示一种环境。以明确当前XPath路径表达式处在什么样的环境下执行。例如同样一个路径表达式处在对根节点操作的环境和处在对某一个特定子节点操作的环境下执行所获得的结果可能是完全不一样的。也就是说XPath路径表达式计算结果取决于它所处的上下文。

　　XPath上下文基本有以下几种：

![img](https://images.cnblogs.com/OutliningIndicators/ContractedBlock.gif) View Code

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
当前节点(./)：如./sender表示选择当前节点下的sender节点集合（等同于下面所讲的"特定元素"，如：sender）

父节点(../)：如../sender表示选择当前节点的父节点下的sender节点集合

根元素（/）：如/messages表示选择从文档根节点下的messages节点集合.

根节点（/*）：这里的*是代表所有节点，但是根元素只有一个，所以这里表示根节点。/*的返回结果和/messages返回的结果一样都是messages节点。

递归下降（//）:如当前上下文是messages节点。则//sender将返回以下结果：

/messages//sender :

<sender>gkt1980@gmail.com</sender>

<sender>111@gmail.com</sender>

<sender>333@gmail.com</sender>

 

/messages/message[1]//sender:

<sender>gkt1980@gmail.com</sender>

<sender>111@gmail.com</sender>

我们可以看出XPath表达式返回的结果是：从当前节点开始递归步进搜索当前节点下的所有子节点找到满足条件的节点集。
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

### 　　3、**运算符及特殊字符**

| **运算符/特殊字符** | **说明**                                                     |
| ------------------- | ------------------------------------------------------------ |
| /                   | 此路径运算符出现在模式开头时，表示应从根节点选择。           |
| //                  | 从当前节点开始递归下降，此路径运算符出现在模式开头时，表示应从根节点递归下降。 |
| .                   | 当前上下文。                                                 |
| ..                  | 当前上下文节点父级。                                         |
| *                   | 通配符；选择所有元素节点与元素名无关。（不包括文本，注释，指令等节点，如果也要包含这些节点请用node()函数） |
| @                   | 属性名的前缀。                                               |
| @*                  | 选择所有属性，与名称无关。                                   |
| :                   | 命名空间分隔符；将命名空间前缀与元素名或属性名分隔。         |
| ( )                 | 括号运算符(优先级最高)，强制运算优先级。                     |
| [ ]                 | 应用筛选模式（即谓词，包括"过滤表达式"和"轴（向前/向后）"）。 |
| [ ]                 | 下标运算符；用于在集合中编制索引。                           |
| \|                  | 两个节点集合的联合，如：//messages/message/to \| //messages/message/cc |
| -                   | 减法。                                                       |
| div，               | 浮点除法。                                                   |
| and, or             | 逻辑运算。                                                   |
| mod                 | 求余。                                                       |
| not()               | 逻辑非                                                       |
| =                   | 等于                                                         |
| ！=                 | 不等于                                                       |
| 特殊比较运算符      | < 或者 &lt;<= 或者 &lt;=> 或者 &gt;>= 或者 &gt;=需要转义的时候必须使用转义的形式，如在XSLT中，而在XMLDOM的scripting中不需要转义。 |

　4、常用表达式

　　

| /                                                            | Document Root文档根.                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| /*                                                           | 选择文档根下面的所有元素节点，即根节点（XML文档只有一个根节点） |
| /node()                                                      | 根元素下所有的节点（包括文本节点，注释节点等）               |
| /text()                                                      | 查找文档根节点下的所有文本节点                               |
| /messages/message                                            | messages节点下的所有message节点                              |
| /messages/message[1]                                         | messages节点下的第一个message节点                            |
| /messages/message[1]/self::node()                            | 第一个message节点（self轴表示自身，node()表示选择所有节点）  |
| /messages/message[1]/node()                                  | 第一个message节点下的所有子节点                              |
| /messages/message[1]/*[last()]                               | 第一个message节点的最后一个子节点                            |
| /messages/message[1]/[last()]                                | Error，谓词前必须是节点或节点集                              |
| /messages/message[1]/node()[last()]                          | 第一个message节点的最后一个子节点                            |
| /messages/message[1]/text()                                  | 第一个message节点的所有子节点                                |
| /messages/message[1]//text()                                 | 第一个message节点下递归下降查找所有的文本节点（无限深度）    |
| /messages/message[1] /child::node()/messages/message[1] /node()/messages/message[position()=1]/node()//message[@id=1] /node() | 第一个message节点下的所有子节点                              |
| //message[@id=1] //child::node()                             | 递归所有子节点（无限深度）                                   |
| //message[position()=1]/node()                               | 选择id=1的message节点以及id=0的message节点                   |
| /messages/message[1] /parent::*                              | Messages节点                                                 |
| /messages/message[1]/body/attachments/parent::node()/messages/message[1]/body/attachments/parent::* /messages/message[1]/body/attachments/.. | attachments节点的父节点。父节点只有一个,所以node()和* 返回结果一样。（..也表示父节点. 表示自身节点） |
| //message[@id=0]/ancestor::*                                 | Ancestor轴表示所有的祖辈，父，祖父等。向上递归               |
| //message[@id=0]/ancestor-or-self::*                         | 向上递归,包含自身                                            |
| //message[@id=0]/ancestor::node()                            | 对比使用*,多一个文档根元素(Document root)                    |
| /messages/message[1]/descendant::node()//messages/message[1]//node() | 递归下降查找message节点的所有节点                            |
| /messages/message[1]/sender/following::*                     | 查找第一个message节点的sender节点后的所有同级节点，并对每一个同级节点递归向下查找。 |
| //message[@id=1]/sender/following-sibling::*                 | 查找id=1的message节点的sender节点的所有后续的同级节点。      |
| //message[@id=1]/datetime/@date                              | 查找id=1的message节点的datetime节点的date属性                |
| //message[@id=1]/datetime[@date]//message/datetime[attribute::date] | 查找id=1的message节点的所有含有date属性的datetime节点        |
| //message[datetime]                                          | 查找所有含有datetime节点的message节点                        |
| //message/datetime/attribute::*//message/datetime/attribute::node()//message/datetime/@* | 返回message节点下datetime节点的所有属性节点                  |
| //message/datetime[attribute::*]//message/datetime[attribute::node()]//message/datetime[@*]//message/datetime[@node()] | 选择所有含有属性的datetime节点                               |
| //attribute::*                                               | 选择根节点下的所有属性节点                                   |
| //message[@id=0]/body/preceding::node()                      | 顺序选择body节点所在节点前的所有同级节点。（查找顺序为：先找到body节点的顶级节点（根节点）,得到根节点标签前的所有同级节点，执行完成后继续向下一级，顺序得到该节点标签前的所有同级节点，依次类推。）注意：查找同级节点是顺序查找，而不是递归查找。 |
| //message[@id=0]/body/preceding-sibling::node()              | 顺序查找body标签前的所有同级节点。（和上例一个最大的区别是：不从最顶层开始到body节点逐层查找。我们可以理解成少了一个循环，而只查找当前节点前的同级节点） |
| //message[@id=1]//*[namespace::amazon]                       | 查找id=1的所有message节点下的所有命名空间为amazon的节点。    |
| //namespace::*                                               | 文档中的所有的命名空间节点。（包括默认命名空间xmlns:xml）    |
| //message[@id=0]//books/*[local-name()='book']               | 选择books下的所有的book节点，注意：由于book节点定义了命名空间<amazone:book>.若写成//message[@id=0]//books/book则查找不出任何节点。 |
| //message[@id=0]//books/*[local-name()='book' and namespace-uri()='http://www.amazon.com/books/schema'] | 选择books下的所有的book节点，(节点名和命名空间都匹配)        |
| //message[@id=0]//books/*[local-name()='book'][year>2006]    | 选择year节点值>2006的book节点                                |
| //message[@id=0]//books/*[local-name()='book'][1]/year>2006  | 指示第一个book节点的year节点值是否大于2006.返回xs:boolean: true |