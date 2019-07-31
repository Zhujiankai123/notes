# matplotlib笔记

​	一般常用引入matplotlib方式：

```python
import matplotlib.pyplot as plt
```

​	同时，由于matplotlib实质上内部都是先转换成nparray处理的，所以一般习惯同时引入numpy或pandas：

```python
import numpy as np
import pandas as pd
#df = pd.Dataframe(something)
#df_2_nparray = df.values
#
#np0 = np.matrix(something)
#np0_2_nparray = np.asarray(np0)
```

​	一般画图可以直接使用plt.plot绘制，多次使用该函数或是在函数里多次输入x和y的值，会在同一图表中同时绘制全部图像：

```python
#eg：
import matplotlib.pylot as plt
import numpy as np
datax = np.array([1,2,3,4])
plt.plot(datax,datax,'ro',label='linner',datax,datax**2,'b-',lable='quadratic',datax,datax**3,'y^',label='cubic') 
#上述例子即将三个图表写一个plot中，其中'ro'、'b-'、'y^'为绘图曲线的描述字符串，第一个字母代表颜色，第二个代表坐标点的图案。
#或着可以使用如下写法：
#plt.plot(datax,datax,'ro',label='linner') 
#plt.plot(datax,datax**2,'b-',lable='quadratic') 
#plt.plot(datax,datax**3,'y^',label='cubic') 
plt.xlabel('x label')
plt.ylabel('y label')
plt.title("Simple Plot")
plt.legend() #添加图例区域
plt.show() 
```

​	由于多幅图表显示在一个figure(matplotlib概念为画布，一个画布可以存在多个axes子图表，每个axes子图表可以是2个axis即2维，也可以是3axis即三维)中会显得拥挤和复杂，所以可以使用子图表方法将多个图表分别画在不同的axes里。有两种写法，分别使用subplot和subplots函数：

```python
#eg1,using subplot：
import matplotlib.pylot as plt
import numpy as np
datax = np.array([1,2,3,4])
subplot(1,3,1) #也可写成subplot(131)，表示1行*3列三个图表区域，当前在第一个图表区域绘制
plt.plot(datax,datax,'ro',label='linner') 
plt.axis = [1,4,1,4] #手动定义xy轴显示范围，分别对应[xmin,xmax,ymin,ymax]
subplot(1,3,2)
plt.plot(datax,datax**2,'b-',lable='quadratic') 
subplot(1,3,2)
plt.plot(datax,datax**3,'y^',label='cubic') 
plt.show() 

#eg2,using subplots
import matplotlib.pylot as plt
import numpy as np
datax = np.array([1,2,3,4])
fig,ax = subplots(1,3) #subplots参数12意义同subplot，返回fig画布对象和axes子图对象。
ax[0].plot = (datax,datax,'ro',label='linner') 
ax[1].plot(datax,datax**2,'b-',lable='quadratic') 
ax[2].plot(datax,datax**3,'y^',label='cubic') 
plt.show
```

###### plt.scatter函数：

​	plt.scatter为绘制散点图函数，其完整参数列表及其含义如下图：![](https://img-blog.csdn.net/20151104231449817?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

​	其中可以通过关键参数传入一个数据字典，以字典键值定义各参数的取值，例如官方文档中的例子：

```python
data = {'a': np.arange(50),
        'c': np.random.randint(0, 50, 50),
        'd': np.random.randn(50)}
data['b'] = data['a'] + 10 * np.random.randn(50)
data['d'] = np.abs(data['d']) * 100

plt.scatter('a', 'b', c='c', s='d', data=data)
plt.xlabel('entry a')
plt.ylabel('entry b')
plt.show()
```

​	其中1-5行分别用随机函数定义了字典data中a、b、c、d的值(nparray类型)，第七行中调用散点图绘制函数，传递data字典到关键字参数中，scatter函数xy取值分别为data[a]和data[b]，颜色参数c为data[c]，散点大小参数s为data[d]。

scatter其他的参数取值参考如下图(与其他绘制函数大致相同)：![](https://img-blog.csdn.net/20151104234024896?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![](https://img-blog.csdn.net/20151104234431749?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

###### 绘图参数设置及其他绘图函数

​	一般常见绘图函数包括plot曲线图、scatter散点图、bar直方图等等。除了在绘图函数中直接设置图像特征（如:plot(x,y,linewidth=2.0,'b-')）,还可以使用 setp()函数传递图像特征参数。

```python
lines = plt.plot(x1, y1, x2, y2) #绘制曲线图
plt.setp(lines, color='r', linewidth=2.0) #关键字参数设置颜色及线宽
plt.setp(lines, 'color', 'r', 'linewidth', 2.0) #matlab风格，用一对字符串表示颜色、线宽
```

