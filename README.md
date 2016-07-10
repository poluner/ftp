指令格式：[规定分隔符为空格或者换行或tab]

列出当前目录结构：list

进入目录x：cd x

返回上一层目录：cd\

删除一个文件（或文件夹）x：delete x

删除n个文件（或文件夹）x0,x1,...,xn-1：

deleteN n

x0

x1

... 

xn-1

设下载一个文件为：op="download"，下载多个文件为：op="downloadN"

上传一个文件为：op="upload"，上传多个文件为：op="uploadN"

主动模式为：model="port" port_client，被动模式为：model="passive"

二进制方式为：way="binary"，ascii方式为：way="ascii"

指定服务器文件相对路径为s（相对于服务器当前路径），服务器文件绝对路径为c

则一个文件操作为：

op model way s c

多个文件操作为：

op model way n

s0 c0

s1 c1

...

sn-1 cn-1
