命令格式：[规定分隔符为空格或者换行或tab]

结束通信为：bye

列出当前目录结构：list

进入目录x：cd x

返回上一层目录：cd\

删除n个文件（或文件夹）x0,x1,...,xn-1：

delete n

x0

x1

... 

xn-1

设下载文件为：op="download"

上传文件为：op="upload"

主动模式为：model="port"...port_client，被动模式为：model="passive"

二进制方式为：way="binary"，ascii方式为：way="ascii"

指定服务器文件相对路径为s（相对于服务器当前路径），服务器文件绝对路径为c

则n个文件操作为：

op model way n

s0 c0 (port_client)//主动模式下port_client在路径最后输入

s1 c1 (port_client)

...

sn-1 cn-1 (port_client)
