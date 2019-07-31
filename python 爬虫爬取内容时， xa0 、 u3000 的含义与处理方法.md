# python 爬虫爬取内容时， \xa0 、 \u3000 的含义与处理方法



**处理方法 str.replace(u'\xa0', u' ')**

最近用 scrapy 爬某网站，发现拿到的内容里面含有 \xa0 、 \u3000 这样的字符，起初还以为是编码不对，搜了一下才知道是见识太少 233 。

#### \xa0 是不间断空白符 `&nbsp;`

我们通常所用的空格是 \x20 ，是在[标准ASCII](https://en.wikipedia.org/wiki/ASCII)可见字符 0x20~0x7e 范围内。
而 \xa0 属于 latin1 （[ISO/IEC_8859-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1)）中的扩展字符集字符，代表空白符[nbsp(non-breaking space)](https://en.wikipedia.org/wiki/Non-breaking_space)。
latin1 字符集向下兼容 ASCII （ 0x20~0x7e ）。通常我们见到的字符多数是 latin1 的，比如在 MySQL 数据库中。

这里也有一张简陋的[Latin1字符集对照表](http://casa.colorado.edu/~ajsh/iso8859-1.html)。

#### \u3000 是全角的空白符

根据[Unicode编码标准](https://en.wikipedia.org/wiki/Unicode)及其[基本多语言面](https://en.wikipedia.org/wiki/Plane_(Unicode)#Basic_Multilingual_Plane)的定义， \u3000 属于[CJK字符](https://en.wikipedia.org/wiki/CJK_characters)的[CJK标点符号](https://en.wikipedia.org/wiki/CJK_Symbols_and_Punctuation)[区块](https://en.wikipedia.org/wiki/Unicode_block)内，是[空白字符](https://en.wikipedia.org/wiki/Whitespace_character#Unicode)之一。它的名字是 Ideographic Space ，有人译作表意字空格、象形字空格等。顾名思义，就是全角的 CJK 空格。它跟 nbsp 不一样，是可以被换行间断的。常用于制造缩进， wiki 还说用于[抬头](https://en.wikipedia.org/wiki/Tai_tou)，但没见过。

这里还有一个 Unicode.org 上关于 CJK 标点符号块的[字符代码表](http://www.unicode.org/charts/PDF/U3000.pdf)。

转自https://www.cnblogs.com/my8100/p/7709371.html



# [HTML转义字符&npsp；表示non-breaking space，unicode编码为u'\xa0',超出gbk编码范围？](http://www.cnblogs.com/my8100/p/7709371.html)



# 0.目录

1.参考
2.问题定位
不间断空格的unicode表示为 u\xa0',超出gbk编码范围？
3.如何处理
.extract_first().replace(u'\xa0', u' ').strip().encode('utf-8','replace')

 

# 1.参考

[Beautiful Soup and Unicode Problems](https://stackoverflow.com/questions/19508442/beautiful-soup-and-unicode-problems)

详细解释

unicodedata.normalize('NFKD',string)  实际作用？？？

 

[Scrapy : Select tag with non-breaking space with xpath](https://stackoverflow.com/questions/35364069/scrapy-select-tag-with-non-breaking-space-with-xpath)

```
>>> selector.xpath(u'''



...     //p[normalize-space()]



...        [not(contains(normalize-space(), "\u00a0"))]
```

normalize-space() 实际作用？？？

 

```
In [244]: sel.css('.content')
Out[244]: [<Selector xpath=u"descendant-or-self::*[@class and contains(concat(' ', normalize-space(@class), ' '), ' content ')]" data=u'<p class="content text-
```

 

 

 

 

[BeautifulSoup下Unicode乱码解决 ](http://www.cnblogs.com/skymoney/p/python_bs_unicode.html)

```
s.replace(u``'\xa0'``, u'``').encode('``utf``-``8``')
```

 

# 2.问题定位

<https://en.wikipedia.org/wiki/Comparison_of_text_editors>

定位元素显示为 &npsp；

![img](https://images2017.cnblogs.com/blog/892328/201710/892328-20171022115649506-2066742783.jpg)