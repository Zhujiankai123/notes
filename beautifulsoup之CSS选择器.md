## [beautifulsoup之CSS选择器](https://www.cnblogs.com/kongzhagen/p/6472746.html)

BeautifulSoup支持大部分的CSS选择器，其语法为：向tag或soup对象的.select()方法中传入字符串参数，选择的结果以列表形式返回。

　　tag.select("string")

　　BeautifulSoup.select("string")

 

源代码示例：

```
html = """
<html>
    <head>
        <title>The Dormouse's story</title>
    </head>
    <body>
        <p class="title" name="dromouse">
            <b>The Dormouse's story</b>
        </p>
        <p class="story">
            Once upon a time there were three little sisters; and their names were
            <a class="mysis" href="http://example.com/elsie" id="link1">
                <b>the first b tag<b>
                Elsie
            </a>,
            <a class="mysis" href="http://example.com/lacie" id="link2" myname="kong">
                Lacie
            </a>and
            <a class="mysis" href="http://example.com/tillie" id="link3">
                Tillie
            </a>;and they lived at the bottom of a well.
        </p>
        <p class="story">
            myStory
            <a>the end a tag</a>
        </p>
        <a>the p tag sibling</a>
    </body>
</html>
"""
soup = BeautifulSoup(html,'lxml')
```

　　1、通过标签选择

```
`# 选择所有title标签``soup.``select``(``"title"``)``# 选择所有p标签中的第三个标签``soup.``select``(``"p:nth-of-type(3)"``) 相当于soup.``select``(p)[2]``# 选择body标签下的所有a标签``soup.``select``(``"body a"``)``# 选择body标签下的直接a子标签``soup.``select``(``"body > a"``)``# 选择id=link1后的所有兄弟节点标签``soup.``select``(``"#link1 ~ .mysis"``)``# 选择id=link1后的下一个兄弟节点标签``soup.``select``(``"#link1 + .mysis"``)`
```

　　2、通过类名查找

```
`# 选择a标签，其类属性为mysis的标签``soup.``select``(``"a.mysis"``)`
```

　　3、通过id查找

```
`# 选择a标签，其id属性为link1的标签``soup.``select``(``"a#link1"``)`
```

　　4、通过【属性】查找，当然也适用于class

```
`# 选择a标签，其属性中存在myname的所有标签``soup.``select``(``"a[myname]"``)``# 选择a标签，其属性href=http://example.com/lacie的所有标签``soup.``select``(``"a[href='http://example.com/lacie']"``)``# 选择a标签，其href属性以http开头``soup.``select``(``'a[href^="http"]'``)``# 选择a标签，其href属性以lacie结尾``soup.``select``(``'a[href$="lacie"]'``)``# 选择a标签，其href属性包含.com``soup.``select``(``'a[href*=".com"]'``)``# 从html中排除某标签，此时soup中不再有script标签``[s.extract() ``for` `s ``in` `soup(``'script'``)]``# 如果想排除多个呢``[s.extract() ``for` `s ``in` `soup([``'script'``,``'fram'``]`
```

　　

　　5、获取文本及属性

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
html_doc = """<html>
    <head>
        <title>The Dormouse's story</title>
    </head>
<body>
    <p class="title"><b>The Dormouse's story</b></p>
    <p class="story">Once upon a time there were three little sisters; and their names were
        <a href="http://example.com/elsie" class="sister" id="link1">Elsie</a>,
        <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
        <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
    </p>
        and they lived at the bottom of a well.
    <p class="story">...</p>
</body>
"""
from bs4 import BeautifulSoup
'''
以列表的形式返回
'''
soup = BeautifulSoup(html_doc, 'html.parser')
s = soup.select('p.story')
s[0].get_text()  # p节点及子孙节点的文本内容
s[0].get_text("|")  # 指定文本内容的分隔符
s[0].get_text("|", strip=True)  # 去除文本内容前后的空白
print(s[0].get("class"))  # p节点的class属性值列表（除class外都是返回字符串）
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

`　　6、UnicodeDammit.detwingle()` 方法只能解码包含在UTF-8编码中的Windows-1252编码内容,

```
new_doc = UnicodeDammit.detwingle(doc)
print(new_doc.decode("utf8"))
# ☃☃☃“I like snowmen!”
```

在创建 `BeautifulSoup` 或 `UnicodeDammit` 对象前一定要先对文档调用 `UnicodeDammit.detwingle()` 确保文档的编码方式正确.如果尝试去解析一段包含Windows-1252编码的UTF-8文档,就会得到一堆乱码,比如: â˜ƒâ˜ƒâ˜ƒ“I like snowmen!”.

　　

　　7、其它：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
html_doc = """<html>
    <head>
        <title>The Dormouse's story</title>
    </head>
<body>
    <p class="title"><b>The Dormouse's story</b></p>
    <p class="story">Once upon a time there were three little sisters; and their names were
        <a href="http://example.com/elsie" class="sister" id="link1">Elsie</a>,
        <a href="http://example.com/lacie" class="sister" id="link2">Lacie</a> and
        <a href="http://example.com/tillie" class="sister" id="link3">Tillie</a>;
    </p>
        and they lived at the bottom of a well.
    <p class="story">...</p>
</body>
"""
from bs4 import BeautifulSoup
'''
以列表的形式返回
'''
soup = BeautifulSoup(html_doc, 'html.parser')
soup.select('title')  # title标签
soup.select("p:nth-of-type(3)")  # 第三个p节点
soup.select('body a')  # body下的所有子孙a节点
soup.select('p > a')  # 所有p节点下的所有a直接节点
soup.select('p > #link1')  # 所有p节点下的id=link1的直接子节点
soup.select('#link1 ~ .sister')  # id为link1的节点后面class=sister的所有兄弟节点
soup.select('#link1 + .sister')  # id为link1的节点后面class=sister的第一个兄弟节点
soup.select('.sister')  # class=sister的所有节点
soup.select('[class="sister"]')  # class=sister的所有节点
soup.select("#link1")  # id=link1的节点
soup.select("a#link1")  # a节点，且id=link1的节点
soup.select('a[href]')  # 所有的a节点，有href属性
soup.select('a[href="http://example.com/elsie"]')  # 指定href属性值的所有a节点
soup.select('a[href^="http://example.com/"]')  # href属性以指定值开头的所有a节点
soup.select('a[href$="tillie"]')  # href属性以指定值结尾的所有a节点
soup.select('a[href*=".com/el"]')  # 支持正则匹配
```