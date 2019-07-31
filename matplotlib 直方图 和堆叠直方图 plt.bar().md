# matplotlib 直方图 和堆叠直方图 plt.bar()



```
job_data = [470, 443, 240, 207, 202, 184, 151, 136, 124, 118]
how_many = [2620,1841,640,790,589,538,408,425,493,550]
labels =["北京","上海","深圳","郑州","广州","成都","杭州","长沙","武汉","大连"]

import matplotlib.pyplot as plt
import numpy as np
#ax2 = fig.add_subplot(222) #2X2 中的第一个子图
#bar(left, height, width, color, align, yerr)函数：绘制柱形图。
# left为x轴的位置序列，一般采用arange函数产生一个序列；
# height为y轴的数值序列，也就是柱形图的高度，一般就是我们需要展示的数据；
# width为柱形图的宽度，一般这是为1即可；color为柱形图填充的颜色;
# align设置plt.xticks()函数中的标签的位置；
# yerr让柱形图的顶端空出一部分。
# color设置柱状的颜色
# alpha 设置柱状填充颜色的透明度 大于0 小于等于1
# linewidth 线条的宽度

#设置各种参数
xlocation =  np.linspace(1, len(job_data) * 0.6, len(job_data)) #len(data个序列)
print(xlocation)
height01 = job_data
height02 = how_many
width = 0.2
color01='darkgoldenrod'
color02 = 'seagreen'

# 画柱状图
fig = plt.figure('十大热门城市招聘排行',figsize=(15,10)) #指定了图的名称 和画布的大小
fig.tight_layout()
ax1 = fig.add_subplot(221) #2X2 中的第一个子图
plt.suptitle('十大热门城市招聘排行', fontsize=15) # 添加图标题
#画图
rects01 = ax1.bar(xlocation, height01, width = 0.2, color=color01,linewidth=1,alpha=0.8)
rects02 = ax1.bar(xlocation+0.2,height02 ,width = 0.2, color=color02,linewidth=1,alpha=0.8)
#添加x轴标签
plt.xticks(xlocation+0.15,labels, fontsize=12 ,rotation = 20)  # 横坐标轴标签 rotation x轴标签旋转的角度
# 横纵坐标分别代表什么
plt.xlabel(u'地点', fontsize=15, labelpad=10)
plt.ylabel(u'职位数量', fontsize=15, labelpad=10)
#图例
ax1.legend((rects01,rects02),( u'职位数量',u'招聘人数'), fontsize=15)  # 图例
# 添加数据标签
for r1,r2 ,amount01,amount02 in zip(rects01, rects02,job_data,how_many):
        h01 = r1.get_height()
        h02 = r2.get_height()
        plt.text(r1.get_x(), h01, amount01, fontsize=13, va ='bottom')  # 添加职位数量标签
        plt.text(r2.get_x(), h02 , amount02, fontsize=13, va='bottom')  # 添加招聘人数

#画堆叠柱状图
data = np.array([[10., 30., 19., 22.],
                [5., 18., 15., 20.],
                [4., 6., 3., 5.]])
color_list = ['b', 'g', 'r']
ax2 = fig.add_subplot(222)
X = np.arange(data.shape[1])
print(X)
for i in range(data.shape[0]):#i表示list的索引值
    ax2.bar(X, data[i],
         width=0.2,
         bottom = np.sum(data[:i], axis = 0),
         color = color_list[i % len(color_list)],
            alpha =0.5
            )
plt.savefig('zhifangtu.png',dpi=120,bbox_inches='tight')
```

![img](https://img-blog.csdn.net/20180712223345665?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NpbmF0XzM4MzQwMTEx/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)